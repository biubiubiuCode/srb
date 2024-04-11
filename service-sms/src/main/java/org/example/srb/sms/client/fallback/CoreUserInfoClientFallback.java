package org.example.srb.sms.client.fallback;

import lombok.extern.slf4j.Slf4j;
import org.example.srb.sms.client.CoreUserInfoClient;
import org.springframework.stereotype.Service;

/**
 * @author wendao
 * @since 2024-04-11
 **/
@Slf4j
@Service
public class CoreUserInfoClientFallback implements CoreUserInfoClient {
    @Override
    public boolean checkMobile(String mobile) {
        log.error("远程调用失败，服务熔断");
        return false;//默认手机号视为未注册
    }
}
