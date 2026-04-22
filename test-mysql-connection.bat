@echo off
chcp 65001 > nul
echo ========================================
echo   MySQL 连接测试脚本
echo ========================================
echo.

echo 检查MySQL服务状态...
sc query MySQL80 | findstr STATE
echo.

echo 请输入您的MySQL root密码（如果没有密码直接按回车）：
set /p PASSWORD="密码: "

echo.
echo 正在测试连接...
if "%PASSWORD%"=="" (
    mysql -u root -e "SELECT '连接成功！' AS Status, VERSION() AS MySQL_Version;" 2>nul
) else (
    mysql -u root -p%PASSWORD% -e "SELECT '连接成功！' AS Status, VERSION() AS MySQL_Version;" 2>nul
)

if %errorlevel% neq 0 (
    echo.
    echo 连接失败！请确认密码是否正确。
    echo.
    echo 常见解决方案：
    echo 1. 打开MySQL Workbench重置密码
    echo 2. 或以管理员身份运行命令提示符，执行：
    echo    mysql -u root -p
    echo    然后输入: ALTER USER 'root'@'localhost' IDENTIFIED BY 'newpassword';
    echo.
)

echo.
pause
