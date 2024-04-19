package org.example.srb.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.srb.core.pojo.bo.TransFlowBO;
import org.example.srb.core.pojo.entity.TransFlow;

import java.util.List;

/**
 * <p>
 * 交易流水表 服务类
 * </p>
 *
 * @author wendao
 * @since 2024-04-01
 */
public interface TransFlowService extends IService<TransFlow> {
    /**
     * 保存交易流水到交易流水表 trans_flow
     * @param transFlowBO
     * @return void
     * @author Administrator
     * @date 2024/4/18 0018 18:10
    */
    void saveTransFlow(TransFlowBO transFlowBO);
    /**
     * 交易幂等性处理：交易流水表 trans_flow中判断流水如果存在，则从业务方法中直接退出
     * @param agentBillNo
     * @return boolean
     * @author Administrator
     * @date 2024/4/18 0018 18:18       
    */
    boolean isSaveTransFlow(String agentBillNo);
    /**
     * 前端网页用户获取其交易流水
     * @param userId
     * @return java.util.List<org.example.srb.core.pojo.entity.TransFlow>
     * @author Administrator
     * @date 2024/4/19 0019 23:35
    */
    List<TransFlow> selectByUserId(Long userId);
}
