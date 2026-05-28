// 全局异常处理器，把 Sa-Token抛出的未登录异常捕获后，返回统一的Result格式

package com.family.account.common;

import cn.dev33.satoken.exception.NotLoginException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 处理未登录异常
    @ExceptionHandler(NotLoginException.class)
    public Result handleNotLoginException(NotLoginException e) {
        return Result.error(401, "未登录或token已失效");
    }

    // 处理其他未知异常
    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e) {
        return Result.error(500, e.getMessage());
    }
}