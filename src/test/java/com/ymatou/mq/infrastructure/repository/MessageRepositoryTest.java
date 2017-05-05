/*
 *
 *  (C) Copyright 2017 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.mq.infrastructure.repository;

import com.ymatou.mq.infrastructure.BaseTest;
import com.ymatou.mq.infrastructure.model.Message;
import org.apache.commons.lang.time.DateFormatUtils;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * @author luoshiqian 2017/3/27 16:34
 */
public class MessageRepositoryTest extends BaseTest{

    @Autowired
    private MessageRepository messageRepository;

    @Test
    public void testSaveMessageAndDuplicateKey(){

        String id = new ObjectId().toHexString();
        Message message = new Message();
        message.setId(id);
        message.setAppId("infrasturcture_test");
        message.setBizId("123bcdf3");
        message.setBody("{\"orderId\":1321321}");
        message.setClientIp("127.0.0.1");
        message.setQueueCode("testJava");
        message.setRecvIp("127.0.0.1");
        message.setCreateTime(new Date());


        Assert.assertTrue(messageRepository.save(message));
        Assert.assertTrue(messageRepository.save(message));
        Assert.assertTrue(messageRepository.save(message));

    }
}
