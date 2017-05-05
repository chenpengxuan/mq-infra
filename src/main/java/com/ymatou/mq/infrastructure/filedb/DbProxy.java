/*
 *
 *  (C) Copyright 2017 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.mq.infrastructure.filedb;

import java.util.Map;

/**
 * @author luoshiqian 2017/3/23 16:13
 */
public interface DbProxy {

    /**
     * 存放数据
     * @param key
     * @param value
     */
    void put(String key,String value);

    /**
     * 同部保存数据
     * @param key
     * @param value
     * @return
     */
    boolean syncPut(String key,String value);

    /**
     * 关闭资源
     */
    void close();

    /**
     * 重新设置配置
     * @param newConfig
     */
    void reset(FileDbConfig newConfig);

    /**
     * 目前状态
     * @return
     */
    Map<String,Object> status();


}
