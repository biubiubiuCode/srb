package org.example.srb.core.controller.api;

import com.alibaba.fastjson.JSON;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.example.common.result.R;
import org.example.srb.base.util.JwtUtils;
import org.example.srb.core.hfb.RequestHelper;
import org.example.srb.core.pojo.vo.UserBindVO;
import org.example.srb.core.service.UserBindService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author wendao
 * @since 2024-04-12
 **/
@Api(tags = "会员账号绑定")
@RestController
@RequestMapping("/api/core/userBind")
@Slf4j
public class UserBindController {

    @Resource
    private UserBindService userBindService;
    //创建表单返回，以便向第三方提交
    @ApiOperation("账户绑定提交数据")
    @PostMapping("/auth/bind")
    public R bind(@RequestBody UserBindVO userBindVO, HttpServletRequest request) {
        //获取token,校验确认用户已登陆
        String token = request.getHeader("token");
        //从token获取userId
        Long userId = JwtUtils.getUserId(token);
        //根据user_Id进行账户绑定,生成动态表单的字符串
        String formStr = userBindService.commitBindUser(userBindVO, userId);
        return R.ok().data("formStr", formStr);
    }

    //来自第三方信息触发的回调
    @ApiOperation("账户绑定异步回调")
    @PostMapping("/notify")
    public String notify(HttpServletRequest request) {
        //把回调返回的表格转化为<S tring,Object>
        Map<String, Object> paramMap = RequestHelper.switchMap(request.getParameterMap());
        log.info("用户账号绑定异步回调：" + JSON.toJSONString(paramMap));

        //校验签名
        if(!RequestHelper.isSignEquals(paramMap)) {
            log.error("用户账号绑定异步回调签名错误：" + JSON.toJSONString(paramMap));
            return "fail";
        }

        //修改绑定状态
        userBindService.notify(paramMap);
        // 对回调的成功的响应，约定为“success”
        return "success";
    }
}
