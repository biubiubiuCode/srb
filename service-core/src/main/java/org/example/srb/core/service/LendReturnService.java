package org.example.srb.core.service;

import org.example.srb.core.pojo.entity.LendReturn;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

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
}
