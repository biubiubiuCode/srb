package org.example.srb.core.controller.admin;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.example.common.result.R;
import org.example.srb.core.pojo.entity.UserLoginRecord;
import org.example.srb.core.service.UserLoginRecordService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author wendao
 * @since 2024-04-10
 **/
@Api(tags = "会员登录日志接口")
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/admin/core/userLoginRecord")
public class AdminUserLoginRecordController {
    @Resource
    private UserLoginRecordService userLoginRecordService;

    @ApiOperation("获取会员登录日志列表")
    @GetMapping("/listTop50/{userId}")
    public R listTop50(
            @ApiParam(value = "用户id", required = true)
            @PathVariable Long userId) {
        List<UserLoginRecord> userLoginRecordList = userLoginRecordService.listTop50(userId);
        return R.ok().data("list", userLoginRecordList);
    }
}
