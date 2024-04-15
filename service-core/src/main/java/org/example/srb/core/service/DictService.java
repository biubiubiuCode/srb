package org.example.srb.core.service;

import org.example.srb.core.pojo.dto.ExcelDictDTO;
import org.example.srb.core.pojo.entity.Dict;
import com.baomidou.mybatisplus.extension.service.IService;

import java.io.InputStream;
import java.util.List;

/**
 * <p>
 * 数据字典 服务类
 * </p>
 *
 * @author wendao
 * @since 2024-04-01
 */
public interface DictService extends IService<Dict> {
    public void importData(InputStream inputStream);

    public List<ExcelDictDTO> listDictData();

    public List<Dict> listByParentId(Long parentId);

    public List<Dict> findByDictCode(String dictCode);

    String getNameByParentDictCodeAndValue(String dictcode, Integer value);
}
