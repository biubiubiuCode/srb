package org.example.common.exception;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.common.result.ResponseEnum;

/**
 * 自定义运行时异常类
 * 在程序运行时抛出这个自定义异常对象，并在同意异常器类中捕获这个异常进行处理
 * 目的： 使用一个或较少异常类，可以捕获和显示所有的异常信息
 * @author wendao
 * @since 2024-04-02
 **/
@Data
@NoArgsConstructor
public class BusinessException extends RuntimeException{
    //状态码
    private Integer code;

    //错误消息
    private String message;


    /*各种构造函数*/
    /**
     *
     * @param message 错误消息
     */
    public BusinessException(String message) {
        this.message = message;
    }

    /**
     *
     * @param message 错误消息
     * @param code 错误码
     */
    public BusinessException(String message, Integer code) {
        this.message = message;
        this.code = code;
    }

    /**
     *
     * @param message 错误消息
     * @param code 错误码
     * @param cause 原始异常对象
     */
    public BusinessException(String message, Integer code, Throwable cause) {
        super(cause);
        this.message = message;
        this.code = code;
    }

    /**
     *
     * @param resultCodeEnum 接收枚举类型
     */
    public BusinessException(ResponseEnum resultCodeEnum) {
        this.message = resultCodeEnum.getMessage();
        this.code = resultCodeEnum.getCode();
    }

    /**
     *
     * @param resultCodeEnum 接收枚举类型
     * @param cause 原始异常对象
     */
    public BusinessException(ResponseEnum resultCodeEnum, Throwable cause) {
        super(cause);
        this.message = resultCodeEnum.getMessage();
        this.code = resultCodeEnum.getCode();
    }
}
