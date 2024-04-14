package org.example.srb.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.srb.core.enums.BorrowerStatusEnum;
import org.example.srb.core.mapper.BorrowerAttachMapper;
import org.example.srb.core.mapper.BorrowerMapper;
import org.example.srb.core.mapper.UserInfoMapper;
import org.example.srb.core.pojo.entity.Borrower;
import org.example.srb.core.pojo.entity.BorrowerAttach;
import org.example.srb.core.pojo.entity.UserInfo;
import org.example.srb.core.pojo.vo.BorrowerVO;
import org.example.srb.core.service.BorrowerService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 借款人 服务实现类
 * </p>
 *
 * @author wendao
 * @since 2024-04-01
 */
@Service
public class BorrowerServiceImpl extends ServiceImpl<BorrowerMapper, Borrower> implements BorrowerService {

    @Resource
    private BorrowerAttachMapper borrowerAttachMapper;

    @Resource
    private UserInfoMapper userInfoMapper;

    /**
     * 保存借款人信息
     * @param borrowerVO
     * @param userId
     * @return void
     * @author Administrator
     * @date 2024/4/14 0014 16:14
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveBorrowerVOByUserId(BorrowerVO borrowerVO, Long userId) {

        UserInfo userInfo = userInfoMapper.selectById(userId);

        //保存借款人信息
        Borrower borrower = new Borrower();
        BeanUtils.copyProperties(borrowerVO, borrower);
        borrower.setUserId(userId);
        borrower.setName(userInfo.getName());
        borrower.setIdCard(userInfo.getIdCard());
        borrower.setMobile(userInfo.getMobile());
        borrower.setStatus(BorrowerStatusEnum.AUTH_RUN.getStatus());//认证中
        baseMapper.insert(borrower);

        //保存附件
        List<BorrowerAttach> borrowerAttachList = borrowerVO.getBorrowerAttachList();
        borrowerAttachList.forEach(borrowerAttach -> {
            borrowerAttach.setBorrowerId(borrower.getId());
            borrowerAttachMapper.insert(borrowerAttach);
        });

        //更新会员状态，更新为认证中
        userInfo.setBorrowAuthStatus(BorrowerStatusEnum.AUTH_RUN.getStatus());
        userInfoMapper.updateById(userInfo);
    }
    /**
     * 检测借款人是否审核成工
     * @param userId
     * @return java.lang.Integer
     * @author Administrator
     * @date 2024/4/14 0014 16:13
    */
    @Override
    public Integer getStatusByUserId(Long userId) {
        QueryWrapper<Borrower> borrowerQueryWrapper = new QueryWrapper<>();
        borrowerQueryWrapper.select("status").eq("user_id", userId);
        List<Object> objects = baseMapper.selectObjs(borrowerQueryWrapper);

        if(objects.size() == 0){
            //借款人尚未提交信息
            return BorrowerStatusEnum.NO_AUTH.getStatus();
        }
        Integer status = (Integer)objects.get(0);
        return status;
    }
    /**
     * 获取借款人列表分页
     * @param pageParam
     * @param keyWord
     * @return com.baomidou.mybatisplus.core.metadata.IPage<org.example.srb.core.pojo.entity.Borrower>
     * @author Administrator
     * @date 2024/4/14 0014 16:13
    */
    @Override
    public IPage<Borrower> listPage(Page<Borrower> pageParam, String keyWord) {
        //非条件查询
        if(StringUtils.isEmpty(keyWord)){
            return baseMapper.selectPage(pageParam,null);
        }
        //条件查询
        QueryWrapper<Borrower> borrowerQueryWrapper =new QueryWrapper<>();
        borrowerQueryWrapper.like("name",keyWord)
                .or().like("id_card",keyWord)
                .or().like("mobile",keyWord)
                .orderByDesc("id");
        return baseMapper.selectPage(pageParam,borrowerQueryWrapper);
    }
}
