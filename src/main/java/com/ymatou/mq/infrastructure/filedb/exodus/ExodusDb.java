/*
 *
 *  (C) Copyright 2017 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.mq.infrastructure.filedb.exodus;

import static com.ymatou.mq.infrastructure.filedb.util.ThreadHelper.*;
import static jetbrains.exodus.bindings.StringBinding.entryToString;
import static jetbrains.exodus.bindings.StringBinding.stringToEntry;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import com.dianping.cat.Cat;
import com.ymatou.performancemonitorclient.PerformanceStatisticContainer;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ymatou.mq.infrastructure.filedb.DbProxy;
import com.ymatou.mq.infrastructure.filedb.FileDbConfig;
import com.ymatou.mq.infrastructure.filedb.util.ThreadHelper;

import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.env.*;
import jetbrains.exodus.io.FileDataReader;
import jetbrains.exodus.io.FileDataWriter;
import jetbrains.exodus.log.LogConfig;

/**
 * @author luoshiqian 2017/3/23 16:15
 */
public class ExodusDb implements DbProxy {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExodusDb.class);
    //最大size 20万
    private static final int MAX_QUEUE_SIZE = 200000;
    //存储线程最大队列长度
    private static final int PUT_MAX_QUEUE_SIZE = 1000000;

    private EnvironmentConfig environmentConfig;
    private Environment environment;
    private Store store;

    private AtomicBoolean isRunning = new AtomicBoolean(true);

    /**
     * 消息 入文件线程池 singlethread
     */
    private ExecutorService putExecutor;

    /**
     * 消息入文件失败重试线程池 singlethread
     */
    private ExecutorService retryExecutor;

    /**
     * 从文件中删数据 调度线程池 singlethread
     */
    private ExecutorService deleteExecutor;

    /**
     * 统计数据上传
     */
    private ExecutorService statisticsExecutor;

    /**
     * 从文件取出数据后 消息者线程池 由使用方定
     */
    private volatile ExecutorService consumeExecutor;
    private FileDbConfig config;

    /**
     * 放入文件队列 失败的队列
     */
    private BlockingQueue<Pair<String,String>> failedQueue = new LinkedBlockingQueue<>(MAX_QUEUE_SIZE);

    /**
     * 从文件取数据 放入此消费队列
     */
    private BlockingQueue<Pair<String,String>> consumeQueue = new LinkedBlockingQueue<>();

    /**
     * 从失败队列重试放入失败次数，如果一个key放于失败超过10次 就不再放入，调用异常处理
     */
    private int failedNums = 0;

    private AtomicLong putCount = new AtomicLong(0);
    private AtomicLong consumedCount = new AtomicLong(0);

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public ExecutorService getDeleteExecutor() {
        return deleteExecutor;
    }

    public void setDeleteExecutor(ExecutorService deleteExecutor) {
        this.deleteExecutor = deleteExecutor;
    }

    public ExecutorService getConsumeExecutor() {
        return consumeExecutor;
    }

    public void setConsumeExecutor(ExecutorService consumeExecutor) {
        this.consumeExecutor = consumeExecutor;
    }

    public FileDbConfig getConfig() {
        return config;
    }

    public void setConfig(FileDbConfig config) {
        this.config = config;
    }

    public BlockingQueue<Pair<String, String>> getFailedQueue() {
        return failedQueue;
    }

    public void setFailedQueue(BlockingQueue<Pair<String, String>> failedQueue) {
        this.failedQueue = failedQueue;
    }

    public BlockingQueue<Pair<String, String>> getConsumeQueue() {
        return consumeQueue;
    }

    public void setConsumeQueue(BlockingQueue<Pair<String, String>> consumeQueue) {
        this.consumeQueue = consumeQueue;
    }

    public int getFailedNums() {
        return failedNums;
    }

    public void setFailedNums(int failedNums) {
        this.failedNums = failedNums;
    }

    public EnvironmentConfig getEnvironmentConfig() {
        return environmentConfig;
    }

    public void setEnvironmentConfig(EnvironmentConfig environmentConfig) {
        this.environmentConfig = environmentConfig;
    }

    public ExodusDb(FileDbConfig fileDbConfig) {

        environmentConfig = new EnvironmentConfig()
                .setLogFileSize(fileDbConfig.getLogFileSize())// 40m one file
                .setLogSyncPeriod(5000);// 5秒fsync周期配置
        this.setEnvironmentConfig(environmentConfig);
        this.config = fileDbConfig;

        File directory = new File(fileDbConfig.getDbPath());
        if(!directory.exists()){
            boolean result = directory.mkdirs();
            if ( !result) {
                LOGGER.error("Failed to create dir:{}", fileDbConfig.getDbPath());
            }
        }

        //初始化env
        environment = Environments.newInstance(
                LogConfig.create(new FileDataReader(new File(fileDbConfig.getDbPath()), 500),
                        new FileDataWriter(directory)), environmentConfig);

        //初始化store
        store = environment.computeInTransaction(
                txn -> environment.openStore(config.getDbName(), StoreConfig.WITHOUT_DUPLICATES_WITH_PREFIXING, txn));

        //初始化线程池
        initExecutors();

        //重试
        retryExecutor.execute(() -> {
            while (isRunning.get()){

                try {
                    Pair<String,String> pair = failedQueue.take();
                    tryPut(pair);

                } catch (Exception e) {
                    LOGGER.error("retryExecutor error",e);
                }
            }
        });

        //消息删除线程
        consume();

        //上报统计数据
        statisticsExecutor.execute(() -> {
            while (isRunning.get()){
                try {
                    environment.executeInReadonlyTransaction(txn ->{
                        int count = Long.valueOf(store.count(txn)).intValue();
                        Cat.logMetricForSum(config.getDbName(),count);
                        LOGGER.info("filedbStatistics name:{},count:{}",config.getDbName(),count);
                    });

                    TimeUnit.SECONDS.sleep(60);
                } catch (Exception e) {
                    LOGGER.error("statisticsExecutor cat log error",e);
                }
            }
        });

    }

    /**
     * 消息删除线程
     */
    private void consume() {

        deleteExecutor.execute(() -> {

            while (isRunning.get()) {

                try {
                    long startTime = System.currentTimeMillis();
                    int maxConsumeSize = config.getMaxConsumeSizeInDuration();
                    environment.executeInReadonlyTransaction(txn -> {

                        try (Cursor cursor = store.openCursor(txn)) {
                            while (cursor.getNext()) {
                                ByteIterable byteKey = cursor.getKey();
                                String key = entryToString(byteKey);
                                String value = entryToString(cursor.getValue());

                                if (consumeQueue.size() < maxConsumeSize) {
                                    consumeQueue.add(Pair.of(key, value));
                                } else {
                                    break;
                                }
                            }
                        }
                    });

                    // 消费
                    CountDownLatch latch = new CountDownLatch(consumeQueue.size());

                    while (consumeQueue.size() > 0) {
                        Pair<String, String> pair = consumeQueue.poll();
                        consumeExecutor.execute(() -> {
                            try {
                                boolean success = config.getConsumer().apply(pair);
                                if (success) {
                                    environment.executeInTransaction(txn -> {
                                        store.delete(txn, stringToEntry(pair.getKey()));
                                    });
                                    reportCatConsumeTotal();
                                }

                                consumedCount.incrementAndGet();
                            } finally {
                                latch.countDown();
                            }
                        });
                    }


                    // 最多等30秒
                    latch.await(30, TimeUnit.SECONDS );

                    long duration = System.currentTimeMillis() - startTime;
                    long consumeDuration = config.getConsumeDuration();
                    if (duration < consumeDuration) { // 如果处理时间小于 间隔
                        // sleep 消息间隔
                        sleep(consumeDuration - duration);
                    }

                } catch (Exception e) {
                    LOGGER.error("deleteExecutor occur exception", e);
                }
            }

        });
    }

    /**
     * 初始化线程池
     */
    private void initExecutors() {
        putExecutor = newSingleThreadPool(PUT_MAX_QUEUE_SIZE,newThreadFactory("ExodusDb_Put_Thread"));
        retryExecutor = newSingleThreadPool(newThreadFactory("ExodusDb_Retry_Thread"));
        deleteExecutor = newSingleThreadPool(newThreadFactory("ExodusDb_Delete_Thread_"));
        statisticsExecutor = newSingleThreadPool(newThreadFactory("ExodusDb_Statistics_Thread_"));

        consumeExecutor = newFixedThreadPool(config.getConsumerThreadNums(), MAX_QUEUE_SIZE, newThreadFactory("ExodusDb_Consume_Thread"));
    }


    private ThreadFactory newThreadFactory(String name){
        return ThreadHelper.threadFactory(name +"_" +config.getDbName());
    }


    /**
     * 尽量保证成功，失败后 sleep 1 ms 如果失败10次 就放掉，打印error
     * @param pair
     */
    private void tryPut(Pair<String,String> pair){
        if(putForResult(pair.getKey(),pair.getValue())){//成功
            failedNums = 0;
        }else {
            failedNums ++;
            if(failedNums < 10){
                sleep(1);
                tryPut(pair);
            }else {//超过10次还失败 调用exception handler
                handlePutException(pair.getKey(),pair.getValue(),Optional.empty());
                failedNums = 0;
            }
        }
    }

    private boolean putForResult(String key,String value){
        AtomicBoolean success = new AtomicBoolean(false);
        environment.executeInTransaction(txn -> {
            store.put(txn,stringToEntry(key),stringToEntry(value));

            success.set(txn.commit());
        });
        return success.get();
    }

    @Override
    public void put(String key, String value) {
        try {
            putExecutor.execute(() -> {

                try {
                    boolean success = putForResult(key, value);
                    // commit 不成功 放入队列 之后再commit
                    if (!success) {
                        LOGGER.info("local file commit fail. {}:{}", key, value);

                        if (failedQueue.size() > 100000) {
                            LOGGER.error("commit fail size is too large :{}", failedQueue.size());
                            handlePutException(key, value,Optional.empty());
                        } else {
                            failedQueue.add(Pair.of(key, value));
                        }
                    }
                } catch (Exception e) {
                    // 异常直接调用异常处理器
                    LOGGER.error("putExecutor execption ", e);
                    handlePutException(key, value,Optional.of(e));
                } finally {
                    putCount.incrementAndGet();
                    reportCatPutTotal();
                }
            });
        } catch (Exception e) {
            //处理极端情形下RejectException
            handlePutException(key,value,Optional.of(e));
            reportCatPutTotal();
        }

    }

    /**
     * 同步保存数据
     * @param key
     * @param value
     * @return
     */
    public boolean syncPut(String key,String value){
        try {
            boolean result = putForResult(key, value);
            return result;
        } catch (Exception e) {
            // 异常直接调用异常处理器
            LOGGER.error("putExecutor execption ", e);
            return false;
        }finally {
            reportCatPutTotal();
        }
    }

    private void handlePutException(String key,String value,Optional<Throwable> optional){
        if(null != config.getPutExceptionHandler()){
            consumeExecutor.execute(() -> config.getPutExceptionHandler().handleException(key,value, optional));
        }
    }

    @Override
    public void close() {

        isRunning.set(false);

        if(putExecutor != null){
            putExecutor.shutdownNow();
        }

        if(retryExecutor != null){
            retryExecutor.shutdownNow();
        }

        if(consumeExecutor != null){
            consumeExecutor.shutdownNow();
        }

        if (deleteExecutor != null ) {
            deleteExecutor.shutdownNow();
        }

        if(statisticsExecutor != null){
            deleteExecutor.shutdownNow();
        }

        if(environment.isOpen()){
            environment.close();
        }
    }

    @Override
    public void reset(FileDbConfig newConfig) {

        LOGGER.info("exodusdb resetting,newConfig:{},oldConfig:{}",newConfig,this.config);
        if (newConfig.getConsumeDuration() != this.config.getConsumeDuration()) {
            this.config.setConsumeDuration(newConfig.getConsumeDuration());
        }
        if (newConfig.getMaxConsumeSizeInDuration() != this.config.getMaxConsumeSizeInDuration()) {
            this.config.setMaxConsumeSizeInDuration(newConfig.getMaxConsumeSizeInDuration());
        }

        // 重新设置 消费线程数
        if (newConfig.getConsumerThreadNums() != this.config.getConsumerThreadNums()) {
            // 新生成线程池
            this.config.setConsumerThreadNums(newConfig.getConsumerThreadNums());

            ExecutorService newExecutor = newFixedThreadPool(newConfig.getConsumerThreadNums(), MAX_QUEUE_SIZE,
                    newThreadFactory("ExodusDb_Consume_Thread"));
            ExecutorService oldExecutor = this.consumeExecutor;
            this.consumeExecutor = newExecutor;

            // 关闭老线程池 ，关闭前会执行完已定交的任务
            oldExecutor.shutdown();
        }
    }

    @Override
    public Map<String,Object> status() {
        Map<String,Object> map = new LinkedHashMap<>();

        map.put("dbName",config.getDbName());
        environment.executeInReadonlyTransaction(txn -> {
            map.put("totalLeft",store.count(txn));
        });
        map.put("putCount",putCount.get());
        map.put("consumedCount",consumedCount.get());
        map.put("consumeQueue",consumeQueue.size());
        map.put("failedQueue",failedQueue.size());
        map.put("failedNums",failedNums);
        map.put("consumeExecutor",consumeExecutor.toString());
        map.put("putExecutor",putExecutor.toString());
        map.put("retryExecutor",retryExecutor.toString());
        map.put("deleteExecutor",deleteExecutor.toString());
        map.put("config",config);

        map.put("environment",environment);


        return map;
    }


    public void reportCatPutTotal(){
        Cat.logMetricForSum(config.getDbName()+"_putTotal",1);
    }

    public void reportCatConsumeTotal(){
        Cat.logMetricForSum(config.getDbName()+"_consumeTotal",1);
    }
}
