/*
 *
 * (C) Copyright 2017 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.mq.infrastructure.util;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import com.google.common.base.Splitter;
import com.ymatou.mq.infrastructure.model.RetryPolicyEnums;

/**
 * @author luoshiqian 2017/4/13 17:54
 */
public class RetryPolicyUtils {

    public static final String SPLITER = "-";

    /**
     * // 1m-5m-10m-20m-30m-em
     * 
     * @param retryPolicy
     * @param retryNums
     * @return
     */
    public static Date getNextTime(String retryPolicy, int retryNums) {

        List<String> retryPolicyList = Splitter.on(SPLITER).splitToList(retryPolicy);

        String policy;
        if (retryNums > retryPolicyList.size()) {
            policy = retryPolicyList.get(retryPolicyList.size() - 1).trim();

            if(!StringUtils.containsIgnoreCase(policy,RetryPolicyEnums.E.name())){
                //超过重试配置次数，如果没有包含every，不再重试，认为失败
                return null;
            }
        } else {
            policy = retryPolicyList.get(retryNums - 1).trim();
        }
        Date nextTime = new Date();

        String end = String.valueOf(policy.charAt(policy.length() - 1)).toUpperCase();
        String delay = StringUtils.removeEndIgnoreCase(policy, end);

        RetryPolicyEnums retryPolicyEnums = RetryPolicyEnums.valueOf(end);

        // 如果使用了 E 则是就是加1
        int plus = 1;
        if (!delay.equalsIgnoreCase(RetryPolicyEnums.E.name())) {
            plus = Integer.valueOf(delay);
        }

        switch (retryPolicyEnums) {
            case S:
                nextTime = DateTime.now().plusSeconds(plus).toDate();
                break;
            case M:
                nextTime = DateTime.now().plusMinutes(plus).toDate();
                break;
            case H:
                nextTime = DateTime.now().plusHours(plus).toDate();
                break;
            case D:
                nextTime = DateTime.now().plusDays(plus).toDate();
                break;
            default:
                nextTime = null;
        }

        return nextTime;
    }
}
