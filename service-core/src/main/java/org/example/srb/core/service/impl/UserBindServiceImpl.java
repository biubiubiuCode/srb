package org.example.srb.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.common.exception.Assert;
import org.example.common.result.ResponseEnum;
import org.example.srb.core.enums.UserBindEnum;
import org.example.srb.core.hfb.FormHelper;
import org.example.srb.core.hfb.HfbConst;
import org.example.srb.core.hfb.RequestHelper;
import org.example.srb.core.mapper.UserBindMapper;
import org.example.srb.core.mapper.UserInfoMapper;
import org.example.srb.core.pojo.entity.UserBind;
import org.example.srb.core.pojo.entity.UserInfo;
import org.example.srb.core.pojo.vo.UserBindVO;
import org.example.srb.core.service.UserBindService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 用户绑定表 服务实现类
 * </p>
 *
 * @author wendao
 * @since 2024-04-01
 */
@Service
public class UserBindServiceImpl extends ServiceImpl<UserBindMapper, UserBind> implements UserBindService {

    @Resource
    private UserInfoMapper userInfoMapper;

    @Override
    public String commitBindUser(UserBindVO userBindVO, Long userId) {
        //查询身份证号码是否绑定
        /*
        * 相同的user_id，相同的身份证。如果存在就取出数据作更新
        * 不同的user_id，相同的身份证，则不允许
        * */
        QueryWrapper<UserBind> userBindQueryWrapper = new QueryWrapper<>();
        userBindQueryWrapper
                .eq("id_card", userBindVO.getIdCard())//保证身份证唯一
                .ne("user_id", userId);//保证用户数据不重复
        UserBind userBind = baseMapper.selectOne(userBindQueryWrapper);
        //USER_BIND_IDCARD_EXIST_ERROR(-301, "身份证号码已绑定"),
        Assert.isNull(userBind, ResponseEnum.USER_BIND_IDCARD_EXIST_ERROR);

        //查询用户绑定信息
        userBindQueryWrapper = new QueryWrapper<>();
        userBindQueryWrapper.eq("user_id", userId);
        userBind = baseMapper.selectOne(userBindQueryWrapper);

        //判断是否有绑定记录
        if(userBind == null) {
            //如果未创建绑定记录，则创建一条记录
            userBind = new UserBind();
            //BeanUtils拷贝userBindVO的同名属性到userBind中
            BeanUtils.copyProperties(userBindVO, userBind);
            userBind.setUserId(userId);
            userBind.setStatus(UserBindEnum.NO_BIND.getStatus());//状态设为未绑定第三方
            baseMapper.insert(userBind);
        } else {
            //曾经跳转到托管平台，但是未操作完成，此时将用户最新填写的数据同步到userBind对象
            BeanUtils.copyProperties(userBindVO, userBind);
            baseMapper.updateById(userBind);
        }

        //规则要求驼峰命名、按字段名升序排列
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("agentId", HfbConst.AGENT_ID);
        paramMap.put("agentUserId", userId);
        paramMap.put("idCard",userBindVO.getIdCard());
        paramMap.put("personalName", userBindVO.getName());
        paramMap.put("bankType", userBindVO.getBankType());
        paramMap.put("bankNo", userBindVO.getBankNo());
        paramMap.put("mobile", userBindVO.getMobile());
        paramMap.put("returnUrl", HfbConst.USERBIND_RETURN_URL);
        paramMap.put("notifyUrl", HfbConst.USERBIND_NOTIFY_URL);
        paramMap.put("timestamp", RequestHelper.getTimestamp());
        //RequestHelper.getSign()中实现排序
        paramMap.put("sign", RequestHelper.getSign(paramMap));

        //构建充值自动提交表单
        String formStr = FormHelper.buildForm(HfbConst.USERBIND_URL, paramMap);
        return formStr;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void notify(Map<String, Object> paramMap) {

        String bindCode = (String)paramMap.get("bindCode");
        //会员id
        String agentUserId = (String)paramMap.get("agentUserId");

        //根据user_id查询user_bind记录
        QueryWrapper<UserBind> userBindQueryWrapper = new QueryWrapper<>();
        userBindQueryWrapper.eq("user_id", agentUserId);

        //更新用户绑定表
        UserBind userBind = baseMapper.selectOne(userBindQueryWrapper);
        userBind.setBindCode(bindCode);//更新绑定的bindId,(实际上就是个逻辑外键)
        userBind.setStatus(UserBindEnum.BIND_OK.getStatus());//更新绑定状态
        baseMapper.updateById(userBind);

        //更新用户表
        UserInfo userInfo = userInfoMapper.selectById(agentUserId);
        userInfo.setBindCode(bindCode);//更新绑定的bindId,(实际上就是个逻辑外键)
        userInfo.setBindStatus(UserBindEnum.BIND_OK.getStatus());//更新绑定状态

        userInfo.setName(userBind.getName());
        userInfo.setIdCard(userBind.getIdCard());
        userInfoMapper.updateById(userInfo);
    }
}
