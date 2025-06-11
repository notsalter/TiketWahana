@echo off
echo ========================================
echo    COMPILING TIKET WAHANA APPLICATION
echo ========================================

REM Check if Java is installed
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java 17 or higher
    pause
    exit /b 1
)

REM Create directories
if not exist "target\classes" mkdir "target\classes"

REM Compile Java files
echo Compiling Java source files...
javac -d target\classes -cp "target\classes" src\main\java\com\mycompany\tiketwahana\*.java src\main\java\com\mycompany\tiketwahana\database\*.java src\main\java\com\mycompany\tiketwahana\forms\*.java

if %errorlevel% neq 0 (
    echo ERROR: Compilation failed
    echo Make sure MySQL Connector JAR is in classpath
    pause
    exit /b 1
)

echo.
echo ========================================
echo    RUNNING TIKET WAHANA APPLICATION
echo ========================================
echo Starting application...
echo.

REM Run the application
java -cp "target\classes" com.mycompany.tiketwahana.MainApp

if %errorlevel% neq 0 (
    echo.
    echo ERROR: Failed to start application
    echo Check database connection and MySQL Connector JAR
    pause
)

pause
