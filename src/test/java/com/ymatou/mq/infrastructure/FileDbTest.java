/*
 *
 * (C) Copyright 2017 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.mq.infrastructure;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.ymatou.mq.infrastructure.filedb.FileDb;
import com.ymatou.mq.infrastructure.filedb.FileDbConfig;

/**
 * @author luoshiqian 2017/3/24 10:48
 */
public class FileDbTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileDbTest.class);

    FileDb fileDb;
    FileDbConfig fileDbConfig;

    public void read() {

        fileDbConfig = FileDbConfig.newInstance()
                .setDbName("test")
                .setDbPath("/data/mq/test")
                .setConsumer(pair -> {
                    LOGGER.info("consume success key:{},value:{}", pair.getKey(), pair.getValue());
                    try {
                        TimeUnit.MILLISECONDS.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return false;
                })
                .setMaxConsumeSizeInDuration(100)
                .setConsumeDuration(1000)
                .setConsumerThreadNums(2)
                .setPutExceptionHandler((key, value, throwable) -> {
                    LOGGER.error("put error key:{},value:{}", key, value, throwable);
                });

        fileDb = FileDb.newFileDb(fileDbConfig);
    }

    public void normal() {

        fileDbConfig = FileDbConfig.newInstance()
                .setDbName("test")
                .setDbPath("/data/mq/test")
                .setConsumer(pair -> {
                    LOGGER.info("consume success key:{},value:{}", pair.getKey(), pair.getValue());
                    try {
                        TimeUnit.MILLISECONDS.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return true;
                })
                .setMaxConsumeSizeInDuration(100)
                .setConsumeDuration(1000)
                .setConsumerThreadNums(2)
                .setPutExceptionHandler((key, value, throwable) -> {
                    LOGGER.error("put error key:{},value:{}", key, value, throwable);
                });

        fileDb = FileDb.newFileDb(fileDbConfig);
    }

    public void quickTest() {

        fileDbConfig = FileDbConfig.newInstance()
                .setDbName("test")
                .setDbPath("/data/mq/test")
                .setConsumer(pair -> {
                    return true;
                })
                .setMaxConsumeSizeInDuration(10000)
                .setConsumeDuration(10)
                .setConsumerThreadNums(10)
                .setPutExceptionHandler((key, value, throwable) -> {
                    LOGGER.error("put error key:{},value:{}", key, value, throwable);
                });

        fileDb = FileDb.newFileDb(fileDbConfig);
    }


    @Test
    public void testReadSize(){
        read();
        LOGGER.info("{}",fileDb.status());
    }

    @Test
    public void testPut() throws Exception {
        normal();

        for (int i = 0; i < 1000000; i++) {
            fileDb.put(RandomStringUtils.randomNumeric(10),"value");
        }

        TimeUnit.SECONDS.sleep(3);
        System.out.println(fileDb.status());

        TimeUnit.SECONDS.sleep(10);

    }


    @Test
    public void testToString(){
        LinkedBlockingQueue<Pair<String,String>> consumeQueue = new LinkedBlockingQueue<>();

        consumeQueue.add(Pair.of("key","value"));
        consumeQueue.add(Pair.of("key1","value1"));
        consumeQueue.add(Pair.of("key2","value2"));

        System.out.println(JSON.toJSONString(consumeQueue));
    }

    @Test
    public void testPutAndConsume()throws Exception{

        quickTest();

        new Thread(() -> {
            for (int i = 0; i < 1000000000; i++) {
                fileDb.put(RandomStringUtils.randomNumeric(10),"value");
//                try {
//                    TimeUnit.MICROSECONDS.sleep(1);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            }
        }).start();

        int time = 700;
        int i = 0;
        while (i < time){

            LOGGER.info("{}",fileDb.status());

            TimeUnit.SECONDS.sleep(1);
            i++;
        }
    }


    @Test
    public void testPutAndConsumeAndReset_ForCorrect()throws Exception{

        quickTest();

        new Thread(() -> {
            for (int i = 0; i < 1000000000; i++) {
                fileDb.put(new ObjectId().toHexString(),"value");
                try {
                    TimeUnit.MICROSECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        int time = 60;
        int i = 0;
        while (i < time){
            LOGGER.info("{}",fileDb.status());
            if(i == 10 ){
                fileDb.reset(FileDbConfig.newInstance().setConsumeDuration(3000).setConsumerThreadNums(1).setMaxConsumeSizeInDuration(100));
            }

            if(i == 20){
                fileDb.reset(FileDbConfig.newInstance().setConsumeDuration(2000).setConsumerThreadNums(5).setMaxConsumeSizeInDuration(1000));
            }

            if(i == 40 ){
                fileDb.reset(FileDbConfig.newInstance().setConsumeDuration(1000).setConsumerThreadNums(20).setMaxConsumeSizeInDuration(10000));
            }
            TimeUnit.SECONDS.sleep(1);
            i++;
        }
        LOGGER.info("{}",fileDb.status());
        System.exit(-1);
    }
}
