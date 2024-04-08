package org.example.srb.sms;

import org.example.srb.sms.util.SmsProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author wendao
 * @since 2024-04-08
 **/
@SpringBootTest
/*
* <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
  </dependency>
  * parent设置的test依赖无 junit-ventige-engine部分，没有上下文环境，无法成功注入bean
  * 有了@RunWith(SpringRunner.class)bean才能实例化到spring容器中，自动注入才能生效
* */

@RunWith(SpringRunner.class)
public class UtilsTest {
    @Test
    public void testProperties(){
        System.out.println(SmsProperties.KEY_ID);
        System.out.println(SmsProperties.KEY_SECRET);
        System.out.println(SmsProperties.REGION_ID);
    }
}
