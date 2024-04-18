package org.example.srb.core.pojo.bo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.srb.core.enums.TransTypeEnum;

import java.math.BigDecimal;

/**
 * businissObject
 * @author wendao
 * @since 2024-04-18
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description="交易流水")
public class TransFlowBO {
    @ApiModelProperty(value = "商户充值订单号")
    private String agentBillNo;
    @ApiModelProperty(value = "充值人绑定协议号")
    private String bindCode;
    @ApiModelProperty(value = "充值金额")
    private BigDecimal amount;
    @ApiModelProperty(value = "流水操作类型")
    private TransTypeEnum transTypeEnum;
    @ApiModelProperty(value = "描述(标的描述)")
    private String memo;
}
