package org.example.srb.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author wendao
 * @since 2024-04-12
 **/
@AllArgsConstructor
@Getter
public enum UserBindEnum {

    NO_BIND(0, "未绑定"),
    BIND_OK(1, "绑定成功"),
    BIND_FAIL(-1, "绑定失败"),
    ;

    private Integer status;
    private String msg;
}
