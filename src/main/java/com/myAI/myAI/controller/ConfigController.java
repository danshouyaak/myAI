package com.myAI.myAI.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.HashMap;
import java.util.Map;

/**
 * 配置检查控制器
 */
@RestController
@RequestMapping("/api/config")
public class ConfigController {

    @Autowired
    private Environment environment;

    @Autowired
    private DataSource dataSource;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String datasourceUsername;

    /**
     * 获取当前配置信息
     */
    @GetMapping("/info")
    public Map<String, Object> getConfigInfo() {
        Map<String, Object> config = new HashMap<>();

        // 活动的配置文件
        String[] activeProfiles = environment.getActiveProfiles();
        String[] defaultProfiles = environment.getDefaultProfiles();
        config.put("activeProfiles", activeProfiles);
        config.put("defaultProfiles", defaultProfiles);
        config.put("currentEnvironment", activeProfiles.length > 0 ? activeProfiles[0] : "default");

        // 应用信息
        config.put("applicationName", environment.getProperty("spring.application.name"));
        config.put("serverPort", environment.getProperty("server.port"));

        // 数据源配置
        config.put("datasourceUrl", datasourceUrl);
        config.put("datasourceUsername", datasourceUsername);

        // 实际数据库连接信息
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            config.put("actualDatabaseUrl", metaData.getURL());
            config.put("actualDatabaseUser", metaData.getUserName());
            config.put("databaseProductName", metaData.getDatabaseProductName());
            config.put("databaseProductVersion", metaData.getDatabaseProductVersion());

            // 获取当前数据库名称
            String currentDatabase = connection.getCatalog();
            config.put("currentDatabase", currentDatabase);

            // 环境验证
            config.put("environmentValid", validateEnvironment(activeProfiles.length > 0 ? activeProfiles[0] : "default", currentDatabase));

        } catch (Exception e) {
            config.put("databaseError", e.getMessage());
            config.put("environmentValid", false);
        }

        return config;
    }

    /**
     * 验证环境配置是否正确
     */
    private boolean validateEnvironment(String profile, String currentDatabase) {
        switch (profile) {
            case "local":
                return "my_ai".equals(currentDatabase);
            case "dev":
                return "my_ai_dev".equals(currentDatabase);
            case "test":
                return "my_ai_test".equals(currentDatabase);
            case "prod":
                return "my_ai_prod".equals(currentDatabase);
            default:
                return false;
        }
    }

    /**
     * 测试数据库连接
     */
    @GetMapping("/test-db")
    public Map<String, Object> testDatabase() {
        Map<String, Object> result = new HashMap<>();

        try (Connection connection = dataSource.getConnection()) {
            result.put("connected", true);
            result.put("currentDatabase", connection.getCatalog());
            result.put("url", connection.getMetaData().getURL());
            result.put("message", "数据库连接成功");

            // 检查环境匹配
            String[] activeProfiles = environment.getActiveProfiles();
            String currentProfile = activeProfiles.length > 0 ? activeProfiles[0] : "default";
            String currentDatabase = connection.getCatalog();
            boolean environmentMatch = validateEnvironment(currentProfile, currentDatabase);

            result.put("environmentMatch", environmentMatch);
            result.put("currentProfile", currentProfile);
            result.put("expectedDatabase", getExpectedDatabase(currentProfile));

        } catch (Exception e) {
            result.put("connected", false);
            result.put("error", e.getMessage());
            result.put("message", "数据库连接失败");
        }

        return result;
    }

    /**
     * 获取环境配置建议
     */
    @GetMapping("/environment-guide")
    public Map<String, Object> getEnvironmentGuide() {
        Map<String, Object> guide = new HashMap<>();

        // 环境配置说明
        Map<String, String> environments = new HashMap<>();
        environments.put("local", "本地开发环境 - 数据库: my_ai");
        environments.put("dev", "开发环境 - 数据库: my_ai_dev");
        environments.put("test", "测试环境 - 数据库: my_ai_test");
        environments.put("prod", "生产环境 - 数据库: my_ai_prod");

        guide.put("environments", environments);

        // 切换方法
        Map<String, String> switchMethods = new HashMap<>();
        switchMethods.put("配置文件", "修改 application.yml 中的 spring.profiles.active");
        switchMethods.put("启动参数", "java -jar app.jar --spring.profiles.active=local");
        switchMethods.put("环境变量", "export SPRING_PROFILES_ACTIVE=local");
        switchMethods.put("IDE配置", "VM options: -Dspring.profiles.active=local");

        guide.put("switchMethods", switchMethods);

        // 当前状态
        String[] activeProfiles = environment.getActiveProfiles();
        guide.put("currentProfile", activeProfiles.length > 0 ? activeProfiles[0] : "default");

        return guide;
    }

    /**
     * 获取期望的数据库名称
     */
    private String getExpectedDatabase(String profile) {
        switch (profile) {
            case "local":
                return "my_ai";
            case "dev":
                return "my_ai_dev";
            case "test":
                return "my_ai_test";
            case "prod":
                return "my_ai_prod";
            default:
                return "unknown";
        }
    }
}
