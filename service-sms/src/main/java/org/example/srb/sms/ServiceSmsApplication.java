package org.example.srb.sms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * ClassName: org.example.srb.core.ServiceCoreApplication
 * Description:
 * $
 *
 * @author wendao
 * @version 1.0
 * @since 2024/04/01 19:40
 **/
@EnableFeignClients
@SpringBootApplication
@ComponentScan({"org.example.srb", "org.example.common"})
public class ServiceSmsApplication {
    public static void main(String[] args) {

        try {
            SpringApplication.run(ServiceSmsApplication.class, args);
        } catch (Throwable e) {
             e.printStackTrace();
        }
    }
}
