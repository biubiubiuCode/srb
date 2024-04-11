package org.example.srb.core.mapper;

import org.example.srb.core.pojo.dto.ExcelDictDTO;
import org.example.srb.core.pojo.entity.Dict;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 * 数据字典 Mapper 接口
 * </p>
 *
 * @author wendao
 * @since 2024-04-01
 */
public interface DictMapper extends BaseMapper<Dict> {
    public void insertBatch(List<ExcelDictDTO> list);

}
