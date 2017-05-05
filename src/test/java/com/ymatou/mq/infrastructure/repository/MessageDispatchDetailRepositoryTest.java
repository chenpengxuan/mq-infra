/*
 *
 *  (C) Copyright 2017 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.mq.infrastructure.repository;

import com.ymatou.mq.infrastructure.BaseTest;
import com.ymatou.mq.infrastructure.model.Message;
import com.ymatou.mq.infrastructure.model.MessageDispatchDetail;
import org.apache.commons.lang.time.DateFormatUtils;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * @author luoshiqian 2017/3/27 18:41
 */
public class MessageDispatchDetailRepositoryTest extends BaseTest {

    @Autowired
    private MessageDispatchDetailRepository repository;

    @Test
    public void testSave(){

        String id = new ObjectId().toHexString();
        MessageDispatchDetail detail = new MessageDispatchDetail();
        detail.setId(id+"_callbackKey");
        detail.setAppId("infrasturcture_test");
        detail.setBizId("123bcdf3");
        detail.setCallNum(1);
        detail.setLastFrom(1);
        detail.setMsgId(id);
        detail.setConsumerId("callbackKey");

        detail.setQueueCode("testJava");

        detail.setCreateTime(new Date());


        Assert.assertTrue(repository.saveDetail(detail));
        Assert.assertTrue(repository.saveDetail(detail));
        Assert.assertTrue(repository.saveDetail(detail));

    }

}
