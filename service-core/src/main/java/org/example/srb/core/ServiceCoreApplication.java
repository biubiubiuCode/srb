package org.example.srb.core;

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
 * @Create 2024/04/01 19:40
 **/
@SpringBootApplication
@ComponentScan({"org.example.srb", "org.example.common"})
public class ServiceCoreApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceCoreApplication.class, args);
    }
}
