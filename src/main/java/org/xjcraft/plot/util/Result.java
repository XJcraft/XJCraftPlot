package org.xjcraft.plot.util;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 公共返回值
 */
@Data
@Accessors(chain = true)
public class Result<T> {
    /**
     * 是否成功
     */
    private boolean success;
    /**
     * 错误信息(失败时有)
     */
    private String message;
    /**
     * 返回数据
     */
    private T data;

    public static <T> Result<T> success(T data) {
        var result = new Result<T>();
        result.setSuccess(true)
                .setMessage("")
                .setData(data);
        return result;
    }

    public static <T> Result<T> success() {
        return Result.success(null);
    }

    public static <T> Result<T> fail(String message) {
        var result = new Result<T>();
        result.setSuccess(false)
                .setMessage(message)
                .setData(null);
        return result;
    }

    public static <T> Result<T> fail() {
        return Result.fail("");
    }

    public static <T> Result<T> failf(String message, Object... params) {
        return Result.fail(String.format(message, params));
    }

    public static <T> Result<T> auto(boolean success, String failMessage) {
        return success ? Result.success() : Result.fail(failMessage);
    }

    public static <T> Result<T> auto(boolean success) {
        return Result.auto(success, "");
    }

    public static <T> Result<T> autof(boolean success, String failMessage, Object... params) {
        return success ? Result.success() : Result.failf(failMessage, params);
    }
}
