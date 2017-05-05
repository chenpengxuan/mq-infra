package com.ymatou.mq.infrastructure.model;

import java.util.Date;

/**
 * 回调结果
 * Created by zhangzhihua on 2017/4/7.
 */
public class CallbackResult {

    /**
     * appId
     */
    private String appId;

    /**
     * queueCode
     */
    private String queueCode;

    /**
     * 消费者id
     */
    private String consumerId;

    /**
     * msgId
     */
    private String msgId;

    /**
     * bizId
     */
    private String bizId;

    /**
     * 调用来源 1:分发站;2:补单站
     */
    private int from;

    /**
     * 请求url
     */
    private String url;

    /**
     * 请求内容
     */
    private String request;

    /**
     * 响应结果
     */
    private String response;

    /**
     * 请求时间
     */
    private Date reqTime;

    /**
     * 响应时间
     */
    private Date respTime;

    /**
     * 状态  0:初始化；1：成功;2：失败;3:补单中
     */
    private int status;

    /**
     * 调用次数
     */
    private int callNum;

    /**
     * 创建时间即接收消息时间
     */
    private Date createTime;

    public Date getReqTime() {
        return reqTime;
    }

    public void setReqTime(Date reqTime) {
        this.reqTime = reqTime;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public Date getRespTime() {
        return respTime;
    }

    public void setRespTime(Date respTime) {
        this.respTime = respTime;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getCallNum() {
        return callNum;
    }

    public void setCallNum(int callNum) {
        this.callNum = callNum;
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getQueueCode() {
        return queueCode;
    }

    public void setQueueCode(String queueCode) {
        this.queueCode = queueCode;
    }

    public String getConsumerId() {
        return consumerId;
    }

    public void setConsumerId(String consumerId) {
        this.consumerId = consumerId;
    }

    public String getBizId() {
        return bizId;
    }

    public void setBizId(String bizId) {
        this.bizId = bizId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
