package org.example.srb.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.example.srb.core.pojo.entity.UserAccount;

import java.math.BigDecimal;

/**
 * <p>
 * 用户账户 Mapper 接口
 * </p>
 *
 * @author wendao
 * @since 2024-04-01
 */
public interface UserAccountMapper extends BaseMapper<UserAccount> {
    /**
     *
     * @param bindCode 充值人绑定协议号
     * @param bigDecimal 修改的余额
     * @param bigDecimal1 修改冻结金额
     * @return void
     * @author Administrator
     * @date 2024/4/18 0018 2:05
    */
    void updateAccount(@Param("bindCode") String bindCode,
                       @Param("amount") BigDecimal amount,
                       @Param("freezeAmount") BigDecimal freezeAmount);
}
