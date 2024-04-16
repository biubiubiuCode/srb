package org.example.srb.core.mapper;

import org.example.srb.core.pojo.entity.BorrowInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 * 借款信息表 Mapper 接口
 * </p>
 *
 * @author wendao
 * @since 2024-04-01
 */
public interface BorrowInfoMapper extends BaseMapper<BorrowInfo> {
    /**
     * 获取所有借款信息
     * @param null
     * @return
     * @author Administrator
     * @date 2024/4/16 0016 16:06
    */
    List<BorrowInfo> selectBorrowInfoList();
}
