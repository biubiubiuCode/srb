package org.example.srb.base.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * ClassName: org.none.pojo.JwtUtils
 * Description:
 * 管理jwt解密以及时间限制$
 *
 * @author wendao
 * @version 1.0
 * @Create 2024/03/20 20:45
 **/
@NoArgsConstructor
@AllArgsConstructor
@Data
@ConfigurationProperties(prefix = "jwt.set")
@Component
public class JwtProperties {
    //密钥
    //@Value("${jwt.set.signkey}")
    private String signKey;
    //过期时长
    //@Value("${jwt.set.expire}")
    private Long expire;
}
