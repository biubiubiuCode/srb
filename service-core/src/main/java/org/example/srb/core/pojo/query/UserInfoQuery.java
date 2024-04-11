package org.example.srb.core.pojo.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author wendao
 * @since 2024-04-09
 **/
@Data
@ApiModel(description="会员搜索对象")
public class UserInfoQuery {
    @ApiModelProperty(value = "手机号")
    private String mobile;

    @ApiModelProperty(value = "状态")
    private Integer status;

    @ApiModelProperty(value = "1：出借人 2：借款人")
    private Integer userType;
}
