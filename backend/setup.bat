@echo off
echo ============================================
echo   ECOMMERCE PROJECT - AUTO SETUP
echo ============================================
echo.

REM Kiểm tra Java
echo [1/5] Checking Java...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Java not found! Please install Java 17+
    pause
    exit /b 1
)
echo [OK] Java found

REM Kiểm tra Maven
echo.
echo [2/5] Checking Maven...
call mvnw.cmd --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Maven wrapper not found!
    pause
    exit /b 1
)
echo [OK] Maven found

REM Kiểm tra application.yml
echo.
echo [3/5] Checking configuration...
if not exist "src\main\resources\application.yml" (
    echo [WARNING] application.yml not found!
    echo Please copy application.yml.example to application.yml and update your config
    echo.
    echo Do you want to copy template now? (Y/N)
    set /p choice=
    if /i "%choice%"=="Y" (
        copy "src\main\resources\application.yml.example" "src\main\resources\application.yml"
        echo [OK] Template copied. Please update your passwords and API keys!
        echo Edit: src\main\resources\application.yml
        pause
    )
) else (
    echo [OK] application.yml found
)

REM Tạo folder uploads
echo.
echo [4/5] Creating uploads folder...
if not exist "uploads" (
    mkdir uploads
    echo [OK] uploads/ folder created
) else (
    echo [OK] uploads/ folder exists
)

REM Build project
echo.
echo [5/5] Building project...
echo This may take a few minutes...
call mvnw.cmd clean install -DskipTests
if %errorlevel% neq 0 (
    echo [ERROR] Build failed!
    pause
    exit /b 1
)
echo [OK] Build successful

echo.
echo ============================================
echo   SETUP COMPLETED!
echo ============================================
echo.
echo Next steps:
echo 1. Create PostgreSQL database: ecommerce
echo 2. Run: psql -U postgres -d ecommerce -f table.sql
echo 3. Run: psql -U postgres -d ecommerce -f insert_faq.sql
echo 4. Start backend: mvnw.cmd spring-boot:run
echo.
echo Press any key to exit...
pause >nul
