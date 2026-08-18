@echo off
cd /d "%~dp0"

:: 自动查找 Java 17+
set "JAVA_CMD="

:: 1) 尝试 JAVA_HOME
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" (
        set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
    )
)

:: 2) 尝试 PATH 中的 java
if not defined JAVA_CMD (
    where java >nul 2>&1
    if %errorlevel% equ 0 set "JAVA_CMD=java"
)

:: 3) 常见安装位置
if not defined JAVA_CMD (
    for %%d in (
        "C:\jdk-17.0.1"
        "C:\Program Files\Java\jdk-17"
        "C:\Program Files\Eclipse Adoptium\jdk-17.*"
    ) do (
        if exist "%%~d\bin\java.exe" (
            set "JAVA_CMD=%%~d\bin\java.exe"
            set "JAVA_HOME=%%~d"
        )
    )
)

if not defined JAVA_CMD (
    echo [ERROR] 未找到 Java 17+，请安装 JDK 17 并设置 JAVA_HOME
    pause
    exit /b 1
)

echo JAVA_HOME=%JAVA_HOME%
echo JAVA: %JAVA_CMD%
%JAVA_CMD% --version

:: 编码设置（如需代理请取消下方注释）
set JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8

echo.
echo ============================================
echo   Starting Smart Travel AI Assistant...
echo   Open http://localhost:8080
echo ============================================
echo.

call mvnw.cmd spring-boot:run
pause
