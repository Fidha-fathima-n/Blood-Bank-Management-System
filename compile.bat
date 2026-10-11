@echo off
title Compile - Blood Bank Management System
echo ========================================================
echo   Compiling Blood Bank Management System (Java OOPS)
echo ========================================================

if not exist bin mkdir bin

echo Compiling Java source files (Desktop Swing + Web Server)...
javac -d bin -cp ".;lib/*;lib/mysql-connector-j-*.jar;lib/mysql-connector-java-*.jar" src/com/bbms/model/*.java src/com/bbms/db/*.java src/com/bbms/dao/*.java src/com/bbms/service/*.java src/com/bbms/ui/*.java src/com/bbms/web/*.java src/com/bbms/Main.java

if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] Compilation finished successfully!
    echo Classes compiled into 'bin' folder.
) else (
    echo [ERROR] Compilation failed. Please ensure JDK 8+ is installed and 'javac' is in your PATH.
)

pause
