package org.example.srb.core.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.example.srb.core.listener.ExcelDictDTOListener;
import org.example.srb.core.mapper.DictMapper;
import org.example.srb.core.pojo.dto.ExcelDictDTO;
import org.example.srb.core.pojo.entity.Dict;
import org.example.srb.core.service.DictService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 数据字典 服务实现类
 * </p>
 *
 * @author wendao
 * @since 2024-04-01
 */
@Slf4j
@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {

//    @Resource
//    private DictMapper dictMapper;

    //注入redis
    @Resource
    private RedisTemplate redisTemplate;
    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void importData(InputStream inputStream) {
//        EasyExcel.read(inputStream, ExcelDictDTO.class, new ExcelDictDTOListener(dictMapper)).sheet().doRead();
        EasyExcel.read(inputStream, ExcelDictDTO.class, new ExcelDictDTOListener(baseMapper)).sheet().doRead();
        log.info("Excel导入成功");
    }

    @Override
    public List<ExcelDictDTO> listDictData() {

        List<Dict> dicts = baseMapper.selectList(null);
        //Dict转换成ExcelDictDTO
        ArrayList<ExcelDictDTO> excelDictDTOS = new ArrayList<>(dicts.size());
        dicts.forEach(dict -> {
            //数据转换
            ExcelDictDTO edd =new ExcelDictDTO();
            BeanUtils.copyProperties(dict,edd);
            //存储
            excelDictDTOS.add(edd);
        });
        log.info("Excel导出成功");
        return excelDictDTOS;
    }

    @Override
    public List<Dict> listByParentId(Long parentId) {
        List<Dict> dictList=null;
        try {
            //1.先从redis查询
            redisTemplate.opsForValue().get("srb:core:dictList:"+parentId);
            if (dictList!=null){
                log.info("从redis中取值");
                return dictList;
            }
        } catch (Exception e) {
            log.error("redis服务器异常"+  ExceptionUtils.getStackTrace(e));//此处不抛出异常，继续执行后面的代码
        }

        //2.没有再从数据库查询
        log.info("从数据库中取值");
        dictList = baseMapper.selectList(new QueryWrapper<Dict>()
                .eq("parent_id",parentId)
        );
        dictList.forEach(dict -> {
            //如果有子节点，则是非叶子节点
            boolean hasChildren = this.hasChildren(dict.getId());
            dict.setHasChildren(hasChildren);
        });
        try {
            //3.将数据存入redis
            log.info("将数据存入redis");
            redisTemplate.opsForValue().set("srb:core:dictList:"+ parentId, dictList,5, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.info("redis服务器异常"+ExceptionUtils.getStackTrace(e));//此处不抛出异常，继续执行后面的代码
        }
        //4.返回结果
        return dictList;
    }
    /**
     * 判断该节点是否有子节点
     */
    private boolean hasChildren(Long  parentId){
        QueryWrapper<Dict> qw = new QueryWrapper<Dict>().eq("parent_id", parentId);
        Integer count =baseMapper.selectCount(qw);
        if (count.intValue()>0){
            return true;
        }
        return false;
    }

}
