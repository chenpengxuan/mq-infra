/*
 *
 * (C) Copyright 2017 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.mq.infrastructure.filedb;

import java.util.function.Function;

import com.ymatou.mq.infrastructure.model.PrintFriendliness;
import org.apache.commons.lang3.tuple.Pair;

import com.alibaba.fastjson.JSON;

/**
 * @author luoshiqian 2017/3/23 15:56
 */
public class FileDbConfig extends PrintFriendliness {

    // 文件路径 或 文件夹路径
    private String dbPath;

    // store的名称，以及 线程池中的名称
    private String dbName = "default";

    // 日志文件大小 单位kb default: 40Mb
    private long logFileSize = 40 * 1024;

    // 消费者 消费处理成功 返回true 删除文件数据
    private Function<Pair<String, String>, Boolean> consumer;

    // 消费间隔 ms default: 1秒
    private volatile long consumeDuration = 1000;

    // 一次消费间隔最大消费数
    private volatile int maxConsumeSizeInDuration = 500;

    // 消费者线程数 默认1
    private int consumerThreadNums = 1;

    // 放入异常处理器
    private PutExceptionHandler putExceptionHandler;


    public Function<Pair<String, String>, Boolean> getConsumer() {
        return consumer;
    }

    public long getLogFileSize() {
        return logFileSize;
    }

    public FileDbConfig setLogFileSize(long logFileSize) {
        this.logFileSize = logFileSize;
        return this;
    }

    public String getDbPath() {
        return dbPath;
    }

    public FileDbConfig setDbPath(String dbPath) {
        this.dbPath = dbPath;
        return this;
    }

    public long getConsumeDuration() {
        return consumeDuration;
    }

    public FileDbConfig setConsumeDuration(long consumeDuration) {
        this.consumeDuration = consumeDuration;
        return this;
    }

    public FileDbConfig setConsumer(Function<Pair<String, String>, Boolean> consumer) {
        this.consumer = consumer;
        return this;
    }

    public int getMaxConsumeSizeInDuration() {
        return maxConsumeSizeInDuration;
    }

    public FileDbConfig setMaxConsumeSizeInDuration(int maxConsumeSizeInDuration) {
        this.maxConsumeSizeInDuration = maxConsumeSizeInDuration;
        return this;
    }

    public int getConsumerThreadNums() {
        return consumerThreadNums;
    }

    public FileDbConfig setConsumerThreadNums(int consumerThreadNums) {
        this.consumerThreadNums = consumerThreadNums;
        return this;
    }

    public PutExceptionHandler getPutExceptionHandler() {
        return putExceptionHandler;
    }

    public FileDbConfig setPutExceptionHandler(PutExceptionHandler putExceptionHandler) {
        this.putExceptionHandler = putExceptionHandler;
        return this;
    }

    public String getDbName() {
        return dbName;
    }

    public FileDbConfig setDbName(String dbName) {
        this.dbName = dbName;
        return this;
    }

    public static FileDbConfig newInstance() {
        return new FileDbConfig();
    }


    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
