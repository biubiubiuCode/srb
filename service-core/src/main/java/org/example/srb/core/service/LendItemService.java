package org.example.srb.core.service;

import org.example.srb.core.pojo.entity.LendItem;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.srb.core.pojo.vo.InvestVO;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 标的出借记录表 服务类
 * 多对多关系表
 * 把借款人与投资人关联在一起
 * </p>
 *
 * @author wendao
 * @since 2024-04-01
 */
public interface LendItemService extends IService<LendItem> {

    /**
     * 投标，构建充值自动提交表单（与第三方交互）
     * @param investVO
     * @return java.lang.String
     * @author Administrator
     * @date 2024/4/18 0018 19:35
    */
    String commitInvest(InvestVO investVO);
    /**
     * 会员投资异步回调
     *
     * @param paramMap
     * @return void
     * @author Administrator
     * @date 2024/4/18 0018 19:49
     */
    String notify(Map<String, Object> paramMap);
    /**
     *
     * @param lendId 标的id（标的表中的主键值，非标的编号）
     * @param status 状态
     * @return java.util.List<org.example.srb.core.pojo.entity.LendItem>
     * @author Administrator
     * @date 2024/4/19 0019 0:41       
    */
    List<LendItem> selectByLendId(Long lendId,Integer status);
    /**
     * 根据标的的id（lend表主键）获取标的的投资列表
     * @param lendId
     * @return java.util.List<org.example.srb.core.pojo.entity.LendItem>
     * @author Administrator
     * @date 2024/4/19 0019 22:11
    */
    List<LendItem> selectByLendId(Long lendId);
}
