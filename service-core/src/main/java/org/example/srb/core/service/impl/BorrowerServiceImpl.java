package org.example.srb.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.srb.core.enums.BorrowerStatusEnum;
import org.example.srb.core.enums.IntegralEnum;
import org.example.srb.core.mapper.BorrowerAttachMapper;
import org.example.srb.core.mapper.BorrowerMapper;
import org.example.srb.core.mapper.UserInfoMapper;
import org.example.srb.core.mapper.UserIntegralMapper;
import org.example.srb.core.pojo.entity.Borrower;
import org.example.srb.core.pojo.entity.BorrowerAttach;
import org.example.srb.core.pojo.entity.UserInfo;
import org.example.srb.core.pojo.entity.UserIntegral;
import org.example.srb.core.pojo.vo.BorrowerApprovalVO;
import org.example.srb.core.pojo.vo.BorrowerAttachVO;
import org.example.srb.core.pojo.vo.BorrowerDetailVO;
import org.example.srb.core.pojo.vo.BorrowerVO;
import org.example.srb.core.service.BorrowerAttachService;
import org.example.srb.core.service.BorrowerService;
import org.example.srb.core.service.DictService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 借款人 服务实现类
 * </p>
 *
 * @author wendao
 * @since 2024-04-01
 */
@Service
public class BorrowerServiceImpl extends ServiceImpl<BorrowerMapper, Borrower> implements BorrowerService {

    @Resource
    private BorrowerAttachMapper borrowerAttachMapper;
    @Resource
    private BorrowerAttachService borrowerAttachService;
    @Resource
    private DictService dictService;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private UserIntegralMapper userIntegralMapper;

    /**
     * 保存借款人信息
     * @param borrowerVO
     * @param userId
     * @return void
     * @author Administrator
     * @date 2024/4/14 0014 16:14
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveBorrowerVOByUserId(BorrowerVO borrowerVO, Long userId) {

        UserInfo userInfo = userInfoMapper.selectById(userId);

        //保存借款人信息
        Borrower borrower = new Borrower();
        BeanUtils.copyProperties(borrowerVO, borrower);
        borrower.setUserId(userId);
        borrower.setName(userInfo.getName());
        borrower.setIdCard(userInfo.getIdCard());
        borrower.setMobile(userInfo.getMobile());
        borrower.setStatus(BorrowerStatusEnum.AUTH_RUN.getStatus());//认证中
        baseMapper.insert(borrower);

        //保存附件
        List<BorrowerAttach> borrowerAttachList = borrowerVO.getBorrowerAttachList();
        borrowerAttachList.forEach(borrowerAttach -> {
            borrowerAttach.setBorrowerId(borrower.getId());
            borrowerAttachMapper.insert(borrowerAttach);
        });

        //更新会员状态，更新为认证中
        userInfo.setBorrowAuthStatus(BorrowerStatusEnum.AUTH_RUN.getStatus());
        userInfoMapper.updateById(userInfo);
    }
    /**
     * 检测借款人是否审核成工
     * @param userId
     * @return java.lang.Integer
     * @author Administrator
     * @date 2024/4/14 0014 16:13
    */
    @Override
    public Integer getStatusByUserId(Long userId) {
        QueryWrapper<Borrower> borrowerQueryWrapper = new QueryWrapper<>();
        borrowerQueryWrapper.select("status").eq("user_id", userId);
        List<Object> objects = baseMapper.selectObjs(borrowerQueryWrapper);

        if(objects.size() == 0){
            //借款人尚未提交信息
            return BorrowerStatusEnum.NO_AUTH.getStatus();
        }
        Integer status = (Integer)objects.get(0);
        return status;
    }
    /**
     * 获取借款人列表分页
     * @param pageParam
     * @param keyWord
     * @return com.baomidou.mybatisplus.core.metadata.IPage<org.example.srb.core.pojo.entity.Borrower>
     * @author Administrator
     * @date 2024/4/14 0014 16:13
    */
    @Override
    public IPage<Borrower> listPage(Page<Borrower> pageParam, String keyWord) {
        //非条件查询
        if(StringUtils.isEmpty(keyWord)){
            return baseMapper.selectPage(pageParam,null);
        }
        //条件查询
        QueryWrapper<Borrower> borrowerQueryWrapper =new QueryWrapper<>();
        borrowerQueryWrapper.like("name",keyWord)
                .or().like("id_card",keyWord)
                .or().like("mobile",keyWord)
                .orderByDesc("id");
        return baseMapper.selectPage(pageParam,borrowerQueryWrapper);
    }
    /*根据ID获取借款人的详细信息*/
    @Override
    public BorrowerDetailVO getBorrowerDetailVOById(Long id) {
        //获取借款人信息
        Borrower borrower = baseMapper.selectById(id);

        //填充基本借款人信息
        BorrowerDetailVO borrowerDetailVO = new BorrowerDetailVO();
        BeanUtils.copyProperties(borrower, borrowerDetailVO);

        //婚否
        borrowerDetailVO.setMarry(borrower.getMarry()?"是":"否");
        //性别
        borrowerDetailVO.setSex(borrower.getSex()==1?"男":"女");

        //计算下拉列表选中内容
        String education = dictService.getNameByParentDictCodeAndValue("education", borrower.getEducation());
        String industry = dictService.getNameByParentDictCodeAndValue("moneyUse", borrower.getIndustry());
        String income = dictService.getNameByParentDictCodeAndValue("income", borrower.getIncome());
        String returnSource = dictService.getNameByParentDictCodeAndValue("returnSource", borrower.getReturnSource());
        String contactsRelation = dictService.getNameByParentDictCodeAndValue("relation", borrower.getContactsRelation());

        //设置下拉列表选中内容
        borrowerDetailVO.setEducation(education);
        borrowerDetailVO.setIndustry(industry);
        borrowerDetailVO.setIncome(income);
        borrowerDetailVO.setReturnSource(returnSource);
        borrowerDetailVO.setContactsRelation(contactsRelation);

        //审批状态
        String status = BorrowerStatusEnum.getMsgByStatus(borrower.getStatus());
        borrowerDetailVO.setStatus(status);

        //获取附件VO列表
        List<BorrowerAttachVO> borrowerAttachVOList =  borrowerAttachService.selectBorrowerAttachVOList(id);
        borrowerDetailVO.setBorrowerAttachVOList(borrowerAttachVOList);

        return borrowerDetailVO;
    }
    /*审批*/
    @Override
    public void approval(BorrowerApprovalVO borrowerApprovalVO) {
        /*
        (1)在user_integral表中添加积分明细
        (2)在user_info表中添加总积分（user_info表中的原始积分 + user_integral表中的积分明细之和 ）
        (3)修改borrower表的借款申请审核状态
        (4)修改user_info表中的借款申请审核状态
        */

        //从前端ajax获取借款人认证状态
        Long borrowerId = borrowerApprovalVO.getBorrowerId();
        //获取borrower中原始的信息
        Borrower borrower = baseMapper.selectById(borrowerId);
        /*=============(3)更新borrower表中的审核状态=============*/
        borrower.setStatus(borrowerApprovalVO.getStatus());
        baseMapper.updateById(borrower);
        //根据id获取user_info表中的信息
        Long userId = borrower.getUserId();
        UserInfo userInfo = userInfoMapper.selectById(userId);

        /*========== (1)在user_integral表中添加积分明细=========*/
        //添加积分
        UserIntegral userIntegral = new UserIntegral();//积分对象
        userIntegral.setUserId(userId);
        //1.往积分记录表中条件加基础信息积分的记录
        userIntegral.setIntegral(borrowerApprovalVO.getInfoIntegral());
        userIntegral.setContent("借款人基本信息");
        userIntegralMapper.insert(userIntegral);

        //在user_info表中添加总积分= user_info表中的原始积分 + user_integral表中的积分明细之和(当前指基础信息积分)
        int curIntegral = userInfo.getIntegral() + borrowerApprovalVO.getInfoIntegral();

        if(borrowerApprovalVO.getIsIdCardOk()) {
            //在user_info表中添加总积分 += 当前idCard信息积分)
            curIntegral += IntegralEnum.BORROWER_IDCARD.getIntegral();
            //2.往积分记录表中条件加idCard信息积分的记录
            userIntegral = new UserIntegral();
            userIntegral.setUserId(userId);
            userIntegral.setIntegral(IntegralEnum.BORROWER_IDCARD.getIntegral());
            userIntegral.setContent(IntegralEnum.BORROWER_IDCARD.getMsg());
            userIntegralMapper.insert(userIntegral);
        }

        if(borrowerApprovalVO.getIsHouseOk()) {
            //在user_info表中添加总积分 += 当前house信息积分)
            curIntegral += IntegralEnum.BORROWER_HOUSE.getIntegral();
            //3.往积分记录表中条件加house信息积分的记录
            userIntegral = new UserIntegral();
            userIntegral.setUserId(userId);
            userIntegral.setIntegral(IntegralEnum.BORROWER_HOUSE.getIntegral());
            userIntegral.setContent(IntegralEnum.BORROWER_HOUSE.getMsg());
            userIntegralMapper.insert(userIntegral);
        }

        if(borrowerApprovalVO.getIsCarOk()) {
            //在user_info表中添加总积分 += 当前car信息积分)
            curIntegral += IntegralEnum.BORROWER_CAR.getIntegral();
            //4.往积分记录表中条件加car信息积分的记录
            userIntegral = new UserIntegral();
            userIntegral.setUserId(userId);
            userIntegral.setIntegral(IntegralEnum.BORROWER_CAR.getIntegral());
            userIntegral.setContent(IntegralEnum.BORROWER_CAR.getMsg());
            userIntegralMapper.insert(userIntegral);
        }
        /*==========(2)在user_info表中添加总积分============*/
        userInfo.setIntegral(curIntegral);
        /*=============(4)修改user_info表中的借款申请审核状态==============*/
        userInfo.setBorrowAuthStatus(borrowerApprovalVO.getStatus());
        userInfoMapper.updateById(userInfo);
    }
}
