@echo off
echo 🚀 Starting Meeting Calendar Assistant...
echo.

REM Check if Maven is available
mvn --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Maven not found! Please install Maven and add it to your PATH.
    echo    Download from: https://maven.apache.org/download.cgi
    pause
    exit /b 1
)

echo ✅ Maven found

REM Build and run the application
echo 📦 Building application...
call mvn clean compile
if %errorlevel% neq 0 (
    echo ❌ Build failed! Please check the error messages above.
    pause
    exit /b 1
)

echo ✅ Build successful
echo.

echo 🧪 Running tests...
call mvn test -q

echo.
echo 🚀 Starting application...
echo 📍 API will be available at: http://localhost:8080/api
echo 🗃️  H2 Console: http://localhost:8080/api/h2-console
echo.
echo Press Ctrl+C to stop the application
echo.

call mvn spring-boot:run 