package org.example.srb.core.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 投标时要在服务器端校验数据：
 * - 标的状态必须为募资中
 * - 标的不能超卖
 * - 账户可用余额充足
 *
 * @author wendao
 * @since 2024-04-18
 **/
@Data
@ApiModel(description = "投标信息(标的投资用户)")
public class InvestVO {
    @ApiModelProperty(value = "标的id")
    private Long lendId;

    @ApiModelProperty(value = "投资用户的投资金额（投标金额）")
    private String investAmount;

    @ApiModelProperty(value = "投资用户的id")
    private Long investUserId;

    @ApiModelProperty(value = "投资用户的姓名")
    private String investName;
}
