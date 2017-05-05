package com.ymatou.mq.infrastructure.support.enums;

/**
 * 分发状态
 * @author zhangyifan 2016/9/1 12:00
 */
public enum DispatchStatusEnum {


    /**
     * 初始化
     */
    INIT(0),

    /**
     * 成功
     */
    SUCCESS(1),


    /**
     * 失败
     */
    FAIL(2),

    /**
     * 补单中
     */
    COMPENSATE(3),;

    private int code;

    DispatchStatusEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}

