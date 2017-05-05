package com.ymatou.mq.infrastructure.support.enums;

/**
 * 回调来源
 * @author zhangyifan 2016/9/1 12:00
 */
public enum CallbackFromEnum {


    /**
     * 初始化
     */
    INIT(0),

    /**
     * 分发站
     */
    DISPATCH(1),


    /**
     * 补单站
     */
    COMPENSATE(2),;

    private int code;

    CallbackFromEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}

