@echo off
title Run - Blood Bank Management System
echo ========================================================
echo   Launching Blood Bank Management System (Java Swing)
echo ========================================================

if not exist bin (
    echo 'bin' directory not found. Running compilation first...
    call compile.bat
)

echo Starting application...
java -cp "bin;.;lib/*;lib/mysql-connector-j-*.jar;lib/mysql-connector-java-*.jar" com.bbms.Main

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Application exited with code %ERRORLEVEL%.
    pause
)
