/*
 *
 * (C) Copyright 2017 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.mq.infrastructure.filedb;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.PropertyFilter;
import com.google.common.collect.Maps;

/**
 * @author luoshiqian 2017/3/23 15:29
 */
public class FileDb {

    private static final Map<String, FileDb> INSTANCES = Maps.newConcurrentMap();

    private String dbPath;
    private FileDbConfig config;
    private DbProxy db;


    private FileDb() {}

    private FileDb(String dbPath) {
        this.dbPath = dbPath;
    }

    public String getDbPath() {
        return dbPath;
    }

    public static Map<String, FileDb> getInstances() {
        return INSTANCES;
    }

    public FileDbConfig getConfig() {
        return config;
    }

    public FileDb setConfig(FileDbConfig config) {
        this.config = config;
        return this;
    }

    public FileDb setDb(DbProxy db) {
        this.db = db;
        return this;
    }


    /**
     * FileDbConfig
     * 
     * @param fileDbConfig
     * @return
     */
    public static synchronized FileDb newFileDb(FileDbConfig fileDbConfig) {

        if (StringUtils.isBlank(fileDbConfig.getDbPath())) {
            throw new IllegalArgumentException("db path can not be empty!");
        }
        FileDb fileDb = INSTANCES.get(fileDbConfig.getDbPath());
        if (fileDb == null) {
            fileDb = new FileDb(fileDbConfig.getDbPath());
        }
        fileDb.setConfig(fileDbConfig).setDb(DbProxyFactory.build(fileDbConfig));

        return fileDb;
    }


    /**
     * put data
     * 
     * @param key
     * @param value
     */
    public void put(String key, String value) {
        db.put(key, value);
    }

    /**
     * 同步put data
     * @param key
     * @param value
     * @return
     */
    public boolean syncPut(String key, String value) {
        return db.syncPut(key,value);
    }


    /**
     * 返回状态
     * 
     * @return
     */
    public Map<String, Object> mapStatus() {
        return db.status();
    }

    public String status() {
        Map<String, Object> map = db.status();
        return JSON.toJSONString(map, new PropertyFilter() {
            @Override
            public boolean apply(Object object, String name, Object value) {
                if (name.equals("log")) {
                    return false;
                }
                return true;
            }
        });
    }

    /**
     * 重新设置
     * 
     * @param newConfig
     */
    public void reset(FileDbConfig newConfig) {
        db.reset(newConfig);
    }

    /**
     * 关闭
     */
    public void close(){
        db.close();
    }

}
