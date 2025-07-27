@echo off
echo 🔥 启动 Spring Boot 热重载开发模式...
echo.

REM 设置环境变量
set SPRING_PROFILES_ACTIVE=local
set SPRING_DEVTOOLS_RESTART_ENABLED=true

echo 📋 当前配置:
echo   - Profile: %SPRING_PROFILES_ACTIVE%
echo   - DevTools: %SPRING_DEVTOOLS_RESTART_ENABLED%
echo   - Port: 8024
echo.

echo 🚀 正在启动应用...
mvn spring-boot:run -Dspring-boot.run.profiles=local

pause
