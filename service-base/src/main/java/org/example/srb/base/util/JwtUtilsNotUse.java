package org.example.srb.base.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.example.srb.base.pojo.JwtProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

/**
 * ClassName: org.none.utils.JwtUtils
 * Description:
 * Jwt令牌工具类
 * @author wendao
 * @version 1.0
 * @Create 2024/03/20 20:25
 **/

@Component //当前类对象由Spring创建和管理
public class JwtUtilsNotUse {
    private static JwtProperties jwtProperties;
    //通过有参构造对静态变量注入
    @Autowired
    public JwtUtilsNotUse(JwtProperties jwtProperties){
        JwtUtilsNotUse.jwtProperties = jwtProperties;
    }
    /**
     * 生成 Jwt 令牌
     * @param claims Jwt 第二部分的 payload 中储存的内容
     * @return java.lang.String
     * @author Administrator
     * @date 2024/3/20 0020 20:29
    */
    public static String generateJwt(Map<String,Object> claims){
        String jwt = Jwts.builder().
                setClaims(claims)
//                .addClaims(claims)
                .signWith(SignatureAlgorithm.HS256, jwtProperties.getSignKey())
                .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getExpire()))
                .compact();
        return jwt;
    }
    /**
     * 解析 JWT 令牌
     * @param jwt
     * @return io.jsonwebtoken.Claims
     * @author Administrator
     * @date 2024/3/20 0020 20:33
    */
    public static Claims parseJWT(String jwt){
        Claims claims = Jwts.parser()
                .setSigningKey(jwtProperties.getSignKey())
                .parseClaimsJws(jwt)
                .getBody();
        return claims;
    }

}
