package org.example.srb.core.service;

import org.example.srb.core.pojo.entity.BorrowInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.srb.core.pojo.vo.BorrowInfoApprovalVO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 借款信息表 服务类
 * </p>
 *
 * @author wendao
 * @since 2024-04-01
 */
public interface BorrowInfoService extends IService<BorrowInfo> {
    /**
     * 根据UserId获取借款人的积分信息，然后根据dict返回借款额度
     * @param null
     * @return  BigDecimal //金融相关需精确小数
     * @author Administrator
     * @date 2024/4/15 0015 16:51
    */

    BigDecimal getBorrowAmount(Long userId);
    /**
     * 借款人提交借款要判断借款人账户绑定状态与借款人信息审批状态，
     * 只有这两个状态都成立才能借款，这两个状态都在会员表中
     *
     * 目标：将借款申请表单中用户填写的数据保存在borrow_info数据库表中
     * @param null
     * @return
     * @author Administrator
     * @date 2024/4/15 0015 18:57       
    */

    void saveBorrowInfo(BorrowInfo borrowInfo, Long userId);
    /**
     * 获取审核借款申请的审核状态
     * @param userId
     * @return java.lang.Integer
     * @author Administrator
     * @date 2024/4/15 0015 19:06
    */
    Integer getStatusByUserId(Long userId);
    /**
     * 后台的借款信息审核管理
     * @param
     * @return java.util.List<org.example.srb.core.pojo.entity.BorrowInfo>
     * @author Administrator
     * @date 2024/4/16 0016 15:55
    */
    List<BorrowInfo> selectList();
    /**
     * 获取单个具体借款申请的信息
     * @param id 借款id
     * @return java.util.Map<java.lang.String,java.lang.Object>返回借款信息+借款人信息
     * @author Administrator
     * @date 2024/4/16 0016 17:33
    */
    Map<String, Object> getBorrowInfoDetail(Long id);
    /**
     * 借款信息的人工调整审批界面的接口
     * @param borrowInfoApprovalVO
     * @return void
     * @author Administrator
     * @date 2024/4/16 0016 22:44
    */
    void approval(BorrowInfoApprovalVO borrowInfoApprovalVO);
}
