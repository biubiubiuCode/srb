package org.example.srb.base.util;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.CompressionCodecs;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.example.common.exception.BusinessException;
import org.example.common.result.ResponseEnum;
import org.springframework.util.StringUtils;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.util.Date;
/**
 * Description:
 * 管理jwt以及时间限制$
 *
 * @author wendao
 * @version 1.0
 * @Create 2024/03/20 20:45
 **/
//@Component
public class JwtUtils {
//    private static JwtProperties jwtProperties;

    private static long tokenExpiration = 24*60*60*1000;
    private static String tokenSignKey = "org.example";

    private static Key getKeyInstance(){
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        byte[] bytes = DatatypeConverter.parseBase64Binary(tokenSignKey);
//        byte[] bytes = DatatypeConverter.parseBase64Binary(jwtProperties.getSignKey());
        return new SecretKeySpec(bytes,signatureAlgorithm.getJcaName());
    }

    public static String createToken(Long userId, String userName) {
        String token = Jwts.builder()
                .setSubject("SRB-USER")
                .setExpiration(new Date(System.currentTimeMillis() + tokenExpiration))
//                .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getExpire()))
                .claim("userId", userId)
                .claim("userName", userName)
                .signWith(SignatureAlgorithm.HS512, getKeyInstance())
                .compressWith(CompressionCodecs.GZIP)
                .compact();
        return token;
    }

    /**
     * 判断token是否有效
     * @param token
     * @return
     */
    public static boolean checkToken(String token) {
        if(StringUtils.isEmpty(token)) {
            return false;
        }
        try {
            Jwts.parser().setSigningKey(getKeyInstance()).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    public static Long getUserId(String token) {
        Claims claims = getClaims(token);
        Integer userId = (Integer)claims.get("userId");
        return userId.longValue();
    }

    public static String getUserName(String token) {
        Claims claims = getClaims(token);
        return (String)claims.get("userName");
    }

    public static void removeToken(String token) {
        //jwttoken无需删除，客户端扔掉即可。
    }

    /**
     * 校验token并返回Claims
     * @param token
     * @return
     */
    private static Claims getClaims(String token) {
        if(StringUtils.isEmpty(token)) {
            // LOGIN_AUTH_ERROR(-211, "未登录"),
            throw new BusinessException(ResponseEnum.LOGIN_AUTH_ERROR);
        }
        try {
            Jws<Claims> claimsJws = Jwts.parser().setSigningKey(getKeyInstance()).parseClaimsJws(token);
            Claims claims = claimsJws.getBody();
            return claims;
        } catch (Exception e) {
            throw new BusinessException(ResponseEnum.LOGIN_AUTH_ERROR);
        }
    }
}

