@echo off
echo   Software Backend - Startup Script
echo.

echo [1/4] Dropping old database (if exists)...
mysql -u root -p12345 -e "DROP DATABASE IF EXISTS softwareuniverse;"
if %errorlevel% neq 0 (
    echo ERROR: Failed to drop database
    pause
    exit /b 1
)
echo       Old database dropped.
echo.

echo [2/4] Creating database and tables from schema.sql...
mysql -u root -p12345 < src\main\resources\db\schema.sql
if %errorlevel% neq 0 (
    echo ERROR: Failed to create database
    pause
    exit /b 1
)
echo       Database ready.
echo.

echo [3/4] Starting Spring Boot (DataSeeder will seed admin)...
echo.

echo [4/4] Launching...
mvn spring-boot:run