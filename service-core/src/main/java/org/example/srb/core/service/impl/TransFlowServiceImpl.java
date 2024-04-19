package org.example.srb.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.srb.core.mapper.TransFlowMapper;
import org.example.srb.core.mapper.UserInfoMapper;
import org.example.srb.core.pojo.bo.TransFlowBO;
import org.example.srb.core.pojo.entity.TransFlow;
import org.example.srb.core.pojo.entity.UserInfo;
import org.example.srb.core.service.TransFlowService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 交易流水表 服务实现类
 * </p>
 *
 * @author wendao
 * @since 2024-04-01
 */
@Service
public class TransFlowServiceImpl extends ServiceImpl<TransFlowMapper, TransFlow> implements TransFlowService {

    @Resource
    private UserInfoMapper userInfoMapper;
    @Override
    public void saveTransFlow(TransFlowBO transFlowBO) {
        //根据bind_code从user_info获取用户基本信息
        QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
        userInfoQueryWrapper.eq("bind_code", transFlowBO.getBindCode());
        UserInfo userInfo = userInfoMapper.selectOne(userInfoQueryWrapper);

        //存储交易流水数据
        TransFlow transFlow = new TransFlow();
        transFlow.setUserId(userInfo.getId());
        transFlow.setUserName(userInfo.getName());
        transFlow.setTransNo(transFlowBO.getAgentBillNo());
        transFlow.setTransType(transFlowBO.getTransTypeEnum().getTransType());
        transFlow.setTransTypeName(transFlowBO.getTransTypeEnum().getTransTypeName());
        transFlow.setTransAmount(transFlowBO.getAmount());
        transFlow.setMemo(transFlowBO.getMemo());
        baseMapper.insert(transFlow);
    }

    @Override
    public boolean isSaveTransFlow(String agentBillNo) {
        //根据流水单号trans_no判断是否已有记录
        QueryWrapper<TransFlow> queryWrapper = new QueryWrapper();
        queryWrapper.eq("trans_no", agentBillNo);
        int count = baseMapper.selectCount(queryWrapper);
        if(count > 0) {
            return true;
        }
        return false;
    }

    @Override
    public List<TransFlow> selectByUserId(Long userId) {
        QueryWrapper<TransFlow> queryWrapper = new QueryWrapper<>();
        queryWrapper
                .eq("user_id", userId)
                .orderByDesc("id");
        return baseMapper.selectList(queryWrapper);
    }
}
