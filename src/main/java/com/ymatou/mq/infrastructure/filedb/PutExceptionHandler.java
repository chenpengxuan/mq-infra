/*
 *
 *  (C) Copyright 2017 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.mq.infrastructure.filedb;

import java.util.Optional;

/**
 * 放入异常处理器
 * @author luoshiqian 2017/3/23 16:23
 */
public interface PutExceptionHandler {

    void handleException(String key, String value, Optional<Throwable> throwable);

}
