package org.example.srb.core.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.srb.core.pojo.entity.UserInfo;
import org.example.srb.core.pojo.query.UserInfoQuery;
import org.example.srb.core.pojo.vo.LoginVO;
import org.example.srb.core.pojo.vo.RegisterVO;
import org.example.srb.core.pojo.vo.UserIndexVO;
import org.example.srb.core.pojo.vo.UserInfoVO;

/**
 * <p>
 * 用户基本信息 服务类
 * </p>
 *
 * @author wendao
 * @since 2024-04-01
 */
public interface UserInfoService extends IService<UserInfo> {
    void register(RegisterVO registerVO);

    UserInfoVO login(LoginVO loginVO, String ip);

    IPage<UserInfo> listPage(Page<UserInfo> pageParam, UserInfoQuery userInfoQuery);

    void lock(Long id, Integer status);

    boolean checkMobile(String mobile);
    /**
     * 获取个人空间（首页）用户信息
     * @param userId
     * @return org.example.srb.core.pojo.vo.UserIndexVO
     * @author Administrator
     * @date 2024/4/19 0019 23:39
    */
    UserIndexVO getIndexUserInfo(Long userId);
}
