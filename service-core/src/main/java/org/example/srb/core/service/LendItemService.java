package org.example.srb.core.service;

import org.example.srb.core.pojo.entity.LendItem;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.srb.core.pojo.vo.InvestVO;

import java.util.Map;

/**
 * <p>
 * 标的出借记录表 服务类
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
}
