@echo off
title Web Server - Blood Bank Management System
echo ========================================================
echo   Launching Blood Bank Web Server (Java + MySQL)
echo ========================================================

if not exist bin (
    echo 'bin' directory not found. Running compilation first...
    call compile.bat
)

echo Starting Java Web Server on port 8080...
start "" http://localhost:8080
java -cp "bin;.;lib/*;lib/mysql-connector-j-*.jar;lib/mysql-connector-java-*.jar" com.bbms.web.WebServer

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Server exited with code %ERRORLEVEL%.
    pause
)
