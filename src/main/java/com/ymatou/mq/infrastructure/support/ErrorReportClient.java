/*
 *
 *  (C) Copyright 2017 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */
package com.ymatou.mq.infrastructure.support;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import com.ymatou.errorreporter.api.BufferedErrorReporter;
import com.ymatou.errorreporter.api.HttpPostErrorConsumer;
import com.ymatou.mq.infrastructure.model.CallbackConfig;
import com.ymatou.mq.infrastructure.model.CallbackMessage;

@Component
public class ErrorReportClient implements InitializingBean {

    private BufferedErrorReporter bufferedErrorReporter = new BufferedErrorReporter();


    public void report(String title, Throwable ex, String appId) {
        bufferedErrorReporter.report(title, ex, appId);
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        HttpPostErrorConsumer errorConsumer = new HttpPostErrorConsumer("http://alarm.ymatou.com/Alarm/SaveSingle");
        bufferedErrorReporter.setErrorConsumer(errorConsumer);
        bufferedErrorReporter.init();
    }

    /**
     * 发送异常报警
     * @param message
     * @param callbackConfig
     */
    public void sendErrorReport(CallbackMessage message, CallbackConfig callbackConfig){
        report(message.getResponse()
                + String.format(" [messageUUID: %s, appId: %s ,callbackKey: %s,url: %s]", message.getBizId(),
                        message.getAppId(), message.getCallbackKey(), callbackConfig.getUrl()),
                message.getEx(),
                callbackConfig.getCallbackAppId());
    }
}
