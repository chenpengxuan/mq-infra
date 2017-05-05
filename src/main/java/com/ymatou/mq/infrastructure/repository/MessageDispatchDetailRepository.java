package com.ymatou.mq.infrastructure.repository;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.nin;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;
import static com.ymatou.mq.infrastructure.util.MongoHelper.*;

import java.util.Date;
import java.util.List;

import com.google.common.collect.Lists;
import com.ymatou.mq.infrastructure.util.NetUtil;
import org.bson.conversions.Bson;
import org.joda.time.DateTime;
import org.mongodb.morphia.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mongodb.*;
import com.ymatou.mq.infrastructure.config.MongoClientConfig;
import com.ymatou.mq.infrastructure.model.CallbackMessage;
import com.ymatou.mq.infrastructure.model.MessageDispatchDetail;
import com.ymatou.mq.infrastructure.support.MongoRepository;
import com.ymatou.mq.infrastructure.support.enums.DispatchStatusEnum;
import org.springframework.util.CollectionUtils;

/**
 * 消息分发明细数据操作 Created by zhangzhihua on 2017/3/24.
 */
@Component("messageDispatchDetailRepository")
public class MessageDispatchDetailRepository extends MongoRepository {

    private static final Logger logger = LoggerFactory.getLogger(MessageDispatchDetailRepository.class);

    @Autowired
    private MongoClientConfig mongoClientConfig;

    private MongoClient mongoClient;

    @Override
    protected MongoClient getMongoClient() {
        return mongoClientConfig.getMongoClientIfNullSet(mongoClient, "messageMongoClient");
    }

    public MessageDispatchDetail findById(String detailId,String msgId,String appId,String queueCode){
        String dbName = getDbName(appId, msgId);
        String collectionName = getMessageDetailCollectionName(queueCode);
        Query<MessageDispatchDetail> query =
                newQuery(MessageDispatchDetail.class, dbName, collectionName, ReadPreference.secondaryPreferred());

        return query.field("id").equal(detailId).get();
    }

    /**
     * 保存分发明细
     *
     * @param detail
     */
    public boolean saveDetail(MessageDispatchDetail detail) {
        return insertEntityIngoreDuplicateKey(getDbName(detail.getAppId(), detail.getMsgId()),
                getMessageDetailCollectionName(detail),
                detail);
    }

    /**
     * 更新分发明细，若不存在则插入
     * 
     * @param detail
     */
    public void updateDetail(MessageDispatchDetail detail) {
        String dbName = getDbName(detail.getAppId(), detail.getMsgId());
        String collectionName = getMessageDetailCollectionName(detail.getQueueCode());

        logger.debug("update detail,msgId:{}", detail.getMsgId());

        Bson doc = eq("_id", String.format("%s_%s", detail.getMsgId(), detail.getConsumerId()));
        Bson set = combine(
                set("msgId", detail.getMsgId()),
                set("bizId", detail.getBizId()),
                set("appId", detail.getAppId()),
                set("queueCode", detail.getQueueCode()),
                set("consumerId", detail.getConsumerId()),
                set("lastFrom", detail.getLastFrom()),
                set("lastTime", detail.getLastTime()),
                set("lastResp", detail.getLastResp()),
                set("status", detail.getStatus()),
                set("dealIp", NetUtil.getHostIp()),
                set("createTime", detail.getCreateTime()),
                set("updateTime", detail.getUpdateTime()));
        updateOne(dbName, collectionName, doc, set, true);
    }


}
