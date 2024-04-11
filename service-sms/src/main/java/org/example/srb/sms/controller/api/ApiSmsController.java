package org.example.srb.sms.controller.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.example.common.exception.Assert;
import org.example.common.result.R;
import org.example.common.result.ResponseEnum;
import org.example.common.util.RandomUtils;
import org.example.common.util.RegexValidateUtils;
import org.example.srb.sms.client.CoreUserInfoClient;
import org.example.srb.sms.service.SmsService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author wendao
 * @since 2024-04-08
 **/


@Api(tags = "短信管理")
@CrossOrigin //跨域
@RestController
@Slf4j
@RequestMapping("/api/sms")
public class ApiSmsController {
    @Resource
    private SmsService smsService;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private CoreUserInfoClient coreUserInfoClient;

    @ApiOperation("获取验证码")
    @GetMapping("/send/{mobile}")
    public R send(
            @ApiParam(value = "手机号", required = true)
            @PathVariable String mobile) {

        //MOBILE_NULL_ERROR(-202, "手机号不能为空"),
        Assert.notEmpty(mobile, ResponseEnum.MOBILE_NULL_ERROR);
        //MOBILE_ERROR(-203, "手机号不正确"),
        Assert.isTrue(RegexValidateUtils.checkCellphone(mobile), ResponseEnum.MOBILE_ERROR);

        //手机号是否注册
        boolean result = coreUserInfoClient.checkMobile(mobile);
        log.info("手机号"+mobile+"是否被注册: result="+result);
        Assert.isTrue(result == false, ResponseEnum.MOBILE_EXIST_ERROR);

        //生成验证码 4位随机数
        String code = RandomUtils.getFourBitRandom();
        //组装短信模板参数
        Map<String, Object> param = new HashMap<>();
        param.put("code", code);
        //发送短信
//        smsService.send(mobile, SmsProperties.TEMPLATE_CODE, param);

        //将验证码存入redis
        redisTemplate.opsForValue().set("srb:sms:code:" + mobile, code, 5, TimeUnit.MINUTES);

        return R.ok().message("短信发送成功");
    }


}