package com.myAI.myAI.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        //设置允许跨域的路径
        registry.addMapping("/**")
                //设置允许跨域请求的域名
                //当**Credentials为true时，**Origin不能为星号，需为具体的ip地址【如果接口不带cookie,ip无需设成具体ip】
                .allowedOrigins(
                        "http://192.168.43.1:5173",
                        "http://localhost:8000",
                        "http://localhost:5174",
                        "http://localhost:5173",
                        "http://localhost:8077",
                        "http://127.0.0.1:9527",
                        "http://127.0.0.1:8082",
                        "http://127.0.0.1:8083",
                        "http://192.168.31.230:5173",
                        "http://47.119.128.91:5173")
                //是否允许证书 不再默认开启
                .allowCredentials(true)
                //设置允许的方法
                .allowedMethods("*")
                //跨域允许时间
                .maxAge(3600);
    }
}
