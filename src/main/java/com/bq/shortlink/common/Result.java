package com.bq.shortlink.common;

import lombok.Data;

/**
 * 统一响应体（复用秒杀项目，去掉与业务实体相关的多余依赖）。
 * <p>业务接口 HTTP 状态码统一为 200，具体结果看 code。</p>
 *
 * @param <T> 业务数据类型
 */
@Data
public class Result<T> {

    /** 业务状态码：200 成功 */
    private Integer code;

    /** 提示信息 */
    private String msg;

    /** 业务数据 */
    private T data;

    /**
     * 成功，无数据。
     */
    public static <T> Result<T> success() {
        return build(200, "成功", null);
    }

    /**
     * 成功，带数据。
     */
    public static <T> Result<T> success(T data) {
        return build(200, "成功", data);
    }

    /**
     * 失败，指定业务码与提示。
     */
    public static <T> Result<T> error(Integer code, String msg) {
        return build(code, msg, null);
    }

    /**
     * 构造响应对象。
     */
    private static <T> Result<T> build(Integer code, String msg, T data) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }
}
