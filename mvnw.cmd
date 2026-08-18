@REM ----------------------------------------------------------------------------
@REM Maven Wrapper for Windows
@REM ----------------------------------------------------------------------------
@echo off
setlocal enabledelayedexpansion

set MAVEN_PROJECTBASEDIR=%CD%
set MAVEN_OPTS=%MAVEN_OPTS% -Dfile.encoding=UTF-8

if not defined JAVA_HOME (
  echo ERROR: JAVA_HOME not set
  exit /b 1
)

set JAVA_EXE="%JAVA_HOME%\bin\java.exe"
set WRAPPER_JAR="%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"

if not exist %WRAPPER_JAR% (
  echo ERROR: maven-wrapper.jar not found at %WRAPPER_JAR%
  exit /b 1
)

%JAVA_EXE% ^
  %MAVEN_OPTS% ^
  -classpath %WRAPPER_JAR% ^
  -Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR% ^
  org.apache.maven.wrapper.MavenWrapperMain ^
  %*
