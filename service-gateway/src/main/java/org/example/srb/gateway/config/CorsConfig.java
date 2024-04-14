package org.example.srb.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * 跨域配置
 * 与@CrossOrigin冲突，需要注释掉此注释
 * @author wendao
 * @since 2024-04-11
 **/
@Configuration
public class CorsConfig {
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true); //是否允许携带cookie
//        config.addAllowedOrigin("*"); //可接受的域，是一个具体域名或者*（代表任意域名）
        config.addAllowedOriginPattern("*"); //因为允许cookies,addAllowedOrigin("*")换成addAllowedOriginPattern("*")
        config.addAllowedHeader("*"); //允许携带的头
        config.addAllowedMethod("*"); //允许访问的方式

        //基于url跨域，reactive包下
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        //任意url都执行跨域配置
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }

//    @Bean
//    public FilterRegistrationBean corsFilter() {
//        //1.添加CORS配置信息
//        CorsConfiguration config = new CorsConfiguration();
//        //1) 允许的域,不要写*，否则cookie就无法使用了
//        //config.addAllowedOrigin("http://manage.leyou.com");
//        //config.addAllowedOrigin("http://www.leyou.com");
//        config.addAllowedOrigin("*");
//        //2) 是否发送Cookie信息
//        config.setAllowCredentials(true);
//        //3) 允许的请求方式
//        config.addAllowedMethod("OPTIONS");
//        config.addAllowedMethod("HEAD");
//        config.addAllowedMethod("GET");
//        config.addAllowedMethod("PUT");
//        config.addAllowedMethod("POST");
//        config.addAllowedMethod("DELETE");
//        config.addAllowedMethod("PATCH");
//        config.setMaxAge(3600L);
//        // 4）允许的头信息
//        config.addAllowedHeader("*");
//
//        //2.添加映射路径，我们拦截一切请求
//        UrlBasedCorsConfigurationSource configSource = new UrlBasedCorsConfigurationSource();
//        configSource.registerCorsConfiguration("/**", config);
//
//        //3.返回新的CorsFilter.
//        //return new CorsFilter(configSource);
//
//        FilterRegistrationBean bean = new FilterRegistrationBean(new CorsFilter((CorsConfigurationSource) configSource));
//        bean.setOrder(0);
//        return bean;
//    }
}
