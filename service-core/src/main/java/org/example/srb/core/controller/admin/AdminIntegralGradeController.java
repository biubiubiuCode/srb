package org.example.srb.core.controller.admin;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.example.common.exception.Assert;
import org.example.common.result.R;
import org.example.common.result.ResponseEnum;
import org.example.srb.core.pojo.entity.IntegralGrade;
import org.example.srb.core.service.IntegralGradeService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author wendao
 * @since 2024-04-01
 **/
@Api(tags = "积分登记管理")
@CrossOrigin //跨域
@RestController
@Slf4j
@RequestMapping("/admin/core/integralGrade")
public class AdminIntegralGradeController {
    @Resource
    private IntegralGradeService integralGradeService;

    @ApiOperation("积分等级列表")
    @GetMapping("/list")
    public R listAll(){
//        log.info("info 级别的日志输出");
//        log.error("error级别的日志输出");
//        log.warn("WARN级别的日志输出");
        List<IntegralGrade> list = integralGradeService.list();
        return R.ok().data("list", list);
    }
    @ApiOperation(value = "根据id删除积分等级", notes = "逻辑删除")
    @DeleteMapping("/remove/{id}")
    public R removeById(
            @ApiParam(value = "数据id", required = true, example = "1")
            @PathVariable Long id){
        boolean result = integralGradeService.removeById(id);
        if(result){
            //return R.setResult(ResponseEnum.UPLOAD_ERROR);
            return R.ok().message("删除成功");
        }else{
            return R.error().message("删除失败");
        }
    }

    @ApiOperation("新增积分等级")
    @PostMapping("/save")
    public R save(
            @ApiParam(value = "积分等级对象", required = true)
            @RequestBody IntegralGrade integralGrade){
        //贷款额度为空则抛出自定义异常
        //如果借款额度为空就手动抛出一个自定义的异常！
        /*if(integralGrade.getBorrowAmount()==null){
            //BORROW_AMOUNT_NULL_ERROR(-201, "借款额度不能为空"),
            throw new BusinessException(ResponseEnum.BORROW_AMOUNT_NULL_ERROR);
        }*/
        Assert.notNull(integralGrade.getBorrowAmount(),ResponseEnum.BORROW_AMOUNT_NULL_ERROR);
        boolean result = integralGradeService.save(integralGrade);
        if (result) {
            return R.ok().message("保存成功");
        } else {
            return R.error().message("保存失败");
        }
    }

    @ApiOperation("根据id获取积分等级")
    @GetMapping("/get/{id}")
    public R getById(
            @ApiParam(value = "数据id", required = true, example = "1")
            @PathVariable Long id
    ){
        IntegralGrade integralGrade = integralGradeService.getById(id);
        if(integralGrade != null){
            return R.ok().data("record", integralGrade);
        }else{
            return R.error().message("数据不存在");
        }
    }
    @ApiOperation("更新积分等级")
    @PutMapping("/update")
    public R updateById(
            @ApiParam(value = "积分等级对象", required = true)
            @RequestBody IntegralGrade integralGrade){
        boolean result = integralGradeService.updateById(integralGrade);
        if(result){
            return R.ok().message("修改成功");
        }else{
            return R.error().message("修改失败");
        }
    }
}