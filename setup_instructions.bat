@echo off
echo ========================================
echo    TIKET WAHANA - Setup Instructions
echo ========================================
echo.
echo 1. Database Setup:
echo    - Open phpMyAdmin (http://localhost/phpmyadmin)
echo    - Import database_schema.sql to create the database
echo    - Default database: ticket_wahana_db
echo    - Default user: root (no password)
echo.
echo 2. Dependencies Required:
echo    - Java 17 or higher
echo    - MySQL Server (XAMPP/WAMP recommended)
echo    - Maven (for building)
echo.
echo 3. To Run the Application:
echo    - Run: mvn clean compile exec:java
echo    - Or compile and run MainApp.java directly
echo.
echo 4. Default Login Credentials:
echo    - Username: admin
echo    - Password: admin
echo.
echo ========================================
echo Press any key to continue...
pause >nul
