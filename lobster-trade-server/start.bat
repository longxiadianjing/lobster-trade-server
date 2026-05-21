@echo off
chcp 65001 >nul
title 龙虾道具交易平台 - 后端服务

echo ================================================
echo   龙虾道具交易平台 - 后端启动脚本
echo ================================================
echo.

cd /d "%~dp0"

echo [1/3] 加载环境变量...
for /f "usebackq tokens=1,2 delims==" %%a in ("%cd%\.env") do (
    set "%%a=%%b"
)
echo       环境变量加载完成

echo.
echo [2/3] 检查 Java...
java -version 2>&1 | findstr /i "version" >nul
if errorlevel 1 (
    echo       [错误] 未检测到 Java，请先安装 JDK 17+
    pause
    exit /b 1
)
echo       Java 检测正常

echo.
echo [3/3] 启动服务...
echo.
echo ================================================
echo   启动参数：
echo   - 端口: %SERVER_PORT%
echo   - 数据库: %DB_HOST%:%DB_PORT%/%DB_NAME%
echo   - Redis: %REDIS_HOST%:%REDIS_PORT%
echo ================================================
echo.

java -jar target\lobster-trade-server-1.0.0-SNAPSHOT.jar ^
    --spring.profiles.active=dev ^
    -DDB_HOST=%DB_HOST% ^
    -DDB_PORT=%DB_PORT% ^
    -DDB_NAME=%DB_NAME% ^
    -DDB_USERNAME=%DB_USERNAME% ^
    -DDB_PASSWORD=%DB_PASSWORD% ^
    -DJWT_SECRET=%JWT_SECRET% ^
    -DREDIS_HOST=%REDIS_HOST% ^
    -DREDIS_PORT=%REDIS_PORT% ^
    -DLOG_LEVEL=%LOG_LEVEL%

pause