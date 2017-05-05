/*
 *
 * (C) Copyright 2017 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.mq.infrastructure.config;

import org.springframework.stereotype.Component;

import com.baidu.disconf.client.common.annotations.DisconfFile;
import com.baidu.disconf.client.common.annotations.DisconfFileItem;

/**
 * @author luoshiqian 2017/3/27 16:40
 */
@Component
@DisconfFile(fileName = "mongodb.properties")
public class MongoConfig {

    /**
     * 配置库
     */
    private String configDbUri;

    /**
     * 消息库
     */
    private String messageDbUri;

    /**
     * 补单库
     */
    private String compensateDbUri;


    @DisconfFileItem(name = "mongo.config.uri")
    public String getConfigDbUri() {
        return configDbUri;
    }

    public void setConfigDbUri(String configDbUri) {
        this.configDbUri = configDbUri;
    }

    @DisconfFileItem(name = "mongo.message.uri")
    public String getMessageDbUri() {
        return messageDbUri;
    }

    public void setMessageDbUri(String messageDbUri) {
        this.messageDbUri = messageDbUri;
    }

    @DisconfFileItem(name = "mongo.compensate.uri")
    public String getCompensateDbUri() {
        return compensateDbUri;
    }

    public void setCompensateDbUri(String compensateDbUri) {
        this.compensateDbUri = compensateDbUri;
    }
}
