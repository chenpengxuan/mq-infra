/*
 *
 *  (C) Copyright 2017 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.mq.infrastructure.service;

import com.ymatou.mq.infrastructure.BaseTest;
import com.ymatou.mq.infrastructure.model.Message;
import com.ymatou.mq.infrastructure.repository.MessageRepository;
import com.ymatou.mq.infrastructure.service.MessageService;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * @author luoshiqian 2017/3/29 11:24
 */
public class MessageServiceTest extends BaseTest {

    @Autowired
    private MessageService messageService;

    @Autowired
    private MessageRepository messageRepository;

    @Test
    public void testSave(){
        String id = new ObjectId().toHexString();
        Message message = new Message();
        message.setId(id);
        message.setAppId("test_javaV2");
        message.setBizId("test233");
        message.setBody("{\"orderId\":1321321}");
        message.setClientIp("127.0.0.1");
        message.setQueueCode("test2");
        message.setRecvIp("127.0.0.1");
        message.setCreateTime(new Date());

        messageService.saveMessage(message);

        Message dbMessage = messageRepository.getById("test_javaV2","test2",id);

        Assert.assertNotNull(dbMessage);
    }

}
