package org.example.srb.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author wendao
 * @since 2024-04-12
 **/
@AllArgsConstructor
@Getter
public enum ReturnMethodEnum {

    ONE(1, "等额本息"),
    TWO(2, "等额本金"),
    THREE(3, "每月还息一次还本"),
    FOUR(4, "一次还本还息"),
    ;

    private Integer method;
    private String msg;
}
