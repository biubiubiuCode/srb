package org.example.srb.core.service;

import org.example.srb.core.pojo.entity.LendItemReturn;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 标的出借回款记录表 服务类
 * </p>
 *
 * @author wendao
 * @since 2024-04-01
 */
public interface LendItemReturnService extends IService<LendItemReturn> {

    /**
     * 投资用户查询借款的还款计划
     * @param lendId 借款id
     * @param userId 发起查询的用户id, 投资人
     * @return java.util.List<org.example.srb.core.pojo.entity.LendItemReturn>
     * @author Administrator
     * @date 2024/4/19 0019 23:10
    */
    List<LendItemReturn> selectByLendId(Long lendId, Long userId);
    /**
     * 根据还款id获取回款列表
     * @param lendReturnId
     * @return java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     * @author Administrator
     * @date 2024/4/20 0020 0:29
    */
    List<Map<String, Object>> addReturnDetail(Long lendReturnId);
    /**
     * 根据还款计划id获取对应的回款计划列表
     * @param lendReturnId
     * @return java.util.List<org.example.srb.core.pojo.entity.LendItemReturn>
     * @author Administrator
     * @date 2024/4/20 0020 0:30
    */
    List<LendItemReturn> selectLendItemReturnList(Long lendReturnId);
}
