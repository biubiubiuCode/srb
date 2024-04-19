package org.example.srb.core.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.example.common.exception.Assert;
import org.example.common.exception.BusinessException;
import org.example.common.result.ResponseEnum;
import org.example.srb.core.enums.LendStatusEnum;
import org.example.srb.core.enums.ReturnMethodEnum;
import org.example.srb.core.enums.TransTypeEnum;
import org.example.srb.core.hfb.HfbConst;
import org.example.srb.core.hfb.RequestHelper;
import org.example.srb.core.mapper.BorrowerMapper;
import org.example.srb.core.mapper.LendItemMapper;
import org.example.srb.core.mapper.LendMapper;
import org.example.srb.core.mapper.UserAccountMapper;
import org.example.srb.core.mapper.UserInfoMapper;
import org.example.srb.core.pojo.bo.TransFlowBO;
import org.example.srb.core.pojo.entity.BorrowInfo;
import org.example.srb.core.pojo.entity.Borrower;
import org.example.srb.core.pojo.entity.Lend;
import org.example.srb.core.pojo.entity.LendItem;
import org.example.srb.core.pojo.entity.LendItemReturn;
import org.example.srb.core.pojo.entity.LendReturn;
import org.example.srb.core.pojo.entity.UserInfo;
import org.example.srb.core.pojo.vo.BorrowInfoApprovalVO;
import org.example.srb.core.pojo.vo.BorrowerDetailVO;
import org.example.srb.core.service.BorrowerService;
import org.example.srb.core.service.DictService;
import org.example.srb.core.service.LendItemReturnService;
import org.example.srb.core.service.LendItemService;
import org.example.srb.core.service.LendReturnService;
import org.example.srb.core.service.LendService;
import org.example.srb.core.service.TransFlowService;
import org.example.srb.core.util.Amount1Helper;
import org.example.srb.core.util.Amount2Helper;
import org.example.srb.core.util.Amount3Helper;
import org.example.srb.core.util.Amount4Helper;
import org.example.srb.core.util.LendNoUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 标的准备表 服务实现类
 * </p>
 *
 * @author wendao
 * @since 2024-04-01
 */
@Slf4j
@Service
public class LendServiceImpl extends ServiceImpl<LendMapper, Lend> implements LendService {

    @Override
    public void createLend(BorrowInfoApprovalVO borrowInfoApprovalVO, BorrowInfo borrowInfo) {
        Lend lend = new Lend();
        lend.setUserId(borrowInfo.getUserId());//用户id
        lend.setBorrowInfoId(borrowInfo.getId());//借款id
        lend.setLendNo(LendNoUtils.getLendNo());//生成借款编号
        lend.setTitle(borrowInfoApprovalVO.getTitle());//标题(标的描述)
        lend.setAmount(borrowInfo.getAmount());//借款金额
        lend.setPeriod(borrowInfo.getPeriod());//借款期限
        //注意从审批对像对象获取而并不是借款对象获取利率，因为人工审核可能会改
        lend.setLendYearRate(borrowInfoApprovalVO.getLendYearRate().divide(new BigDecimal(100)));//从审批对象中获取年化利率
        lend.setServiceRate(borrowInfoApprovalVO.getServiceRate().divide(new BigDecimal(100)));//从审批对象中获取平台服务费率
        lend.setReturnMethod(borrowInfo.getReturnMethod());//还款方式
        lend.setLowestAmount(new BigDecimal(100));//最低投资金额
        lend.setInvestAmount(new BigDecimal(0));//已投金额
        lend.setInvestNum(0);//投资人数
        lend.setPublishDate(LocalDateTime.now());

        //起息日期
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate lendStartDate = LocalDate.parse(borrowInfoApprovalVO.getLendStartDate(), dtf);
        lend.setLendStartDate(lendStartDate);
        //结束日期
        LocalDate lendEndDate = lendStartDate.plusMonths(borrowInfo.getPeriod());
        lend.setLendEndDate(lendEndDate);

        lend.setLendInfo(borrowInfoApprovalVO.getLendInfo());//描述

        //平台预期收益
        //      月年化 = 年化 / 12
        //scale小数位精度，经验值8; BigDecimal.ROUND_DOWN向下取整
        BigDecimal monthRate = lend.getServiceRate().divide(new BigDecimal(12), 8, BigDecimal.ROUND_DOWN);
        //      平台收益 = 标的金额 * 月年化 * 期数
        BigDecimal expectAmount = lend.getAmount().multiply(monthRate).multiply(new BigDecimal(lend.getPeriod()));
        lend.setExpectAmount(expectAmount);

        //实际收益
        lend.setRealAmount(new BigDecimal(0));
        //状态
        lend.setStatus(LendStatusEnum.INVEST_RUN.getStatus());
        //审核时间
        lend.setCheckTime(LocalDateTime.now());

        /*
        * 审核人
        *      目前是固定值，需要优化！！
        * */
        lend.setCheckAdminId(1L);

        baseMapper.insert(lend);
    }

    @Resource
    private DictService dictService;
    @Override
    public List<Lend> selectList() {
        List<Lend> lendList = baseMapper.selectList(null);
        lendList.forEach(lend -> {
            //把值转换成描述，存到其他参数Param字段的Map中
            String returnMethod = dictService.getNameByParentDictCodeAndValue("returnMethod", lend.getReturnMethod());
            String status = LendStatusEnum.getMsgByStatus(lend.getStatus());
            lend.getParam().put("returnMethod", returnMethod);
            lend.getParam().put("status", status);
        });
        return lendList;
    }

    @Resource
    private BorrowerMapper borrowerMapper;

    @Resource
    private BorrowerService borrowerService;
    @Override
    public Map<String, Object> getLendDetail(Long id) {
        //查询标的对象
        Lend lend = baseMapper.selectById(id);
        //组装数据
        String returnMethod = dictService.getNameByParentDictCodeAndValue("returnMethod", lend.getReturnMethod());
        String status = LendStatusEnum.getMsgByStatus(lend.getStatus());
        lend.getParam().put("returnMethod", returnMethod);
        lend.getParam().put("status", status);

        //根据user_id获取借款人对象
        QueryWrapper<Borrower> borrowerQueryWrapper = new QueryWrapper<Borrower>();
        borrowerQueryWrapper.eq("user_id", lend.getUserId());
        Borrower borrower = borrowerMapper.selectOne(borrowerQueryWrapper);
        //组装借款人对象
//        BorrowerDetailVO borrowerDetailVO = borrowerService.getBorrowerDetailVOById(borrower.getId());
        BorrowerDetailVO borrowerDetailVO = borrowerService.getBorrowerDetailVOById(borrower);

        //组装数据
        Map<String, Object> result = new HashMap<>();
        result.put("lend", lend);
        result.put("borrower", borrowerDetailVO);

        return result;
    }

    @Override
    public BigDecimal getInterestCount(BigDecimal invest, BigDecimal yearRate, Integer totalmonth, Integer returnMethod) {
        BigDecimal interestCount;
        //计算总利息
        if (returnMethod.intValue() == ReturnMethodEnum.ONE.getMethod()) {
            //等额本息
            interestCount = Amount1Helper.getInterestCount(invest, yearRate, totalmonth);
        } else if (returnMethod.intValue() == ReturnMethodEnum.TWO.getMethod()) {
            //等额本金
            interestCount = Amount2Helper.getInterestCount(invest, yearRate, totalmonth);
        } else if(returnMethod.intValue() == ReturnMethodEnum.THREE.getMethod()) {
            //按期付息到期还本
            interestCount = Amount3Helper.getInterestCount(invest, yearRate, totalmonth);
        } else {
            //一次性还本付息
            interestCount = Amount4Helper.getInterestCount(invest, yearRate, totalmonth);
        }
        return interestCount;
    }

    @Resource
    private UserInfoMapper userInfoMapper;
    @Resource
    private UserAccountMapper userAccountMapper;
    @Resource
    private LendItemService lendItemService;
    @Resource
    private TransFlowService transFlowService;
    /**
     * 流程：
     * （1）标的状态和标的平台收益
     * （2）给借款账号转入金额
     * （3）增加借款交易流水
     * （4）解冻并扣除投资人资金
     * （5）增加投资人交易流水
     * （6）生成借款人还款计划和出借人回款计划
     * */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void makeLoan(Long lendId) {
        //根据标的id（非标的编号）获取标的信息
        Lend lend = baseMapper.selectById(lendId);

        //组装向第三方放款接口调用提交的信息
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("agentId", HfbConst.AGENT_ID);////hfb给商户分配的唯一标识
        paramMap.put("agentProjectCode", lend.getLendNo());//标的编号
        String agentBillNo = LendNoUtils.getLoanNo();//生成放款编号
        paramMap.put("agentBillNo", agentBillNo);

        //平台收益，提前从放款扣除得出实际放款金额，借款人借款实际金额=借款金额-平台收益
        //月年化
        BigDecimal monthRate = lend.getServiceRate().divide(new BigDecimal(12), 8, BigDecimal.ROUND_DOWN);
        //平台实际收益 = 已投金额 * 月年化 * 标的期数
        BigDecimal realAmount = lend.getInvestAmount().multiply(monthRate).multiply(new BigDecimal(lend.getPeriod()));

        paramMap.put("mchFee", realAmount); //商户手续费(平台实际收益)
        paramMap.put("timestamp", RequestHelper.getTimestamp());//获取时间戳
        String sign = RequestHelper.getSign(paramMap);
        paramMap.put("sign", sign);

        log.info("放款参数：" + JSONObject.toJSONString(paramMap));
        //发送同步远程调用（注意是同步！不是异步，发送后result直接接收到了第三方平台的JSON响应）
        JSONObject result = RequestHelper.sendRequest(paramMap, HfbConst.MAKE_LOAD_URL);
        log.info("放款结果：" + result.toJSONString());

        //放款失败
        if (!"0000".equals(result.getString("resultCode"))) {
            throw new BusinessException(result.getString("resultMsg"));
        }

        //更新标的信息
        lend.setRealAmount(realAmount);
        //PAY_RUN(2, "还款中"),
        lend.setStatus(LendStatusEnum.PAY_RUN.getStatus());
        lend.setPaymentTime(LocalDateTime.now());//放款时间
        baseMapper.updateById(lend);

        //根据user_id获取用户（借款人）信息，间接获得bindCode
        Long userId = lend.getUserId();
        UserInfo userInfo = userInfoMapper.selectById(userId);
        String bindCode = userInfo.getBindCode();

        //给借款账号转入金额
        BigDecimal total = new BigDecimal(result.getString("voteAmt"));
        userAccountMapper.updateAccount(bindCode, total, new BigDecimal(0));

        //新增借款人交易流水
        TransFlowBO transFlowBO = new TransFlowBO(
                agentBillNo,//放款编号
                bindCode,
                total,
                TransTypeEnum.BORROW_BACK,//BORROW_BACK(5,"放款到账")
                "借款放款到账，编号：" + lend.getLendNo());//标的项目编号
        transFlowService.saveTransFlow(transFlowBO);

        //获取投资列表信息
        List<LendItem> lendItemList = lendItemService.selectByLendId(lendId, 1);
        lendItemList.stream().forEach(item -> {

            //获取投资人信息
            Long investUserId = item.getInvestUserId();
            UserInfo investUserInfo = userInfoMapper.selectById(investUserId);
            String investBindCode = investUserInfo.getBindCode();

            //投资人账号冻结金额转出
            BigDecimal investAmount = item.getInvestAmount(); //投资金额
            userAccountMapper.updateAccount(investBindCode, new BigDecimal(0), investAmount.negate());

            //新增投资人交易流水
            TransFlowBO investTransFlowBO = new TransFlowBO(
                    LendNoUtils.getTransNo(),
                    investBindCode,
                    investAmount,
                    TransTypeEnum.INVEST_UNLOCK,
                    "冻结资金转出，出借放款，编号：" + lend.getLendNo());//项目编号
            transFlowService.saveTransFlow(investTransFlowBO);
        });

        //放款成功生成借款人还款计划和投资人回款计划
        this.repaymentPlan(lend);

    }


    @Resource
    private LendReturnService lendReturnService;

    @Resource
    private LendItemReturnService lendItemReturnService;

    /**
     * 生借款人还款计划
     * 内部调用方法生成投资人回款计划
     * @param lend
     */
    private void repaymentPlan(Lend lend) {

        //还款计划列表
        List<LendReturn> lendReturnList = new ArrayList<>();

        //按还款时间生成还款计划
        int len = lend.getPeriod().intValue();
        for (int i = 1; i <= len; i++) {

            //创建还款计划对象
            LendReturn lendReturn = new LendReturn();
            lendReturn.setReturnNo(LendNoUtils.getReturnNo());//生成还款编号
            lendReturn.setLendId(lend.getId());//借款（标的）id
            lendReturn.setBorrowInfoId(lend.getBorrowInfoId());//借款信息id
            lendReturn.setUserId(lend.getUserId());//借款人id
            lendReturn.setAmount(lend.getAmount());//标的金额
            lendReturn.setBaseAmount(lend.getInvestAmount());//计息本金额
            lendReturn.setLendYearRate(lend.getLendYearRate());
            lendReturn.setCurrentPeriod(i);//当前期数
            lendReturn.setReturnMethod(lend.getReturnMethod());

            //说明：还款计划中的这三项 = 回款计划中对应的这三项和：因此需要先生成对应的回款计划
            //			lendReturn.setPrincipal();
            //			lendReturn.setInterest();
            //			lendReturn.setTotal();

            lendReturn.setFee(new BigDecimal(0));
            lendReturn.setReturnDate(lend.getLendStartDate().plusMonths(i)); //第二个月开始还款
            lendReturn.setOverdue(false);
            if (i == len) { //最后一个月
                //标识为最后一次还款，特殊处理：防止小数位除法带来的四舍五入问题
                lendReturn.setLast(true);
            } else {
                lendReturn.setLast(false);
            }
            lendReturn.setStatus(0);
            lendReturnList.add(lendReturn);
        }
        //批量保存
        lendReturnService.saveBatch(lendReturnList);

        //获取lendReturnList中还款期数与还款计划id对应map
        Map<Integer, Long> lendReturnMap = lendReturnList.stream().collect(
                Collectors.toMap(LendReturn::getCurrentPeriod, LendReturn::getId)
        );

        //======================================================
        //=============获取所有投资者，生成回款计划===================
        //======================================================
        //回款计划列表
        List<LendItemReturn> lendItemReturnAllList = new ArrayList<>();
        //获取投资成功的投资记录
        List<LendItem> lendItemList = lendItemService.selectByLendId(lend.getId(), 1);
        for (LendItem lendItem : lendItemList) {

            //创建回款计划列表
            List<LendItemReturn> lendItemReturnList = this.returnInvest(lendItem.getId(), lendReturnMap, lend);
            lendItemReturnAllList.addAll(lendItemReturnList);
        }

        //更新还款计划中的相关金额数据
        for (LendReturn lendReturn : lendReturnList) {

            BigDecimal sumPrincipal = lendItemReturnAllList.stream()
                    //过滤条件：当回款计划中的还款计划id == 当前还款计划id的时候
                    .filter(item -> item.getLendReturnId().longValue() == lendReturn.getId().longValue())
                    //将所有回款计划中计算的每月应收本金相加
                    .map(LendItemReturn::getPrincipal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal sumInterest = lendItemReturnAllList.stream()
                    .filter(item -> item.getLendReturnId().longValue() == lendReturn.getId().longValue())
                    .map(LendItemReturn::getInterest)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal sumTotal = lendItemReturnAllList.stream()
                    .filter(item -> item.getLendReturnId().longValue() == lendReturn.getId().longValue())
                    .map(LendItemReturn::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            lendReturn.setPrincipal(sumPrincipal); //每期还款本金
            lendReturn.setInterest(sumInterest); //每期还款利息
            lendReturn.setTotal(sumTotal); //每期还款本息
        }
        lendReturnService.updateBatchById(lendReturnList);
    }


    /**
     * 生成每个投资人的回款计划
     *
     * @param lendItemId
     * @param lendReturnMap 还款期数与还款计划id对应map
     * @param lend
     * @return
     */
    public List<LendItemReturn> returnInvest(Long lendItemId, Map<Integer, Long> lendReturnMap, Lend lend) {

        //投标信息
        LendItem lendItem = lendItemService.getById(lendItemId);

        //投资金额
        BigDecimal amount = lendItem.getInvestAmount();
        //年化利率
        BigDecimal yearRate = lendItem.getLendYearRate();
        //投资期数
        int totalMonth = lend.getPeriod();

        Map<Integer, BigDecimal> mapInterest = null;  //还款期数 -> 利息
        Map<Integer, BigDecimal> mapPrincipal = null; //还款期数 -> 本金

        //根据还款方式计算本金和利息
        if (lend.getReturnMethod().intValue() == ReturnMethodEnum.ONE.getMethod()) {
            //利息
            mapInterest = Amount1Helper.getPerMonthInterest(amount, yearRate, totalMonth);
            //本金
            mapPrincipal = Amount1Helper.getPerMonthPrincipal(amount, yearRate, totalMonth);
        } else if (lend.getReturnMethod().intValue() == ReturnMethodEnum.TWO.getMethod()) {
            mapInterest = Amount2Helper.getPerMonthInterest(amount, yearRate, totalMonth);
            mapPrincipal = Amount2Helper.getPerMonthPrincipal(amount, yearRate, totalMonth);
        } else if (lend.getReturnMethod().intValue() == ReturnMethodEnum.THREE.getMethod()) {
            mapInterest = Amount3Helper.getPerMonthInterest(amount, yearRate, totalMonth);
            mapPrincipal = Amount3Helper.getPerMonthPrincipal(amount, yearRate, totalMonth);
        } else {
            mapInterest = Amount4Helper.getPerMonthInterest(amount, yearRate, totalMonth);
            mapPrincipal = Amount4Helper.getPerMonthPrincipal(amount, yearRate, totalMonth);
        }

        //创建回款计划列表
        List<LendItemReturn> lendItemReturnList = new ArrayList<>();
        for (Map.Entry<Integer, BigDecimal> entry : mapInterest.entrySet()) {
            Integer currentPeriod = entry.getKey();
            //根据还款期数获取还款计划的id
            Long lendReturnId = lendReturnMap.get(currentPeriod);

            LendItemReturn lendItemReturn = new LendItemReturn();
            lendItemReturn.setLendReturnId(lendReturnId);
            lendItemReturn.setLendItemId(lendItemId);
            lendItemReturn.setInvestUserId(lendItem.getInvestUserId());
            lendItemReturn.setLendId(lendItem.getLendId());
            lendItemReturn.setInvestAmount(lendItem.getInvestAmount());
            lendItemReturn.setLendYearRate(lend.getLendYearRate());
            lendItemReturn.setCurrentPeriod(currentPeriod);
            lendItemReturn.setReturnMethod(lend.getReturnMethod());
            //最后一次本金计算
            if (lendItemReturnList.size() > 0 && currentPeriod.intValue() == lend.getPeriod().intValue()) {
                //最后一期本金 = 本金 - 前几次之和，防止小数位除法带来的四舍五入问题
                BigDecimal sumPrincipal = lendItemReturnList.stream()
                        .map(LendItemReturn::getPrincipal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                //最后一期应还本金 = 用当前投资人的总投资金额 - 除了最后一期前面期数计算出来的所有的应还本金
                BigDecimal lastPrincipal = lendItem.getInvestAmount().subtract(sumPrincipal);
                lendItemReturn.setPrincipal(lastPrincipal);
            } else {
                lendItemReturn.setPrincipal(mapPrincipal.get(currentPeriod));
                lendItemReturn.setInterest(mapInterest.get(currentPeriod));
            }

            lendItemReturn.setTotal(lendItemReturn.getPrincipal().add(lendItemReturn.getInterest()));
            lendItemReturn.setFee(new BigDecimal("0"));
            lendItemReturn.setReturnDate(lend.getLendStartDate().plusMonths(currentPeriod));
            //是否逾期，默认未逾期
            lendItemReturn.setOverdue(false);
            lendItemReturn.setStatus(0);

            lendItemReturnList.add(lendItemReturn);
        }
        lendItemReturnService.saveBatch(lendItemReturnList);

        return lendItemReturnList;
    }
    @Resource
    private LendItemMapper lendItemMapper;
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void cancelLend(Long lendId) {
        /*
         * 1.根据id从lend表获取标的对象，间接获得lend_id
         * 2.- 通知第三方平台撤标（同步），响应0000=接收撤标信息成功则第三方成功撤标
         * 3.根据lend_id从在lend_item表中获得List<LendItem>(内涵lend_id)
         * 4.遍历List<LendItem>//返回投资人金额
         *    - 封装撤标对象主要是"agentProjectCode"= lend.getLendNo();//项目标号
         *    - 修改当前lendItem的状态信息等
         *    - 根据LendItem的invest_user_id（用户id）修改用户冻结金额
         * 5.修改lend对象的状态等
         * */


        //根据标的id（非标的编号）获取标的信息
        Lend lend = baseMapper.selectById(lendId);
        //标的状态必须为募资中
        Assert.isTrue(
                lend.getStatus().intValue() == LendStatusEnum.INVEST_RUN.getStatus().intValue(),
                ResponseEnum.LEND_INVEST_ERROR);

        //组装向第三方放款接口调用提交撤标的信息
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("agentId", HfbConst.AGENT_ID);////hfb给商户分配的唯一标识
        paramMap.put("agentProjectCode", lend.getLendNo());//标的编号
        paramMap.put("timestamp", RequestHelper.getTimestamp());//获取时间戳
        String sign = RequestHelper.getSign(paramMap);
        paramMap.put("sign", sign);

        log.info("撤标参数：" + JSONObject.toJSONString(paramMap));
        //发送同步远程调用（注意是同步！不是异步，发送后result直接接收到了第三方平台的JSON响应）
        JSONObject result = RequestHelper.sendRequest(paramMap, HfbConst.CANCEL_LEND_URL);
        log.info("撤标结果：" + result.toJSONString());

        //撤标失败
        //0000=接收撤标信息成功||00001=撤标处理完成
        if (!"0000".equals(result.getString("resultCode"))||!"0001".equals(result.getString("resultCode"))) {
            throw new BusinessException(result.getString("resultMsg"));
        }

        //更新撤标的标的信息
        //CANCEL(-1, "已撤标"),
        lend.setStatus(LendStatusEnum.CANCEL.getStatus());
        lend.setCheckTime(LocalDateTime.now());//结束时间
//        lend.setDeleted(true);//逻辑删除
        baseMapper.updateById(lend);


        //获取投资列表信息
        List<LendItem> lendItemList = lendItemService.selectByLendId(lendId, 1);
        lendItemList.stream().forEach(item -> {
            //获取投资人信息
            Long investUserId = item.getInvestUserId();
            UserInfo investUserInfo = userInfoMapper.selectById(investUserId);
            String investBindCode = investUserInfo.getBindCode();

            //投资人账号冻结金额转出
            BigDecimal investAmount = item.getInvestAmount(); //投资金额
            userAccountMapper.updateAccount(investBindCode, investAmount,investAmount.negate());

            //新增投资人交易流水
            TransFlowBO investTransFlowBO = new TransFlowBO(
                    LendNoUtils.getTransNo(),
                    investBindCode,
                    investAmount,
                    TransTypeEnum.INVEST_UNLOCK,
                    "标的:"+lend.getLendNo()+"撤标，"+"把投资编号"+item.getLendItemNo()+"的投资金额"+investAmount+" 返回给给投资人：" + item.getInvestUserId());
            transFlowService.saveTransFlow(investTransFlowBO);
            //修改当前lendItem的状态信息等
            item.setStatus(2);
            item.setDeleted(true);
            lendItemMapper.updateById(item);
        });

    }
}
