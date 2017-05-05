package com.ymatou.mq.infrastructure.support.enums;

/**
 * 补单记录来源
 * @author zhangyifan 2016/9/1 12:00
 */
public enum CompensateFromEnum {

    /**
     * 分发站
     */
    DISPATCH(1),


    /**
     * 补单站
     */
    COMPENSATE(2),;

    private int code;

    CompensateFromEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}

