package com.myAI.myAI.config;

import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.devtools.restart.RestartScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * DevTools 热重载配置
 */
@Configuration
@Profile({"local", "dev"}) // 只在本地和开发环境启用
@Slf4j
public class DevToolsConfig {

    /**
     * 配置热重载监听器
     */
    @Bean
//    @RestartScope
    public DevToolsListener devToolsListener() {
        log.info("🔥 DevTools 热重载已启用");
        return new DevToolsListener();
    }

    /**
     * DevTools 监听器
     */
    public static class DevToolsListener {
        public DevToolsListener() {
            log.info("🚀 应用热重载监听器已初始化");
        }
    }
}
