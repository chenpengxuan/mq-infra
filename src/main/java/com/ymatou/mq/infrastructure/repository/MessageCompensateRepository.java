package com.ymatou.mq.infrastructure.repository;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.inc;
import static com.mongodb.client.model.Updates.set;
import static com.ymatou.mq.infrastructure.util.MongoHelper.buildCompensateId;
import static com.ymatou.mq.infrastructure.util.MongoHelper.getMessageCompensateCollectionName;

import java.util.Date;
import java.util.List;

import com.google.common.collect.Lists;
import com.mongodb.*;
import com.mongodb.operation.UpdateOperation;
import com.ymatou.mq.infrastructure.model.CallbackMessage;
import com.ymatou.mq.infrastructure.util.NetUtil;
import org.bson.conversions.Bson;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ymatou.mq.infrastructure.config.MongoClientConfig;
import com.ymatou.mq.infrastructure.model.MessageCompensate;
import com.ymatou.mq.infrastructure.support.MongoRepository;
import com.ymatou.mq.infrastructure.support.enums.CompensateStatusEnum;

/**
 * 消息补单数据操作
 * Created by zhangzhihua on 2017/3/24.
 */
@Component("messageCompensateRepository")
public class MessageCompensateRepository extends MongoRepository {

    @Autowired
    private MongoClientConfig mongoClientConfig;

    private MongoClient mongoClient;

    public static final String dbName = "JMQ_V2_Message_Compensate";

    @Override
    protected MongoClient getMongoClient() {
        return mongoClientConfig.getMongoClientIfNullSet(mongoClient, "messageCompensateMongoClient");
    }


    public MessageCompensate findByQueueCodeAndId(String appId,String queueCode,String id){
        String collectionName = getMessageCompensateCollectionName(appId,queueCode);

        Query<MessageCompensate> query = newQuery(MessageCompensate.class,dbName,collectionName, ReadPreference.secondaryPreferred());
        query.field("id").equal(id);

        return query.get();
    }

    /**
     * 保存补单
     * @param messageCompensate
     */
    public void saveCompensate(MessageCompensate messageCompensate) {
        String collectionName = getMessageCompensateCollectionName(messageCompensate.getAppId(),messageCompensate.getQueueCode());

        insertEntityIngoreDuplicateKey(dbName,collectionName,messageCompensate);
    }

    /**
     * 更新补单
     * @param messageCompensate
     */
    public void updateCompensate(MessageCompensate messageCompensate){
        String collectionName = getMessageCompensateCollectionName(messageCompensate.getAppId(),messageCompensate.getQueueCode());
        Bson doc = eq("_id", messageCompensate.getId());
        Bson set = combine(
                set("status", messageCompensate.getStatus())
        );
        updateOne(dbName,collectionName,doc,set);
    }




}
