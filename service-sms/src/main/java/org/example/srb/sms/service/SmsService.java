package org.example.srb.sms.service;

import java.util.Map;

/**
 *  短信发送业务
 *  @author wendao
 * @since 2024-04-08
 **/
public interface SmsService {
    void send(String mobile, String templateCode, Map<String,Object> param);
}
