package org.example.srb.core.controller.api;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.example.common.exception.Assert;
import org.example.common.result.R;
import org.example.common.result.ResponseEnum;
import org.example.common.util.RegexValidateUtils;
import org.example.srb.base.util.JwtUtils;
import org.example.srb.core.pojo.vo.LoginVO;
import org.example.srb.core.pojo.vo.RegisterVO;
import org.example.srb.core.pojo.vo.UserIndexVO;
import org.example.srb.core.pojo.vo.UserInfoVO;
import org.example.srb.core.service.UserInfoService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 用户基本信息 前端控制器
 * </p>
 *
 * @author wendao
 * @since 2024-04-01
 */
@Api(tags = "会员接口")
@Slf4j
//@CrossOrigin
@RestController
@RequestMapping("/api/core/userInfo")
public class UserInfoController {
    @Resource
    private UserInfoService userInfoService;

    @Resource
    private RedisTemplate redisTemplate;

    @ApiOperation("会员注册")
    @PostMapping("/register")
    public R register(@RequestBody RegisterVO registerVO){

        String mobile = registerVO.getMobile();
        String password = registerVO.getPassword();
        String code = registerVO.getCode();

        //MOBILE_NULL_ERROR(-202, "手机号不能为空"),
        Assert.notEmpty(mobile, ResponseEnum.MOBILE_NULL_ERROR);
        //MOBILE_ERROR(-203, "手机号不正确"),
        Assert.isTrue(RegexValidateUtils.checkCellphone(mobile), ResponseEnum.MOBILE_ERROR);
        //PASSWORD_NULL_ERROR(-204, "密码不能为空"),
        Assert.notEmpty(password, ResponseEnum.PASSWORD_NULL_ERROR);
        //CODE_NULL_ERROR(-205, "验证码不能为空"),
        Assert.notEmpty(code, ResponseEnum.CODE_NULL_ERROR);

        //校验验证码
        String codeGen = (String)redisTemplate.opsForValue().get("srb:sms:code:" + mobile);
        //CODE_ERROR(-206, "验证码不正确"),
        Assert.equals(code, codeGen, ResponseEnum.CODE_ERROR);

        //注册
        userInfoService.register(registerVO);
        return R.ok().message("注册成功");
    }

    @ApiOperation("会员登录")
    @PostMapping("/login")
    public R login(@RequestBody LoginVO loginVO, HttpServletRequest request) {

        String mobile = loginVO.getMobile();
        String password = loginVO.getPassword();
        Assert.notEmpty(mobile, ResponseEnum.MOBILE_NULL_ERROR);
        Assert.notEmpty(password, ResponseEnum.PASSWORD_NULL_ERROR);

        String ip = request.getRemoteAddr();
        UserInfoVO userInfoVO = userInfoService.login(loginVO, ip);//service里生成token

        return R.ok().data("userInfo", userInfoVO);
    }

    @ApiOperation("校验令牌")
    @GetMapping("/checkToken")
    public R checkToken(HttpServletRequest request) {

        String token = request.getHeader("token");
        boolean result = JwtUtils.checkToken(token);

        if(result){
            return R.ok();
        }else{
            //LOGIN_AUTH_ERROR(-211, "未登录"),
            return R.setResult(ResponseEnum.LOGIN_AUTH_ERROR);
        }
    }

    @ApiOperation("校验手机号是否注册")
    @GetMapping("/checkMobile/{mobile}")
    public boolean checkMobile(@PathVariable("mobile") String mobile){
        return userInfoService.checkMobile(mobile);
    }

    @ApiOperation("获取个人空间（首页）用户信息")
    @GetMapping("/auth/getIndexUserInfo")
    public R getIndexUserInfo(HttpServletRequest request) {
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        UserIndexVO userIndexVO = userInfoService.getIndexUserInfo(userId);
        return R.ok().data("userIndexVO", userIndexVO);
    }
}

