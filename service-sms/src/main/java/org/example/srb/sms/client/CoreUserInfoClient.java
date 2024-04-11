package org.example.srb.sms.client;

import org.example.srb.sms.client.fallback.CoreUserInfoClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author wendao
 * @since 2024-04-10
 **/
@FeignClient(value = "service-core",fallback = CoreUserInfoClientFallback.class)
public interface CoreUserInfoClient {
    @GetMapping("/api/core/userInfo/checkMobile/{mobile}")
    boolean checkMobile(@PathVariable("mobile") String mobile);
}
