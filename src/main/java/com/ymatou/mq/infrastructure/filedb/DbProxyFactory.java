/*
 *
 * (C) Copyright 2017 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.mq.infrastructure.filedb;

import com.ymatou.mq.infrastructure.filedb.exodus.ExodusDb;

/**
 * db proxy factory
 * 
 * @author luoshiqian 2017/3/23 17:39
 */
public class DbProxyFactory {


    public static DbProxy build(FileDbConfig config) {
        return new ExodusDb(config);
    }

}
