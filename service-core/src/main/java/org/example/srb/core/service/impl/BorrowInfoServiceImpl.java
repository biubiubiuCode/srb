package org.example.srb.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.common.exception.Assert;
import org.example.common.result.ResponseEnum;
import org.example.srb.core.enums.BorrowInfoStatusEnum;
import org.example.srb.core.enums.BorrowerStatusEnum;
import org.example.srb.core.enums.UserBindEnum;
import org.example.srb.core.mapper.BorrowInfoMapper;
import org.example.srb.core.mapper.BorrowerMapper;
import org.example.srb.core.mapper.IntegralGradeMapper;
import org.example.srb.core.mapper.UserInfoMapper;
import org.example.srb.core.pojo.entity.BorrowInfo;
import org.example.srb.core.pojo.entity.Borrower;
import org.example.srb.core.pojo.entity.IntegralGrade;
import org.example.srb.core.pojo.entity.UserInfo;
import org.example.srb.core.pojo.vo.BorrowInfoApprovalVO;
import org.example.srb.core.pojo.vo.BorrowerDetailVO;
import org.example.srb.core.service.BorrowInfoService;
import org.example.srb.core.service.BorrowerService;
import org.example.srb.core.service.DictService;
import org.example.srb.core.service.LendService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 借款信息表 服务实现类
 *
 * @author wendao
 * @since 2024-04-01
 */
@Service
public class BorrowInfoServiceImpl extends ServiceImpl<BorrowInfoMapper, BorrowInfo> implements BorrowInfoService {
    @Resource
    private UserInfoMapper userInfoMapper;
    @Resource
    private IntegralGradeMapper integralGradeMapper;
    @Resource
    private DictService dictService;

    @Resource
    private BorrowerMapper borrowerMapper;

    @Resource
    private BorrowerService borrowerService;
    @Resource
    private LendService lendService;

    @Override
    public BigDecimal getBorrowAmount(Long userId) {
        //获取用户积分
        UserInfo userInfo=userInfoMapper.selectById(userId);
        //LOGIN_MOBILE_ERROR(208, "用户不存在")
        Assert.notNull(userInfo, ResponseEnum.LOGIN_MOBILE_ERROR);
        //获取积分
        Integer integral = userInfo.getIntegral();
        //根据积分查询额度
        QueryWrapper<IntegralGrade> queryWrapper =new QueryWrapper<>();
        queryWrapper.le("integral_start",integral)
                .ge("integral_end",integral);
        //积分区间不相交。最匹配的只有一个区间，用selectOne()
        IntegralGrade integralGradeConfig = integralGradeMapper.selectOne(queryWrapper);
        if (integralGradeConfig==null){
            return new BigDecimal(0);
        }
        return integralGradeConfig.getBorrowAmount();
    }

    @Override
    public void saveBorrowInfo(BorrowInfo borrowInfo, Long userId) {
        //获取userInfo的用户数据
        UserInfo userInfo = userInfoMapper.selectById(userId);

        //判断用户绑定状态
        Assert.isTrue(
                userInfo.getBindStatus().intValue() == UserBindEnum.BIND_OK.getStatus().intValue(),
                ResponseEnum.USER_NO_BIND_ERROR);

        //判断用户信息是否审批通过
        Assert.isTrue(
                userInfo.getBorrowAuthStatus().intValue() == BorrowerStatusEnum.AUTH_OK.getStatus().intValue(),
                ResponseEnum.USER_NO_AMOUNT_ERROR);

        //判断借款额度是否足够
        BigDecimal borrowAmount = this.getBorrowAmount(userId);
        Assert.isTrue(
                borrowInfo.getAmount().doubleValue() <= borrowAmount.doubleValue(),
                ResponseEnum.USER_AMOUNT_LESS_ERROR);

        //存储数据
        borrowInfo.setUserId(userId);
        //百分比转成小数
        borrowInfo.setBorrowYearRate( borrowInfo.getBorrowYearRate().divide(new BigDecimal(100)));
        borrowInfo.setStatus(BorrowInfoStatusEnum.CHECK_RUN.getStatus());
        baseMapper.insert(borrowInfo);
    }

    @Override
    public Integer getStatusByUserId(Long userId) {
        QueryWrapper<BorrowInfo> borrowInfoQueryWrapper = new QueryWrapper<>();
        borrowInfoQueryWrapper.select("status").eq("user_id", userId);
        //获取借款登记信息
        List<Object> objects = baseMapper.selectObjs(borrowInfoQueryWrapper);

        if(objects.size() == 0){
            //借款人尚未提交信息
            return BorrowInfoStatusEnum.NO_AUTH.getStatus();
        }
        Integer status = (Integer)objects.get(0);//只有一个status字段
        return status;
    }

    @Override
    public List<BorrowInfo> selectList() {
        List<BorrowInfo> borrowInfoList = baseMapper.selectBorrowInfoList();
        borrowInfoList.forEach(borrowInfo -> {
            //获取还款方式的名字（根据字典，把值转换成名字）
            String returnMethod=dictService.getNameByParentDictCodeAndValue("returnMethod", borrowInfo.getReturnMethod());
            //获取借款的用途（根据字典，把值转换成名字）
            String moneyUse = dictService.getNameByParentDictCodeAndValue("moneyUse", borrowInfo.getMoneyUse());
            //获取审核状态的名字
            String status = BorrowInfoStatusEnum.getMsgByStatus(borrowInfo.getStatus());
            //存值到borrowInfo其他参数Map中
            borrowInfo.getParam().put("returnMethod", returnMethod);
            borrowInfo.getParam().put("moneyUse", moneyUse);
            borrowInfo.getParam().put("status", status);
        });
        return borrowInfoList;
    }

    @Override
    public Map<String, Object> getBorrowInfoDetail(Long id) {
        //1.查询借款对象BorrowInfo，返回借款信息，同时间接获得了借款人user_id
        //封装borrowInfo对象的借款的其他参数：存借款用途、还款方式、审批状态等"
        BorrowInfo borrowInfo = baseMapper.selectById(id);
        //组装数据，操作类似获取借款列表selectList()
        String returnMethod = dictService.getNameByParentDictCodeAndValue("returnMethod", borrowInfo.getReturnMethod());
        String moneyUse = dictService.getNameByParentDictCodeAndValue("moneyUse", borrowInfo.getMoneyUse());
        String status = BorrowInfoStatusEnum.getMsgByStatus(borrowInfo.getStatus());
        borrowInfo.getParam().put("returnMethod", returnMethod);
        borrowInfo.getParam().put("moneyUse", moneyUse);
        borrowInfo.getParam().put("status", status);

        //根据user_id获取借款人对象的个人基本信息，提供给审查使用
        QueryWrapper<Borrower> borrowerQueryWrapper = new QueryWrapper<Borrower>();
        //根据user_id获得Borrower对象，再调用borrower的id查询转换成BorrowerDetailVO
        /*
        * 注意到getBorrowerDetailVOById()里面还查了一次borrower
        * 因为borrow_info表与borrower表的关联外键是user_id，
        * 因为borrower表与borrower_attach表的关联外键是borrower表的id，
        * 所有这里实际查了俩次！
        * 可以优化？需要重新设计表格！
        * 一、
        * 优化borrower表？user_id作为主键的话不利于分页分表，代价有点点大
        * 二、
        * 优化borrow_info表，把user_id改为borrower_id，可行，但要改其他接口。
        * 三、
        * 折中方案，直接重写getBorrowerDetailVOById()，传Borrower
        *
        */
        borrowerQueryWrapper.eq("user_id", borrowInfo.getUserId());
        Borrower borrower = borrowerMapper.selectOne(borrowerQueryWrapper);

        //2.组装借款人对象BorrowerDetailVO，返回借款人信息
//        BorrowerDetailVO borrowerDetailVO = borrowerService.getBorrowerDetailVOById(borrower.getId());
        BorrowerDetailVO borrowerDetailVO = borrowerService.getBorrowerDetailVOById(borrower);

        //3.Map组装数据
        Map<String, Object> result = new HashMap<>();
        result.put("borrowInfo", borrowInfo);
        result.put("borrower", borrowerDetailVO);
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void approval(BorrowInfoApprovalVO borrowInfoApprovalVO) {

        //修改借款信息状态
        Long borrowInfoId = borrowInfoApprovalVO.getId();
        BorrowInfo borrowInfo = baseMapper.selectById(borrowInfoId);
        borrowInfo.setStatus(borrowInfoApprovalVO.getStatus());
        baseMapper.updateById(borrowInfo);

        //审核通过则创建标的
        if (borrowInfoApprovalVO.getStatus().intValue() == BorrowInfoStatusEnum.CHECK_OK.getStatus().intValue()) {
            //创建标的
            lendService.createLend(borrowInfoApprovalVO, borrowInfo);
        }
    }
}
