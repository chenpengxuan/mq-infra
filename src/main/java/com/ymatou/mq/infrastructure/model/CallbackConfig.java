/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.mq.infrastructure.model;

import org.apache.commons.lang3.StringUtils;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Property;

import com.baidu.disconf.client.config.DisClientConfig;

/**
 * 回调配置
 * 
 * @author wangxudong 2016年8月2日 下午5:01:22
 *
 */
@Embedded
public class CallbackConfig extends PrintFriendliness{
    /**
     * 回调配置Id
     */
    @Property("CallbackKey")
    private String callbackKey;

    /**
     * 回调Url
     */
    @Property("Url")
    private String url;

    /**
     * 是否启用
     */
    @Property("Enable")
    private Boolean enable;

    /**
     * 是否重试
     */
    @Property("IsRetry")
    private Integer isRetry;

    /**
     * 业务端接受的HTTP类型，默认POST
     */
    @Property("HttpMethod")
    private String httpMethod;

    /**
     * 业务端接受的媒体类型，默认application/json
     */
    @Property("ContentType")
    private String contentType;

    /**
     * 回调业务端超时时间（0：不超时，大于0 则表示超时），默认不超时
     */
    @Property("CallbackTimeOut")
    private Integer callbackTimeOut;

    /**
     * 并发度，用于保护业务系统
     */
    @Property("ParallelismNum")
    private Integer parallelismNum;

    /**
     * 超时时间，默认10分钟，单位分钟
     */
    @Property("RetryTimeout")
    private Integer retryTimeout;

    /**
     * 重试补偿策略（Null或者空为每分钟补偿一次）
     * 例如：1m-5m-10m-30m-1h-4h-8h-15h-1d-3d
     */
    @Property("RetryPolicy")
    private String retryPolicy;

    /**
     * 订阅者AppId
     */
    @Property("CallbackAppId")
    private String callbackAppId;

    /**
     * 秒级补单时间间隔（单位：秒）
     */
    @Property("SecondCompensateSpan")
    private Integer secondCompensateSpan;

    /**
     * 所属队列配置
     */
    private QueueConfig queueConfig;

    /**
     * 只在stg生效
     */
    @Property("OnlyStgEnable")
    private Boolean onlyStgEnable;

    /**
     * 放空处理，不回调
     */
    @Property("AbandonQueue")
    private Boolean abandonQueue;

    /**
     * @return the callbackKey
     */
    public String getCallbackKey() {
        return callbackKey;
    }

    /**
     * @param callbackKey the callbackKey to set
     */
    public void setCallbackKey(String callbackKey) {
        this.callbackKey = callbackKey;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return the enable
     */
    public Boolean getEnable() {
        //没有配置 默认是true
        if(enable == null){
            return Boolean.TRUE;
        }
        return enable;
    }

    /**
     * @param enable the enable to set
     */
    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    /**
     * @return the isRetry
     */
    public Integer getIsRetry() {
        return isRetry;
    }

    /**
     * @param isRetry the isRetry to set
     */
    public void setIsRetry(Integer isRetry) {
        this.isRetry = isRetry;
    }

    /**
     * @return the httpMethod
     */
    public String getHttpMethod() {
        return httpMethod;
    }

    /**
     * @param httpMethod the httpMethod to set
     */
    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    /**
     * @return the contentType
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * @param contentType the contentType to set
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * @return the callbackTimeOut
     */
    public Integer getCallbackTimeOut() {
        return callbackTimeOut;
    }

    /**
     * @param callbackTimeOut the callbackTimeOut to set
     */
    public void setCallbackTimeOut(Integer callbackTimeOut) {
        this.callbackTimeOut = callbackTimeOut;
    }

    /**
     * @return the parallelismNum
     */
    public final Integer getParallelismNum() {
        return parallelismNum;
    }

    /**
     * @param parallelismNum the parallelismNum to set
     */
    public final void setParallelismNum(Integer parallelismNum) {
        this.parallelismNum = parallelismNum;
    }

    /**
     * 获取到超时时间（没填或者<50,返回默认值5000）
     * 
     * @return
     */
    public int getTimeout() {
        Integer timeout = getCallbackTimeOut();
        if (timeout == null || timeout.intValue() < 50) {
            return 5000;
        } else {
            return timeout.intValue();
        }
    }

    /**
     * @return the retryTimeout
     */
    public final Integer getRetryTimeout() {
        if (retryTimeout == null) {
            return 30;
        } else {
            return retryTimeout;
        }
    }

    public Boolean getOnlyStgEnable() {
        if(null == onlyStgEnable){
            return Boolean.FALSE;
        }
        return onlyStgEnable;
    }

    public void setOnlyStgEnable(Boolean onlyStgEnable) {
        this.onlyStgEnable = onlyStgEnable;
    }

    /**
     * @param retryTimeout the retryTimeout to set
     */
    public final void setRetryTimeout(Integer retryTimeout) {
        this.retryTimeout = retryTimeout;
    }

    /**
     * @return the callbackAppId
     */
    public final String getCallbackAppId() {
        return callbackAppId;
    }

    /**
     * @param callbackAppId the callbackAppId to set
     */
    public final void setCallbackAppId(String callbackAppId) {
        this.callbackAppId = callbackAppId;
    }

    /**
     * @return the secondCompensateSpan
     */
    public Integer getSecondCompensateSpan() {
        if(secondCompensateSpan == null){
            return 0;
        }
        return secondCompensateSpan;
    }

    /**
     * @param secondCompensateSpan the secondCompensateSpan to set
     */
    public void setSecondCompensateSpan(Integer secondCompensateSpan) {
        this.secondCompensateSpan = secondCompensateSpan;
    }

    public Boolean getAbandonQueue() {
        return abandonQueue == null?false:abandonQueue;
    }

    public void setAbandonQueue(Boolean abandonQueue) {
        this.abandonQueue = abandonQueue;
    }

    /**
     * @return the retryPolicy
     */
    public String getRetryPolicy() {
        if(retryPolicy == null){
            //默认为空时 使用此值
            return "1m-5m-10m-30m-1h-4h-8h-15h-1d-2d";
        }
        return retryPolicy;
    }

    /**
     * @param retryPolicy the retryPolicy to set
     */
    public void setRetryPolicy(String retryPolicy) {
        this.retryPolicy = retryPolicy;
    }

    public QueueConfig getQueueConfig() {
        return queueConfig;
    }

    public void setQueueConfig(QueueConfig queueConfig) {
        this.queueConfig = queueConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()){
            return false;
        }
        if (this == o) return true;

        CallbackConfig that = (CallbackConfig) o;
        if(StringUtils.isBlank(callbackKey)){
            return false;
        }
        return callbackKey.equals(that.callbackKey);
    }


    /**
     * 是否回调生效
     * 
     * @return
     */
    public boolean isCallbackEnable() {
        // 若队列配置、回调配置开启
        if (this.getQueueConfig().getEnable() && this.getEnable()) {
            // 若队列配置开启消息存储
            if (this.getQueueConfig().getEnableLog()) {
                // 若开启补单
                if (this.getIsRetry() != null && this.getIsRetry() > 0) {
                    // 若 只要stg开启
                    if (this.getOnlyStgEnable()) {
                        // 当前环境是STG
                        if (DisClientConfig.getInstance().ENV.equals("STG")) {
                            return true;
                        }
                    } else {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 判断接收是否可用
     * @return
     */
    public boolean isReceiveEnable() {
        // 若队列配置、回调配置开启
        if (this.getQueueConfig().getEnable() && this.getEnable()) {
            // 若 只要stg开启
            if (this.getOnlyStgEnable()) {
                // 当前环境是STG
                if (DisClientConfig.getInstance().ENV.equals("STG")) {
                    return true;
                }
            } else {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断分发是否可用
     * @return
     */
    public boolean isDispatchEnable() {
        // 若队列配置、回调配置开启
        if (this.getQueueConfig().getEnable() && this.getEnable()) {
            // 若 只要stg开启
            if (this.getOnlyStgEnable()) {
                // 当前环境是STG
                if (DisClientConfig.getInstance().ENV.equals("STG")) {
                    return true;
                }
            } else {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断补单是否可用
     * @return
     */
    public boolean isCompensateEnable() {
        // 若队列配置、回调配置开启
        if (this.getQueueConfig().getEnable() && this.getEnable()) {
            // 若队列配置开启消息存储
            if (this.getQueueConfig().getEnableLog()) {
                // 若开启补单
                if (this.getIsRetry() != null && this.getIsRetry() > 0) {
                    // 若 只要stg开启
                    if (this.getOnlyStgEnable()) {
                        // 当前环境是STG
                        if (DisClientConfig.getInstance().ENV.equals("STG")) {
                            return true;
                        }
                    } else {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isOnlyStgCallback(){
        if(DisClientConfig.getInstance().ENV.equals("STG") && this.getOnlyStgEnable()){
            return true;
        }
        return false;
    }
}
