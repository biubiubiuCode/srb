package org.example.srb.core.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.example.common.result.R;
import org.example.srb.core.pojo.entity.Borrower;
import org.example.srb.core.pojo.vo.BorrowerApprovalVO;
import org.example.srb.core.pojo.vo.BorrowerDetailVO;
import org.example.srb.core.service.BorrowerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author wendao
 * @since 2024-04-14
 **/
@Api(tags = "借款人管理")
@RestController
@RequestMapping("/admin/core/borrower")
public class AdminBorrowerController {
    @Resource
    private BorrowerService borrowerService;

    @ApiOperation("获取借款人分页列表")
    @GetMapping("/list/{page}/{limit}")
    public R listPage(
            @ApiParam(value = "当前页码",required = true)
            @PathVariable("page")
            Long page,
            @ApiParam(value = "每页记录数",required = true)
            @PathVariable("limit")
            Long limit,
            @ApiParam(value = "查询关键字",required = false)
            @RequestParam(value = "keyWord",defaultValue = "")
            String keyWord
    ){
        //这里的@RequestParam其实是可以省略的，但是在目前的swagger版本中（2.9.2）不能省略，
        //否则默认将没有注解的参数解析为body中的传递的数据
        Page<Borrower> pageParam = new Page<>(page,limit);
        IPage<Borrower> pageModel=borrowerService.listPage(pageParam,keyWord);
        return R.ok().data("pageModel",pageModel);
    }

    @ApiOperation("获取借款人信息")
    @GetMapping("/show/{id}")
    public R show(
            @ApiParam(value = "借款人id", required = true)
            @PathVariable Long id) {
        BorrowerDetailVO borrowerDetailVO = borrowerService.getBorrowerDetailVOById(id);
        return R.ok().data("borrowerDetailVO", borrowerDetailVO);
    }

    @ApiOperation("借款额度审批")
    @PostMapping("/approval")
    public R approval(@RequestBody BorrowerApprovalVO borrowerApprovalVO) {
        borrowerService.approval(borrowerApprovalVO);
        return R.ok().message("审批完成");
    }

}
