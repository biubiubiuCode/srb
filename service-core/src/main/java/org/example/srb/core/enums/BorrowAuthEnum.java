package org.example.srb.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
/**
 * @author wendao
 * @since 2024-04-12
 **/
@AllArgsConstructor
@Getter
public enum BorrowAuthEnum {

    NO_AUTH(0, "未认证"),
    AUTH_RUN(1, "认证中"),
    AUTH_OK(2, "认证成功"),
    AUTH_FAIL(-1, "认证失败"),
    ;

    private Integer status;
    private String msg;
}
