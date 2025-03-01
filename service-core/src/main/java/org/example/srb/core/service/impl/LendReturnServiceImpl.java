package org.example.srb.core.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.example.common.exception.Assert;
import org.example.common.result.ResponseEnum;
import org.example.srb.core.enums.LendStatusEnum;
import org.example.srb.core.enums.TransTypeEnum;
import org.example.srb.core.hfb.FormHelper;
import org.example.srb.core.hfb.HfbConst;
import org.example.srb.core.hfb.RequestHelper;
import org.example.srb.core.mapper.LendItemMapper;
import org.example.srb.core.mapper.LendItemReturnMapper;
import org.example.srb.core.mapper.LendMapper;
import org.example.srb.core.mapper.LendReturnMapper;
import org.example.srb.core.mapper.UserAccountMapper;
import org.example.srb.core.pojo.bo.TransFlowBO;
import org.example.srb.core.pojo.entity.Lend;
import org.example.srb.core.pojo.entity.LendItem;
import org.example.srb.core.pojo.entity.LendItemReturn;
import org.example.srb.core.pojo.entity.LendReturn;
import org.example.srb.core.service.LendItemReturnService;
import org.example.srb.core.service.LendReturnService;
import org.example.srb.core.service.TransFlowService;
import org.example.srb.core.service.UserAccountService;
import org.example.srb.core.service.UserBindService;
import org.example.srb.core.util.LendNoUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 还款记录表 服务实现类
 * </p>
 *
 * @author wendao
 * @since 2024-04-01
 */
@Slf4j
@Service
public class LendReturnServiceImpl extends ServiceImpl<LendReturnMapper, LendReturn> implements LendReturnService {

    @Override
    public List<LendReturn> selectByLendId(Long lendId) {
        QueryWrapper<LendReturn> queryWrapper = new QueryWrapper();
        queryWrapper.eq("lend_id", lendId);
        List<LendReturn> lendReturnList = baseMapper.selectList(queryWrapper);
        return lendReturnList;
    }
    @Resource
    private UserAccountService userAccountService;

    @Resource
    private LendMapper lendMapper;

    @Resource
    private UserBindService userBindService;

    @Resource
    private LendItemReturnService lendItemReturnService;
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String commitReturn(Long lendReturnId, Long userId) {
        //获取还款记录
        LendReturn lendReturn = baseMapper.selectById(lendReturnId);

        //判断账号余额是否充足
        BigDecimal amount = userAccountService.getAccount(userId);
        Assert.isTrue(amount.doubleValue() >= lendReturn.getTotal().doubleValue(),
                ResponseEnum.NOT_SUFFICIENT_FUNDS_ERROR);

        //获取借款人code
        String bindCode = userBindService.getBindCodeByUserId(userId);
        //获取lend
        Long lendId = lendReturn.getLendId();
        Lend lend = lendMapper.selectById(lendId);

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("agentId", HfbConst.AGENT_ID);
        //商户商品名称
        paramMap.put("agentGoodsName", lend.getTitle());
        //批次号
        paramMap.put("agentBatchNo",lendReturn.getReturnNo());
        //还款人绑定协议号
        paramMap.put("fromBindCode", bindCode);
        //还款总额
        paramMap.put("totalAmt", lendReturn.getTotal());
        paramMap.put("note", "");
        //还款明细
        List<Map<String, Object>> lendItemReturnDetailList = lendItemReturnService.addReturnDetail(lendReturnId);
        paramMap.put("data", JSONObject.toJSONString(lendItemReturnDetailList));
        paramMap.put("voteFeeAmt", new BigDecimal(0));
        paramMap.put("notifyUrl", HfbConst.BORROW_RETURN_NOTIFY_URL);
        paramMap.put("returnUrl", HfbConst.BORROW_RETURN_RETURN_URL);
        paramMap.put("timestamp", RequestHelper.getTimestamp());
        String sign = RequestHelper.getSign(paramMap);
        paramMap.put("sign", sign);

        //构建自动提交表单
        String formStr = FormHelper.buildForm(HfbConst.BORROW_RETURN_URL, paramMap);
        return formStr;
    }
    @Resource
    private TransFlowService transFlowService;

    @Resource
    private UserAccountMapper userAccountMapper;

    @Resource
    private LendItemReturnMapper lendItemReturnMapper;

    @Resource
    private LendItemMapper lendItemMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void notify(Map<String, Object> paramMap) {
        //还款编号
        String agentBatchNo = (String)paramMap.get("agentBatchNo");

        boolean result = transFlowService.isSaveTransFlow(agentBatchNo);
        if(result){
            log.warn("幂等性返回");
            return;
        }

        //获取还款数据
        String voteFeeAmt = (String)paramMap.get("voteFeeAmt");
        QueryWrapper lendReturnQueryWrapper = new QueryWrapper<LendReturn>();
        lendReturnQueryWrapper.eq("return_no", agentBatchNo);
        LendReturn lendReturn = baseMapper.selectOne(lendReturnQueryWrapper);;

        //更新还款状态
        lendReturn.setStatus(1);
        lendReturn.setFee(new BigDecimal(voteFeeAmt));
        lendReturn.setRealReturnTime(LocalDateTime.now());
        baseMapper.updateById(lendReturn);

        //更新标的信息
        Lend lend = lendMapper.selectById(lendReturn.getLendId());
        //最后一次还款更新标的状态
        if(lendReturn.getLast()) {
            lend.setStatus(LendStatusEnum.PAY_OK.getStatus());
            lendMapper.updateById(lend);
        }

        //借款账号转出金额
        BigDecimal totalAmt = new BigDecimal((String)paramMap.get("totalAmt"));//还款金额
        String bindCode = userBindService.getBindCodeByUserId(lend.getUserId());
        userAccountMapper.updateAccount(bindCode, totalAmt.negate(), new BigDecimal(0));

        //借款人交易流水
        TransFlowBO transFlowBO = new TransFlowBO(
                agentBatchNo,
                bindCode,
                totalAmt,
                TransTypeEnum.RETURN_DOWN,
                "借款人还款扣减，项目编号：" + lend.getLendNo() + "，项目名称：" + lend.getTitle());
        transFlowService.saveTransFlow(transFlowBO);

        //获取回款明细
        List<LendItemReturn> lendItemReturnList = lendItemReturnService.selectLendItemReturnList(lendReturn.getId());
        lendItemReturnList.forEach(item -> {
            //更新回款状态
            item.setStatus(1);
            item.setRealReturnTime(LocalDateTime.now());
            lendItemReturnMapper.updateById(item);

            //更新出借信息
            LendItem lendItem = lendItemMapper.selectById(item.getLendItemId());
            lendItem.setRealAmount(item.getInterest());
            lendItemMapper.updateById(lendItem);

            //投资账号转入金额
            String investBindCode = userBindService.getBindCodeByUserId(item.getInvestUserId());
            userAccountMapper.updateAccount(investBindCode, item.getTotal(), new BigDecimal(0));

            //投资账号交易流水
            TransFlowBO investTransFlowBO = new TransFlowBO(
                    LendNoUtils.getReturnItemNo(),
                    investBindCode,
                    item.getTotal(),
                    TransTypeEnum.INVEST_BACK,
                    "还款到账，项目编号：" + lend.getLendNo() + "，项目名称：" + lend.getTitle());
            transFlowService.saveTransFlow(investTransFlowBO);
        });
        log.info("还款成功,还款编号:"+agentBatchNo);
    }
}
