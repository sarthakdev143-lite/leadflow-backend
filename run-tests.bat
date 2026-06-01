@echo off
REM Load Balancing Test Suite - Batch Version
REM Run this file to execute all tests

setlocal enabledelayedexpansion

echo.
echo ========================================
echo   LOAD BALANCING TEST SUITE
echo ========================================
echo.

REM Colors aren't available in batch, so we'll use text output

echo [TEST 1] Checking Docker installation...
docker --version >nul 2>&1
if %errorlevel% equ 0 (
    echo   OK - Docker installed
) else (
    echo   FAIL - Docker not installed
    goto :eof
)

echo.
echo [TEST 2] Checking Docker Compose...
docker-compose --version >nul 2>&1
if %errorlevel% equ 0 (
    echo   OK - Docker Compose installed
) else (
    echo   FAIL - Docker Compose not installed
    goto :eof
)

echo.
echo [TEST 3] Checking container status...
docker-compose ps
echo.

echo [TEST 4] Testing health endpoint (6 requests - watch for round-robin: 1,2,3,1,2,3)...
for /l %%i in (1,1,6) do (
    echo   Request %%i:
    curl -s http://localhost/api/health
    timeout /t 1 /nobreak >nul
)

echo.
echo [TEST 5] Testing load distribution with 9 requests...
echo   Expect: Instance 1, 2, 3, 1, 2, 3, 1, 2, 3
for /l %%i in (1,1,9) do (
    curl -s http://localhost/api/health
    timeout /t 0 /nobreak >nul
)

echo.
echo [TEST 6] Testing failover - stopping backend-2...
docker-compose stop leadflow-backend-2
timeout /t 10 /nobreak >nul
echo   Sending 6 requests (should get instances 1 and 3 only):
for /l %%i in (1,1,6) do (
    curl -s http://localhost/api/health
    timeout /t 1 /nobreak >nul
)
echo.
echo   Restarting backend-2...
docker-compose start leadflow-backend-2
timeout /t 15 /nobreak >nul

echo.
echo [TEST 7] Verifying recovery - all 3 instances should be back...
for /l %%i in (1,1,9) do (
    curl -s http://localhost/api/health
    timeout /t 1 /nobreak >nul
)

echo.
echo [TEST 8] Checking Redis...
docker-compose exec -T leadflow-redis redis-cli ping

echo.
echo [TEST 9] Checking MongoDB...
docker-compose exec -T leadflow-mongo mongosh -u admin -p secret --eval "db.adminCommand('ping')"

echo.
echo ========================================
echo   TESTS COMPLETED
echo ========================================
echo.
