package org.example.srb.core.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.srb.core.mapper.DictMapper;
import org.example.srb.core.pojo.dto.ExcelDictDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * 监听器读 excel数据
 * @author wendao
 * @since 2024-04-06
 **/
@NoArgsConstructor
@Slf4j
public class ExcelDictDTOListener extends AnalysisEventListener<ExcelDictDTO> {

    //建数据列表进行缓存
    private List<ExcelDictDTO> list =  new ArrayList<>();

    //每隔5条记录批量存一次
    static final int BATCH_COUNT = 5;

    private DictMapper dictMapper;

    public ExcelDictDTOListener(DictMapper dictMapper) {
        this.dictMapper = dictMapper;
    }

    @Override
    public void invoke(ExcelDictDTO data, AnalysisContext analysisContext) {
        log.info("解析到一条数据；{}", data);
        //缓存,一般3000条
        list.add(data);
        // 达到BATCH_COUNT了，需要去存储一次数据库，防止数据几万条数据在内存，容易OOM
        if (list.size() >= BATCH_COUNT){
            //调用mapper层接口的save方法
            saveData();
            // 存储完成清理 list
            list.clear();
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        //当最后剩余数据量不足BATCH_COUNT,在此处进行保存
        saveData();

        log.info("所有数据解析完成");
    }

    private void saveData(){
        log.info("{}条数据存储到数据库...",list.size());
        //调用mapper层接口的save方法
        dictMapper.insertBatch(list);
        log.info("{}条数据存储到数据库成功",list.size());
    }
}
