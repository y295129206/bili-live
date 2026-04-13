package com.bililive.advice;

import com.bililive.pojo.BaseException;
import com.bililive.pojo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

//全局异常处理器
//负责捕获所有未被处理的异常，并统一转换为 Result 格式返回
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(BaseException.class)
    public Result<?> handleBaseException(BaseException e) {
        // 记录日志 (生产环境建议记录 warn 级别)
        log.warn("业务异常捕获 -> Code: {}, Msg: {}", e.getCode(), e.getMsg());

        // 直接利用异常中的 code 和 msg 构建 Result
        // 前端拦截器会根据 e.getCode() (如 901) 执行跳转逻辑
        return Result.error(e.getCode(), e.getMsg());
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        // 记录错误日志 (error 级别，包含堆栈)
        log.error("系统未知异常", e);

        // 返回通用的服务器错误，code 设为 0 或 500，视前端定义而定
        // 这里返回 0，对应前端拦截器的 else 分支 (弹出错误提示)
        return Result.error("系统繁忙，请稍后再试");
    }

}