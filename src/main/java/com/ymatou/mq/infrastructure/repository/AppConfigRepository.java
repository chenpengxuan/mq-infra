/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.mq.infrastructure.repository;

import com.mongodb.MongoClient;
import com.ymatou.mq.infrastructure.config.MongoClientConfig;
import com.ymatou.mq.infrastructure.support.MongoRepository;
import com.ymatou.mq.infrastructure.model.AppConfig;
import com.ymatou.mq.infrastructure.util.SpringContextHolder;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class AppConfigRepository extends MongoRepository{

    @Autowired
    private MongoClientConfig mongoClientConfig;
    private MongoClient mongoClient;

    private static final String dbName = "MQ_Configuration_201609";

    /*
     * (non-Javadoc)
     * 
     * @see com.ymatou.messagebus.infrastructure.mongodb.MongoRepository#getMongoClient()
     */
    @Override
    protected MongoClient getMongoClient() {
        return mongoClientConfig.getMongoClientIfNullSet(mongoClient,"configMongoClient");
    }


    /**
     * 统计出配置App的总数
     * 
     * @return
     */
    public long count() {
        Datastore datastore = getDatastore(dbName);

        Query<AppConfig> find = datastore.find(AppConfig.class);

        return find.countAll();
    }

    /**
     * 返回AppConfig
     * 
     * @return
     */
    public AppConfig getAppConfig(String appId) {
        Datastore datastore = getDatastore(dbName);

        return datastore.find(AppConfig.class).field("_id").equal(appId).get();
    }

    /**
     * @return
     */
    public List<AppConfig> getAllAppConfig() {
        Datastore datastore = getDatastore(dbName);

        return datastore.find(AppConfig.class).asList();
    }
}
