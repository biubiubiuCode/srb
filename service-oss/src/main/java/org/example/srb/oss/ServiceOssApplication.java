package org.example.srb.oss;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
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
@SpringBootApplication
@ComponentScan({"org.example.srb", "org.example.common"})
public class ServiceOssApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceOssApplication.class, args);
    }
}
