package org.example.srb.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.srb.core.pojo.entity.BorrowInfo;
import org.example.srb.core.pojo.entity.Lend;
import org.example.srb.core.pojo.vo.BorrowInfoApprovalVO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 标的准备表 服务类
 * </p>
 *
 * @author wendao
 * @since 2024-04-01
 */
public interface LendService extends IService<Lend> {
    /**
     * 创建标的
     * @param borrowInfoApprovalVO
     * @param borrowInfo
     * @return void
     * @author Administrator
     * @date 2024/4/17 0017 18:42
    */
    void createLend(BorrowInfoApprovalVO borrowInfoApprovalVO, BorrowInfo borrowInfo);
    /**
     * 给后台标的审核界面响应标的列表
     * @param
     * @return java.util.List<org.example.srb.core.pojo.entity.Lend>
     * @author Administrator
     * @date 2024/4/17 0017 19:07
    */
    List<Lend> selectList();
    /**
     * 获取标的信息
     * @param id 标的id
     * @return java.util.Map<java.lang.String,java.lang.Object>
     * @author Administrator
     * @date 2024/4/17 0017 20:34       
    */
    Map<String,Object> getLendDetail(Long id);
    /**
     * 根据投资金额计算收益（根据四种还款方式计算）
     * @param invest
     * @param yearRate
     * @param totalmonth
     * @param returnMethod
     * @return java.math.BigDecimal
     * @author Administrator
     * @date 2024/4/18 0018 19:25       
    */
    BigDecimal getInterestCount(BigDecimal invest, BigDecimal yearRate, Integer totalmonth, Integer returnMethod);

    /**
     * 标的满标放款
     * @param lendId
     * @return void
     * @author Administrator
     * @date 2024/4/19 0019 0:03
    */
    void makeLoan(Long lendId);
    /**
     * 撤标
     * @param lendId
     * @return void
     * @author Administrator
     * @date 2024/4/19 0019 1:53
    */
    void cancelLend(Long lendId);
}
