package org.example.srb.core.service;

import org.example.srb.core.pojo.entity.UserAccount;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.util.Map;

/**
 * <p>
 * 用户账户 服务类
 * </p>
 *
 * @author wendao
 * @since 2024-04-01
 */
public interface UserAccountService extends IService<UserAccount> {

    /**
     * 用户充值接口
     * @param chargeAmt 充值金额BigDecimal
     * @param userId 用户id
     * @return java.lang.String
     * @author Administrator
     * @date 2024/4/17 0017 22:43
    */
    String commitCharge(BigDecimal chargeAmt, Long userId);
    /**
     * 用户充值回调的成功的响应
     * @param paramMap
     * @return java.lang.String
     * @author Administrator
     * @date 2024/4/17 0017 23:14
    */
    String notify(Map<String, Object> paramMap);
    /**
     * 获取账户余额 amount
     * @param userId
     * @return java.math.BigDecimal
     * @author Administrator
     * @date 2024/4/18 0018 19:11
    */
    BigDecimal getAccount(Long userId);
    /**
     * 用户提现（需要跟第三方交互）
     * @param fetchAmt 提现金额
     * @param userId 提现用户的id
     * @return java.lang.String
     * @author Administrator
     * @date 2024/4/20 0020 0:04
    */
    String commitWithdraw(BigDecimal fetchAmt, Long userId);
    /**
     * 用户提现异步回调
     * @param paramMap
     * @return void
     * @author Administrator
     * @date 2024/4/20 0020 0:05       
    */
    void notifyWithdraw(Map<String, Object> paramMap);
}
