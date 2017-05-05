/*
 *
 *  (C) Copyright 2017 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.mq.infrastructure.model;

import com.ymatou.mq.infrastructure.support.enums.CallbackFromEnum;
import org.mongodb.morphia.annotations.Property;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author luoshiqian 2017/4/13 17:11
 */
public class CallbackMessage extends PrintFriendliness{
    /**
     * 唯一标识
     */
    private String id;

    /**
     * 应用id
     */
    private String appId;

    /**
     * 队列code
     */
    private String queueCode;

    /**
     * 回调KEY
     */
    private String callbackKey;

    /**
     * 消息id,客户端消息标识
     */
    private String bizId;

    /**
     * 消息体
     */
    private String body;

    /**
     * 发送方客户端ip
     */
    private String clientIp;

    /**
     * 接收站ip
     */
    private String recvIp;


    /**
     * 回调来源
     */
    private CallbackFromEnum lastFrom;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 请求时间
     */
    private Date requestTime;

    /**
     * 响应时间
     */
    private Date responseTime;

    /**
     * 回调响应内容
     */
    private String response;

    /**
     * 重试次数
     */
    private int retryNums;

    /**
     * 下一次时间
     */
    private Date nextTime;

    //异常信息
    private Exception ex;

    //秒补次数
    private int secondCompensateNums;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getQueueCode() {
        return queueCode;
    }

    public void setQueueCode(String queueCode) {
        this.queueCode = queueCode;
    }

    public String getCallbackKey() {
        return callbackKey;
    }

    public void setCallbackKey(String callbackKey) {
        this.callbackKey = callbackKey;
    }

    public String getBizId() {
        return bizId;
    }

    public void setBizId(String bizId) {
        this.bizId = bizId;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Date getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(Date requestTime) {
        this.requestTime = requestTime;
    }

    public Date getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(Date responseTime) {
        this.responseTime = responseTime;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public int getRetryNums() {
        return retryNums;
    }

    public void setRetryNums(int retryNums) {
        this.retryNums = retryNums;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getNextTime() {
        return nextTime;
    }

    public void setNextTime(Date nextTime) {
        this.nextTime = nextTime;
    }

    public CallbackFromEnum getLastFrom() {
        return lastFrom;
    }

    public void setLastFrom(CallbackFromEnum lastFrom) {
        this.lastFrom = lastFrom;
    }

    public Exception getEx() {
        return ex;
    }

    public void setEx(Exception ex) {
        this.ex = ex;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getRecvIp() {
        return recvIp;
    }

    public void setRecvIp(String recvIp) {
        this.recvIp = recvIp;
    }

    public int getSecondCompensateNums() {
        return secondCompensateNums;
    }

    public void setSecondCompensateNums(int secondCompensateNums) {
        this.secondCompensateNums = secondCompensateNums;
    }
}
