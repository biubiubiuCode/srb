package org.example.srb.core.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.example.common.exception.Assert;
import org.example.common.result.ResponseEnum;
import org.example.srb.core.enums.TransTypeEnum;
import org.example.srb.core.hfb.FormHelper;
import org.example.srb.core.hfb.HfbConst;
import org.example.srb.core.hfb.RequestHelper;
import org.example.srb.core.mapper.UserAccountMapper;
import org.example.srb.core.mapper.UserInfoMapper;
import org.example.srb.core.pojo.bo.TransFlowBO;
import org.example.srb.core.pojo.entity.UserAccount;
import org.example.srb.core.pojo.entity.UserInfo;
import org.example.srb.core.service.TransFlowService;
import org.example.srb.core.service.UserAccountService;
import org.example.srb.core.service.UserBindService;
import org.example.srb.core.util.LendNoUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 用户账户 服务实现类
 * </p>
 *
 * @author wendao
 * @since 2024-04-01
 */
@Slf4j
@Service
public class UserAccountServiceImpl extends ServiceImpl<UserAccountMapper, UserAccount> implements UserAccountService {

    @Resource
    private UserInfoMapper userInfoMapper;
    @Override
    public String commitCharge(BigDecimal chargeAmt, Long userId) {
        UserInfo userInfo = userInfoMapper.selectById(userId);
        String bindCode = userInfo.getBindCode();
        //判断账户绑定状态
            //USER_NO_BIND_ERROR(302, "用户未绑定"),
        Assert.notEmpty(bindCode, ResponseEnum.USER_NO_BIND_ERROR);
        //创建表单的Map对象，用于传给第三方平台
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("agentId", HfbConst.AGENT_ID);
        paramMap.put("agentBillNo", LendNoUtils.getNo());
        paramMap.put("bindCode", bindCode);
        paramMap.put("chargeAmt", chargeAmt);
        paramMap.put("feeAmt", new BigDecimal("0"));
        paramMap.put("notifyUrl", HfbConst.RECHARGE_NOTIFY_URL);//检查常量是否正确
        paramMap.put("returnUrl", HfbConst.RECHARGE_RETURN_URL);
        paramMap.put("timestamp", RequestHelper.getTimestamp());
        String sign = RequestHelper.getSign(paramMap);
        paramMap.put("sign", sign);

        //把Map构建成充值提交的表单
        String formStr = FormHelper.buildForm(HfbConst.RECHARGE_URL, paramMap);
        return formStr;
    }

    @Resource
    private TransFlowService transFlowService;
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String notify(Map<String, Object> paramMap) {
        //幂等性判断(根据流水的商户订单号判断，可以利用数据库键的唯一性来实现按)
        String agentBillNo = (String)paramMap.get("agentBillNo"); //商户充值订单号
            //判断交易流水表trans_flow中交易流水是否存在
        boolean isSave = transFlowService.isSaveTransFlow(agentBillNo);
        if(isSave){
            log.warn("充值幂等性返回");
            return "success";
        }
        //账户处理
        String bindCode = (String)paramMap.get("bindCode"); //充值人绑定协议号
        String chargeAmt = (String)paramMap.get("chargeAmt"); //充值金额

        //修改商户系统中的用户账户金额：余额、冻结金额
        //优化，自定义Mapper方法：第二个参数修改余额，第三个参数修改冻结金额
        try {
            //更新用户账户user_account表中的金额
            baseMapper.updateAccount(bindCode, new BigDecimal(chargeAmt), new BigDecimal(0));
        } catch (Exception e) {
            log.info("充值失败，sql服务器故障：" + JSONObject.toJSONString(paramMap));
        }
        log.info("充值成功：" + JSONObject.toJSONString(paramMap));
        //记录交易流水到交易流水表中
        TransFlowBO transFlowBO = new TransFlowBO(
                agentBillNo,
                bindCode,
                new BigDecimal(chargeAmt),
                TransTypeEnum.RECHARGE,
                "充值");
        transFlowService.saveTransFlow(transFlowBO);

        return "success";
    }

    @Override
    public BigDecimal getAccount(Long userId) {
        //根据userId查找用户账户
        QueryWrapper<UserAccount> userAccountQueryWrapper = new QueryWrapper<>();
        userAccountQueryWrapper.eq("user_id", userId);
        UserAccount userAccount = baseMapper.selectOne(userAccountQueryWrapper);

        BigDecimal amount = userAccount.getAmount();
        return amount;
    }


    @Resource
    private UserBindService userBindService;

    @Resource
    private UserAccountService userAccountService;

    @Override
    public String commitWithdraw(BigDecimal fetchAmt, Long userId) {
        //账户可用余额充足：当前用户的余额 >= 当前用户的提现金额
        BigDecimal amount = userAccountService.getAccount(userId);//获取当前用户的账户余额
        Assert.isTrue(amount.doubleValue() >= fetchAmt.doubleValue(),
                ResponseEnum.NOT_SUFFICIENT_FUNDS_ERROR);


        String bindCode = userBindService.getBindCodeByUserId(userId);

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("agentId", HfbConst.AGENT_ID);
        paramMap.put("agentBillNo", LendNoUtils.getWithdrawNo());
        paramMap.put("bindCode", bindCode);
        paramMap.put("fetchAmt", fetchAmt);
        paramMap.put("feeAmt", new BigDecimal(0));
        paramMap.put("notifyUrl", HfbConst.WITHDRAW_NOTIFY_URL);
        paramMap.put("returnUrl", HfbConst.WITHDRAW_RETURN_URL);
        paramMap.put("timestamp", RequestHelper.getTimestamp());
        String sign = RequestHelper.getSign(paramMap);
        paramMap.put("sign", sign);

        //构建自动提交表单
        String formStr = FormHelper.buildForm(HfbConst.WITHDRAW_URL, paramMap);
        return formStr;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void notifyWithdraw(Map<String, Object> paramMap) {
        String agentBillNo = (String)paramMap.get("agentBillNo");
        boolean result = transFlowService.isSaveTransFlow(agentBillNo);
        if(result){
            log.warn("幂等性返回");
            return;
        }

        String bindCode = (String)paramMap.get("bindCode");
        String fetchAmt = (String)paramMap.get("fetchAmt");

        //根据用户账户修改账户金额
        baseMapper.updateAccount(bindCode, new BigDecimal("-" + fetchAmt), new BigDecimal(0));

        //增加交易流水
        TransFlowBO transFlowBO = new TransFlowBO(
                agentBillNo,//提现商单号
                bindCode,//绑定协议号
                new BigDecimal(fetchAmt),//提现金额
                TransTypeEnum.WITHDRAW,
                "提现");
        transFlowService.saveTransFlow(transFlowBO);
        log.info(bindCode+"提现成功"+"获得金额"+fetchAmt);
    }
}
