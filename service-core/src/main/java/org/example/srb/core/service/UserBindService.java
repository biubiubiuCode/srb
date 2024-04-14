package org.example.srb.core.service;

import org.example.srb.core.pojo.entity.UserBind;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.srb.core.pojo.vo.UserBindVO;

import java.util.Map;

/**
 * <p>
 * 用户绑定表 服务类
 * </p>
 *
 * @author wendao
 * @since 2024-04-01
 */
public interface UserBindService extends IService<UserBind> {
    /**
     * 账户绑定提交到托管平台的数据
     */
    String commitBindUser(UserBindVO userBindVO, Long userId);
    /**
    *  处理第三方发回来的回调
    */
    void notify(Map<String, Object> paramMap);
}
