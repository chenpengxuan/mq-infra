/*
 *
 * (C) Copyright 2017 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.mq.infrastructure.service;

import com.ymatou.mq.infrastructure.model.CallbackConfig;
import com.ymatou.mq.infrastructure.model.CallbackMessage;

/**
 * 远程调用结果 只有当返回码200 并且返回ok时 才算成功
 * 
 * @author luoshiqian 2017/4/13 10:40
 */
public interface HttpInvokeResultService {

    /**
     * 远程处理成功
     * @param callbackMessage
     * @param callbackConfig
     */
    void onInvokeSuccess(CallbackMessage callbackMessage, CallbackConfig callbackConfig);


    /**
     * 远程处理失败或请求不通等
     * @param callbackMessage
     * @param callbackConfig
     */
    void onInvokeFail(CallbackMessage callbackMessage, CallbackConfig callbackConfig);

}
