package org.example.srb.core.service;

import org.example.srb.core.pojo.entity.LendReturn;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 还款记录表 服务类
 * </p>
 *
 * @author wendao
 * @since 2024-04-01
 */
public interface LendReturnService extends IService<LendReturn> {
    /**
     * 获取标的的还款计划列表（从借款人的角度理解）
     * @param lendId
     * @return java.util.List<org.example.srb.core.pojo.entity.LendReturn>
     * @author Administrator
     * @date 2024/4/19 0019 22:37
    */
    List<LendReturn> selectByLendId(Long lendId);
    /**
     * 用户还款
     * @param lendReturnId
     * @param userId
     * @return java.lang.String
     * @author Administrator
     * @date 2024/4/20 0020 0:26
    */
    String commitReturn(Long lendReturnId, Long userId);
    /**
     * 还款异步回调
     * @param paramMap
     * @return void
     * @author Administrator
     * @date 2024/4/20 0020 0:31       
    */
    void notify(Map<String, Object> paramMap);
}
