package com.ymatou.mq.infrastructure.model;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

import java.util.Date;

/**
 * 消息分发明细模型 Created by zhangzhihua on 2017/3/24.
 */
@Entity(noClassnameStored = true)
public class MessageDispatchDetail extends PrintFriendliness{

    /**
     * 唯一标识 uuid + "_" + callback_id
     */
    @Id
    private String id;

    /**
     * 消息主表id
     */
    @Property("msgId")
    private String msgId;

    /**
     * 应用id
     */
    @Property("appId")
    private String appId;

    /**
     * 队列code
     */
    @Property("queueCode")
    private String queueCode;

    /**
     * 消息id,客户端消息标识
     */
    @Property("bizId")
    private String bizId;

    /**
     * 订阅者id
     */
    @Property("consumerId")
    private String consumerId;

    /**
     * 消息分发状态 0:初始化；1：成功;2：失败;3:补单中
     */
    @Property("status")
    private int status;

    /**
     * 调用次数
     */
    @Property("callNum")
    private int callNum;

    /**
     * 最后一次调用来源 0:初始化;1:分发站;2:补单站
     */
    @Property("lastFrom")
    private int lastFrom;

    /**
     * 最后一次调用时间
     */
    @Property("lastTime")
    private Date lastTime;

    /**
     * 最后一次调用响应结果
     */
    @Property("lastResp")
    private String lastResp;

    /**
     * 创建时间
     */
    @Property("createTime")
    private Date createTime;

    /**
     * 更新时间
     */
    @Property("updateTime")
    private Date updateTime;

    /**
     * 处理ip
     */
    @Property("dealIp")
    private String dealIp;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
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

    public String getBizId() {
        return bizId;
    }

    public void setBizId(String bizId) {
        this.bizId = bizId;
    }

    public String getConsumerId() {
        return consumerId;
    }

    public void setConsumerId(String consumerId) {
        this.consumerId = consumerId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getCallNum() {
        return callNum;
    }

    public void setCallNum(int callNum) {
        this.callNum = callNum;
    }

    public int getLastFrom() {
        return lastFrom;
    }

    public void setLastFrom(int lastFrom) {
        this.lastFrom = lastFrom;
    }

    public String getLastResp() {
        return lastResp;
    }

    public void setLastResp(String lastResp) {
        this.lastResp = lastResp;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getLastTime() {
        return lastTime;
    }

    public void setLastTime(Date lastTime) {
        this.lastTime = lastTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }


    public String getDealIp() {
        return dealIp;
    }

    public void setDealIp(String dealIp) {
        this.dealIp = dealIp;
    }
}
