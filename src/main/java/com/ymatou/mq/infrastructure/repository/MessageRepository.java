package com.ymatou.mq.infrastructure.repository;

import static com.ymatou.mq.infrastructure.util.MongoHelper.getDbName;
import static com.ymatou.mq.infrastructure.util.MongoHelper.getMessageCollectionName;

import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mongodb.*;
import com.ymatou.mq.infrastructure.config.MongoClientConfig;
import com.ymatou.mq.infrastructure.model.Message;
import com.ymatou.mq.infrastructure.support.MongoRepository;

/**
 * 消息数据操作 Created by zhangzhihua on 2017/3/23.
 */
@Component("messageRepository")
public class MessageRepository extends MongoRepository {

    @Autowired
    private MongoClientConfig mongoClientConfig;

    private MongoClient mongoClient;

    @Override
    protected MongoClient getMongoClient() {
        return mongoClientConfig.getMongoClientIfNullSet(mongoClient, "messageMongoClient");
    }

    /**
     * 保存消息
     *
     * @param msg
     * @return true 保存成功
     */
    public boolean save(Message msg) {
        return insertEntityIngoreDuplicateKey(getDbName(msg.getAppId(), msg.getId()),
                getMessageCollectionName(msg.getQueueCode()),
                msg);
    }


    public Message getById(String appId, String queueCode, String id) {
        String dbName = getDbName(appId, id);
        String collectionName = getMessageCollectionName(queueCode);

        Query<Message> query = newQuery(Message.class, dbName, collectionName, ReadPreference.secondaryPreferred());
        Message message = query.field("id").equal(id).get();

        return message;
    }


    public void ensureIndex(String dbName, String collectionName) {
        DBCollection dbCollection = getCollection(dbName, collectionName);

        DBObject indexCreateTime = new BasicDBObject();
        indexCreateTime.put("createTime", 1);
        dbCollection.createIndex(indexCreateTime);

        DBObject indexBizId = new BasicDBObject();
        indexBizId.put("bizId", 1);
        dbCollection.createIndex(indexBizId);
    }
}
