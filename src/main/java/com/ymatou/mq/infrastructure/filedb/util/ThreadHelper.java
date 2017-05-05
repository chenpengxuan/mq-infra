/*
 *
 *  (C) Copyright 2017 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.mq.infrastructure.filedb.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * @author luoshiqian 2017/3/23 16:54
 */
public class ThreadHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadHelper.class);

    /**
     * 设置线程工厂 线程名称 传入 rpc-pool format: rpc-pool-%d 生成 rpc-pool-1,rpc-pool-2
     *
     * @param threadName
     * @return
     */
    public static ThreadFactory threadFactory(String threadName) {
        return new ThreadFactoryBuilder().setNameFormat(threadName + "-%d").build();
    }


    /**
     * new 固定大小线程 tostring可以看到线程池情况
     * @param threadCount
     * @param maxSize
     * @param threadFactory
     * @return
     */
    public static ExecutorService newFixedThreadPool(int threadCount,int maxSize,ThreadFactory threadFactory){
        return new MyThreadPoolExecutor(threadCount, threadCount,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(maxSize),threadFactory);
    }

    /**
     * new 固定大小线程 tostring可以看到线程池情况
     * @param threadCount
     * @param threadFactory
     * @return
     */
    public static ExecutorService newFixedThreadPool(int threadCount,ThreadFactory threadFactory){
        return new MyThreadPoolExecutor(threadCount, threadCount,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),threadFactory);
    }

    /**
     * new 固定大小线程 tostring可以看到线程池情况
     * @param threadFactory
     * @return
     */
    public static ExecutorService newSingleThreadPool(ThreadFactory threadFactory){
        return new MyThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),threadFactory);
    }

    /**
     * new 固定大小线程 tostring可以看到线程池情况
     * @param threadFactory
     * @return
     */
    public static ExecutorService newSingleThreadPool(int capacity,ThreadFactory threadFactory){
        return new MyThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(capacity),threadFactory);
    }

    public static void sleep(long time){
        try {
            TimeUnit.MILLISECONDS.sleep(time);
        } catch (InterruptedException e) {
            LOGGER.warn("try put sleep InterruptedException",e);
        }
    }
}
