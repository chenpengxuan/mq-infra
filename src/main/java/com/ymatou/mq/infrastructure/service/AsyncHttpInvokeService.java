/*
 *
 *  (C) Copyright 2017 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.mq.infrastructure.service;

import java.io.IOException;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ymatou.mq.infrastructure.model.CallbackConfig;
import com.ymatou.mq.infrastructure.model.CallbackMessage;
import com.ymatou.mq.infrastructure.support.AdjustableSemaphore;
import com.ymatou.mq.infrastructure.support.SemaphorManager;
import com.ymatou.performancemonitorclient.PerformanceStatisticContainer;

/**
 * async http调用service
 * Created by zhangzhihua on 2017/4/1.
 */
public class AsyncHttpInvokeService implements FutureCallback<HttpResponse> {

    private static final Logger logger = LoggerFactory.getLogger(AsyncHttpInvokeService.class);

    /**
     * 每个回调key url的业务性能监控
     */
    public static final String MONITOR_CALLBACK_KEY_URL_APP_ID = "mqmonitor.callbackkeyurl.iapi.ymatou.com";

    public static final Integer CONN_TIME_OUT = 5000;
    public static final Integer SOCKET_TIME_OUT = 5000;
    public static final Integer CONN_REQ_TIME_OUT = 5000;

    private static RequestConfig DEFAULT_REQUEST_CONFIG = RequestConfig.custom()
            .setConnectTimeout(CONN_TIME_OUT)
            .setSocketTimeout(SOCKET_TIME_OUT)
            .setConnectionRequestTimeout(CONN_REQ_TIME_OUT)
            .build();

    private static  CloseableHttpAsyncClient httpAsyncClient;

    private HttpPost httpPost;

    /**
     * 信号量
     */
    private AdjustableSemaphore semaphore;

    private CallbackMessage message;

    private CallbackConfig callbackConfig;

    private HttpInvokeResultService httpInvokeResultService;

    private long startTime;

    static {
        if(httpAsyncClient == null){
            initAsyncHttpClient();
        }
    }

    public AsyncHttpInvokeService(CallbackMessage message, CallbackConfig callbackConfig, HttpInvokeResultService httpInvokeResultService){
        this.message = message;
        this.callbackConfig = callbackConfig;
        this.httpInvokeResultService = httpInvokeResultService;

        httpPost = new HttpPost(callbackConfig.getUrl());
        this.semaphore = SemaphorManager.get(callbackConfig.getCallbackKey());
        setContentType(callbackConfig.getContentType());
        setTimeout(callbackConfig.getTimeout());
    }

    /**
     * 初始化async http client
     */
    static void initAsyncHttpClient(){
        try {
            ConnectingIOReactor ioReactor = new DefaultConnectingIOReactor();
            PoolingNHttpClientConnectionManager cm = new PoolingNHttpClientConnectionManager(ioReactor);
            cm.setDefaultMaxPerRoute(20);
            cm.setMaxTotal(1000);

            RequestConfig defaultRequestConfig = RequestConfig.custom()
                    .setSocketTimeout(5000)
                    .setConnectTimeout(5000)
                    .setConnectionRequestTimeout(5000)
                    .build();

            httpAsyncClient = HttpAsyncClients.custom().setDefaultRequestConfig(defaultRequestConfig)
                    .setConnectionManager(cm).build();
            httpAsyncClient.start();
        } catch (IOReactorException e) {
            throw new RuntimeException("crate async http client error.",e);
        }
    }

    /**
     * 设置Content-Type
     *
     * @param contentType
     * @return
     */
    void setContentType(String contentType) {
        if (StringUtils.isEmpty(contentType)) {
            httpPost.setHeader("Content-Type", "application/json;charset=utf-8");
        } else {
            httpPost.setHeader("Content-Type", String.format("%s;charset=utf-8", contentType));
        }
    }

    /**
     * 设置超时
     *
     * @param timeout
     * @return
     */
    void setTimeout(int timeout) {
        RequestConfig requestConfig = RequestConfig.copy(DEFAULT_REQUEST_CONFIG)
                // .setConnectionRequestTimeout(timeout)
                .setSocketTimeout(timeout)
                .build();
        httpPost.setConfig(requestConfig);
    }


    /**
     * async send
     */
    public void send() throws InterruptedException{
        send(null);
    }

    /**
     * async send
     * @param timeout
     */
    public void send(Long timeout) throws InterruptedException{
        if (semaphore != null) {
            if (timeout != null) {
                semaphore.tryAcquire(timeout);
            } else {
                semaphore.acquire();
            }
        }

        StringEntity postEntity = new StringEntity(message.getBody(), "UTF-8");
        httpPost.setEntity(postEntity);

        message.setRequestTime(new Date());

        startTime = System.currentTimeMillis();
        httpAsyncClient.execute(httpPost,this);
    }

    @Override
    public void completed(HttpResponse result) {
        clear();

        message.setResponseTime(new Date());
        try {
            HttpEntity entity = result.getEntity();
            int statusCode = result.getStatusLine().getStatusCode();

            // 上报分发回调调用记录
            long consumedTime = System.currentTimeMillis() - startTime;
            PerformanceStatisticContainer.add(consumedTime, String.format("%s_%s", callbackConfig.getCallbackKey(),callbackConfig.getUrl()),
                    MONITOR_CALLBACK_KEY_URL_APP_ID);

            String body = EntityUtils.toString(entity, "UTF-8");

            wrapperResponse(message,statusCode,body,null);
            if (isCallbackSuccess(statusCode,body)) {
                httpInvokeResultService.onInvokeSuccess(message, callbackConfig);
            }else {
                httpInvokeResultService.onInvokeFail(message, callbackConfig);
            }
        } catch (IOException e) {
            message.setResponse(e.toString() + "|" + result.toString());
            wrapperResponse(message,null,result.toString(),e);
            httpInvokeResultService.onInvokeFail(message, callbackConfig);
        }
    }

    private void wrapperResponse(CallbackMessage message,Integer statusCode,String body,Exception ex){
        message.setResponseTime( new Date());
        long duration = System.currentTimeMillis() - startTime;

        StringBuffer sb = new StringBuffer();
        if(statusCode != null){
            sb.append(statusCode +", ");
        }

        if(body != null){
            sb.append(body +", ");
        }

        sb.append(duration + "ms, ");

        message.setEx(ex);
        if(ex != null){
            sb.append(ex.toString());
        }

        message.setResponse(sb.toString());
    }

    @Override
    public void cancelled() {
        clear();

        // 上报分发回调调用记录
        long consumedTime = System.currentTimeMillis() - startTime;
        PerformanceStatisticContainer.add(consumedTime, String.format("%s_%s", callbackConfig.getCallbackKey(),callbackConfig.getUrl()),
                MONITOR_CALLBACK_KEY_URL_APP_ID);

        wrapperResponse(message,null,"request cancelled",null);
        httpInvokeResultService.onInvokeFail(message,callbackConfig);
    }

    @Override
    public void failed(Exception ex) {
        clear();

        // 上报分发回调调用记录
        long consumedTime = System.currentTimeMillis() - startTime;
        PerformanceStatisticContainer.add(consumedTime, String.format("%s_%s", callbackConfig.getCallbackKey(),callbackConfig.getUrl()),
                MONITOR_CALLBACK_KEY_URL_APP_ID);

        wrapperResponse(message,null,null,ex);
        httpInvokeResultService.onInvokeFail(message,callbackConfig);
    }


    public void clear(){
        try {
            if (semaphore != null) {
                semaphore.release();
            }
            httpPost.releaseConnection();
        } catch (Exception e) {
            logger.error("clear asyncHttpInvoke resource error", e);
        }
    }

    /**
     * 根据返回代码判断是否成功
     * @param statusCode
     * @param body
     * @return
     */
    private boolean isCallbackSuccess(int statusCode, String body) {
        if (statusCode == 200 && body != null
                && (body.trim().equalsIgnoreCase("ok") || body.trim().equalsIgnoreCase("\"ok\""))) {
            return true;
        } else {
            return false;
        }
    }

}
