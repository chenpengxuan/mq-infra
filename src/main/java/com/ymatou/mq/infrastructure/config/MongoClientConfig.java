/*
 *
 * (C) Copyright 2017 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.mq.infrastructure.config;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

/**
 * @author luoshiqian 2017/3/27 16:45
 */
@Component
public class MongoClientConfig {

    @Autowired
    private MongoConfig mongoConfig;

    private Map<String, MongoClient> mongoClientMap = Maps.newConcurrentMap();


    @PostConstruct
    public void init() {
        /**
         * 消息库
         */
        newMongoClient(mongoConfig.getMessageDbUri(), "messageMongoClient");

        /**
         * 配置库
         */
        newMongoClient(mongoConfig.getConfigDbUri(), "configMongoClient");

        /**
         * 补单库
         */
        newMongoClient(mongoConfig.getCompensateDbUri(), "messageCompensateMongoClient");

    }

    private void newMongoClient(String uri, String beanName) {

        MongoClientURI mongoClientURI = new MongoClientURI(uri);

        MongoClient mongoClient = new MongoClient(mongoClientURI);

        mongoClientMap.put(beanName, mongoClient);
    }


    public MongoClient getMongoClient(String beanName) {
        return mongoClientMap.get(beanName);
    }

    public MongoClient getMongoClientIfNullSet(MongoClient mongoClient, String beanName) {
        if (mongoClient == null) {
            synchronized (this) {
                if (mongoClient == null) {
                    mongoClient = getMongoClient(beanName);
                }
            }
        }
        return mongoClient;
    }
}
