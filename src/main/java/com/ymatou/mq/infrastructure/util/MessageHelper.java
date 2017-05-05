/*
 *
 * (C) Copyright 2017 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.mq.infrastructure.util;

import java.util.Date;

import com.ymatou.mq.infrastructure.model.CallbackMessage;
import com.ymatou.mq.infrastructure.model.Message;
import com.ymatou.mq.infrastructure.model.MessageCompensate;
import com.ymatou.mq.infrastructure.model.MessageDispatchDetail;
import com.ymatou.mq.infrastructure.support.enums.CallbackFromEnum;
import com.ymatou.mq.infrastructure.support.enums.CompensateFromEnum;
import com.ymatou.mq.infrastructure.support.enums.CompensateStatusEnum;

/**
 * @author luoshiqian 2017/4/13 11:14
 */
public class MessageHelper {

    public static CallbackMessage fromCompensation(MessageCompensate compensate, CallbackFromEnum lastFrom) {
        CallbackMessage message = new CallbackMessage();

        message.setId(compensate.getMsgId());
        message.setAppId(compensate.getAppId());
        message.setQueueCode(compensate.getQueueCode());
        message.setCallbackKey(compensate.getConsumerId());
        message.setBizId(compensate.getBizId());
        message.setBody(compensate.getBody());

        message.setCreateTime(compensate.getCreateTime());
        message.setRequestTime(new Date());
        message.setLastFrom(lastFrom);
        message.setResponse(compensate.getLastResp());
        message.setResponseTime(compensate.getLastTime());

        message.setRetryNums(compensate.getCompensateNum() + 1);

        return message;
    }

    public static CallbackMessage fromMessage(Message message) {
        CallbackMessage callbackMessage = Converter.convert(message, CallbackMessage.class);
        return callbackMessage;
    }

    public static MessageCompensate fromMessageDispatchDetail(MessageDispatchDetail dispatchDetail,
            CompensateFromEnum fromEnum) {
        MessageCompensate messageCompensate = new MessageCompensate();
        messageCompensate.setId(dispatchDetail.getId());
        messageCompensate.setMsgId(dispatchDetail.getMsgId());
        messageCompensate.setAppId(dispatchDetail.getAppId());
        messageCompensate.setQueueCode(dispatchDetail.getQueueCode());
        messageCompensate.setBizId(dispatchDetail.getBizId());
        messageCompensate.setConsumerId(dispatchDetail.getConsumerId());
        messageCompensate.setStatus(CompensateStatusEnum.COMPENSATE.getCode());
        messageCompensate.setSource(fromEnum.getCode());
        messageCompensate.setCompensateNum(0);
        messageCompensate.setCreateTime(new Date());
        messageCompensate.setUpdateTime(new Date());
        messageCompensate.setNextTime(new Date());


        return messageCompensate;

    }

}
