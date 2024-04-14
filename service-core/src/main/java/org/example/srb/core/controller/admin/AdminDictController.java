package org.example.srb.core.controller.admin;

import com.alibaba.excel.EasyExcel;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.example.common.exception.BusinessException;
import org.example.common.result.R;
import org.example.common.result.ResponseEnum;
import org.example.srb.core.pojo.dto.ExcelDictDTO;
import org.example.srb.core.pojo.entity.Dict;
import org.example.srb.core.service.DictService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.List;

/**
 * @author wendao
 * @since 2024-04-06
 **/
@Api(tags = "数据字典管理")
//@CrossOrigin //跨域
@RestController
@Slf4j
@RequestMapping("/admin/core/dict")
public class AdminDictController {
    @Resource
    private DictService dictService;

    @ApiOperation("Excel批量导入数据字典")
    @PostMapping("/import")
    public R bacthImport(
            @ApiParam(value = "Excel数据字典文件", required = true)
            @RequestParam("file")MultipartFile files
            ){
        try {
            InputStream inputStream = files.getInputStream();
            dictService.importData(inputStream);
            return R.ok().message("批量导入成功");
        } catch (Exception e) {
            //UPLOAD_ERROR(-103, "文件上传错误"),
            throw new BusinessException(ResponseEnum.UPLOAD_ERROR, e);
        }
    }

    @ApiOperation("Excel数据的导出")
    @GetMapping("/export")
    public void export(HttpServletResponse response){

        try {
            //使用swagger测试会导致各种问题，直接用浏览器或者用postman
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
            String fileName = URLEncoder.encode("mydict", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            EasyExcel.write(response.getOutputStream(), ExcelDictDTO.class).sheet("数据字典").doWrite(dictService.listDictData());

        } catch (IOException e) {
            //EXPORT_DATA_ERROR(104, "数据导出失败"),
            throw  new BusinessException(ResponseEnum.EXPORT_DATA_ERROR, e);
        }
    }

    @ApiOperation("(用于懒加载)字典展示之根据id获取字节数据列表")
    @GetMapping("/listByParentId/{parentId}")
    public R listByParentId(
            @ApiParam(value = "上一级节点的Id", required = true)
            @PathVariable("parentId") Long id
    ){
        List<Dict> dictList =dictService.listByParentId(id);
        return R.ok().data("list",dictList);
    }
}
