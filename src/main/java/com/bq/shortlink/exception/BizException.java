package com.bq.shortlink.exception;

import lombok.Getter;

/**
 * 业务异常（复用秒杀项目）。
 * <p>由 GlobalExceptionHandler 统一捕获并转成 Result。</p>
 */
@Getter
public class BizException extends RuntimeException {

    /** 业务状态码 */
    private final Integer code;

    /**
     * 构造业务异常。
     *
     * @param code 业务状态码
     * @param msg  错误提示
     */
    public BizException(Integer code, String msg) {
        super(msg);
        this.code = code;
    }
}
