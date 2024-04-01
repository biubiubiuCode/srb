package org.example.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.example.common.result.R;
import org.example.common.result.ResponseEnum;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 统一异常处理器类
 * @author wendao
 * @since 2024-04-01
 **/
@Slf4j
@Component
@RestControllerAdvice //在controller层添加通知。
// 如果使用@ControllerAdvice，则方法上需要添加@ResponseBody
public class UnifiedExceptionHandler {
    /**
     * 未定义异常
     */
    @ExceptionHandler(value = Exception.class)//捕获所有的异常
    public R handleException(Exception e){
        log.error(e.getMessage(),e);
        return R.error();
    }

    /**
     * 特定异常 BadSqlGrammarException
     */
    @ExceptionHandler(BadSqlGrammarException.class)
    public R handleBadSqlGrammarException(BadSqlGrammarException e){
        log.error(e.getMessage(), e);
        return R.setResult(ResponseEnum.BAD_SQL_GRAMMAR_ERROR);
    }

}
