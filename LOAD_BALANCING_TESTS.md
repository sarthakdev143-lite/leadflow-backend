# Load Balancing Testing Guide

## Test Levels
1. **Pre-Deployment Tests** - Verify setup before starting
2. **Container Health Tests** - Verify all services running
3. **Load Balancing Tests** - Verify round-robin distribution
4. **Session Persistence Tests** - Verify Redis sharing
5. **Database Tests** - Verify MongoDB consistency
6. **API Tests** - Verify endpoints work
7. **Failover Tests** - Verify auto-recovery
8. **Performance Tests** - Verify load handling

---

# LEVEL 1: Pre-Deployment Tests

## Test 1.1: Environment Variables Verification
**Goal:** Ensure .env file has all required variables

```powershell
# Check .env exists
Test-Path ".\.env"

# Verify key variables exist
Get-Content ".\.env" | Select-String "MONGO_URL|JWT_SECRET|MAIL_USERNAME"
```

**Expected Result:** 
```
True (file exists)
MONGO_URL=...
JWT_SECRET_TOKEN=...
MAIL_USERNAME=...
```

## Test 1.2: Docker Installation Check
**Goal:** Verify Docker and Docker Compose are installed
cllel
```powershell
# Check Docker version
docker --version

# Check Docker Compose version
docker-compose --version

# Check Docker daemon running
docker ps
```

**Expected Result:**
```
Docker version 24.0.x
Docker Compose version 2.x.x
CONTAINER ID   IMAGE     COMMAND   CREATED   STATUS
(empty list or running containers)
```

## Test 1.3: Port Availability
**Goal:** Ensure required ports are free

```powershell
# Check if ports are available
netstat -ano | Select-String ":80 |:8080|:6379|:27017"

# Better check - this should return empty
Get-NetTCPConnection -LocalPort 80 -ErrorAction SilentlyContinue
Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue
Get-NetTCPConnection -LocalPort 6379 -ErrorAction SilentlyContinue
Get-NetTCPConnection -LocalPort 27017 -ErrorAction SilentlyContinue
```

**Expected Result:** (No output = ports are free)

---

# LEVEL 2: Container Health Tests

## Test 2.1: Build Docker Images
**Goal:** Verify Docker images build without errors

```powershell
# Clean previous builds
docker-compose down -v

# Build all images
docker-compose build --no-cache

# Verify images created
docker images | Select-String "leadflow"
```

**Expected Result:**
```
[+] Building 2m30.5s
 => => exporting to image
Successfully built ...

REPOSITORY              TAG       IMAGE ID      CREATED
leadflow-nginx          latest    abc123def456  2 seconds ago
leadflow-backend        latest    xyz789uvw012  5 seconds ago
```

## Test 2.2: Start Containers
**Goal:** Start all services and verify they initialize

```powershell
# Start all containers
docker-compose up -d

# Wait 30 seconds for services to initialize
Start-Sleep -Seconds 30

# Check container status
docker-compose ps
```

**Expected Result:**
```
NAME                    STATUS              PORTS
leadflow-nginx          Up (healthy)        0.0.0.0:80->80/tcp
leadflow-backend-1      Up (healthy)        8080/tcp
leadflow-backend-2      Up (healthy)        8080/tcp
leadflow-backend-3      Up (healthy)        8080/tcp
leadflow-redis          Up (healthy)        6379/tcp
leadflow-mongo          Up (healthy)        0.0.0.0:27017->27017/tcp
```

## Test 2.3: Container Logs Inspection
**Goal:** Verify no startup errors in logs

```powershell
# Check Nginx logs for errors
docker-compose logs nginx | Select-String "error|failed" -NotMatch

# Check backend-1 startup
docker-compose logs leadflow-backend-1 | Select-String "started|ready" -Match

# Check Redis startup
docker-compose logs leadflow-redis | Select-String "ready to accept"

# Check MongoDB startup
docker-compose logs leadflow-mongo | Select-String "waiting for connections"
```

**Expected Result:** (No error messages)
```
[+] 2024/05/27 12:30:00 started
nginx: master process started
redis-server: ready to accept connections
mongo: waiting for connections on port 27017
```

---

# LEVEL 3: Load Balancing Tests

## Test 3.1: Round-Robin Distribution
**Goal:** Verify Nginx distributes requests across 3 instances

```powershell
# Create array to track instances
$instances = @()

# Send 9 requests and collect responses
for ($i = 1; $i -le 9; $i++) {
    $response = curl http://localhost/api/health
    $instances += $response
    Write-Host "Request $i : $response"
    Start-Sleep -Milliseconds 500
}

# Count distribution
$instances | Group-Object | Format-Table @{N="Instance";E={$_.Name}}, @{N="Count";E={$_.Count}}
```

**Expected Result:**
```
Request 1 : OK - Instance: 1, Port: 8080, Host: leadflow-backend-1
Request 2 : OK - Instance: 2, Port: 8080, Host: leadflow-backend-2
Request 3 : OK - Instance: 3, Port: 8080, Host: leadflow-backend-3
Request 4 : OK - Instance: 1, Port: 8080, Host: leadflow-backend-1
Request 5 : OK - Instance: 2, Port: 8080, Host: leadflow-backend-2
Request 6 : OK - Instance: 3, Port: 8080, Host: leadflow-backend-3
Request 7 : OK - Instance: 1, Port: 8080, Host: leadflow-backend-1
Request 8 : OK - Instance: 2, Port: 8080, Host: leadflow-backend-2
Request 9 : OK - Instance: 3, Port: 8080, Host: leadflow-backend-3

Instance     Count
--------     -----
1            3
2            3
3            3
```

## Test 3.2: Load Balancing with Concurrent Requests
**Goal:** Verify load balancing under concurrent load

```powershell
# Create 30 concurrent requests
$jobs = @()

for ($i = 1; $i -le 30; $i++) {
    $job = Start-Job -ScriptBlock {
        curl http://localhost/api/health
    }
    $jobs += $job
}

# Wait for all jobs to complete
$results = $jobs | Wait-Job | Receive-Job

# Analyze distribution
$results | Select-String "Instance: \d" -AllMatches | % {$_.Matches.Value} | Group-Object | Format-Table Value, @{N="Count";E={$_.Count}}
```

**Expected Result:**
```
Value       Count
-----       -----
Instance: 1  10
Instance: 2  10
Instance: 3  10
```

## Test 3.3: Verify Nginx Logs Show Distribution
**Goal:** Check Nginx logs for proof of distribution

```powershell
# Get last 20 Nginx access logs
docker-compose logs --tail=20 nginx | Select-String "leadflow-backend"
```

**Expected Result:**
```
nginx_1 | leadflow-backend-1:8080
nginx_1 | leadflow-backend-2:8080
nginx_1 | leadflow-backend-3:8080
nginx_1 | leadflow-backend-1:8080
...
```

---

# LEVEL 4: Session Persistence Tests

## Test 4.1: Create Session Data
**Goal:** Write session to Redis and verify persistence

```powershell
# Send request to create session
$response = Invoke-RestMethod -Uri "http://localhost/api/health" -Headers @{"Cookie"="SESSION_ID=test-session-123"}

# Check Redis for session
docker-compose exec leadflow-redis redis-cli
# Inside redis-cli:
# > KEYS leadflow:session*
# > GET leadflow:session:test-session-123
```

**Expected Result:**
```
redis-cli> KEYS leadflow:session*
1) "leadflow:session:test-session-123"

redis-cli> GET leadflow:session:test-session-123
"{...session_data...}"
```

## Test 4.2: Session Access from Different Instance
**Goal:** Verify session accessible across all instances

```powershell
# Note the Set-Cookie from first request
$req1 = curl -i http://localhost/api/health

# Extract SESSION cookie
$cookie = [regex]::match($req1, 'SESSION=\w+').Value

# Send same cookie to different instances (repeat until you hit different instance)
for ($i = 1; $i -le 10; $i++) {
    $response = curl -H "Cookie: $cookie" http://localhost/api/health
    if ($response -notmatch "Instance: 1") {
        Write-Host "Session accessible from different instance: $response"
        break
    }
}
```

**Expected Result:**
```
Session accessible from different instance: OK - Instance: 2, Port: 8080
(Session data persisted across instances)
```

---

# LEVEL 5: Database Tests

## Test 5.1: MongoDB Connectivity
**Goal:** Verify MongoDB is accessible from backend

```powershell
# Check MongoDB connection
docker-compose exec leadflow-mongo mongosh -u admin -p secret --eval "db.adminCommand('ping')"
```

**Expected Result:**
```
{ ok: 1 }
```

## Test 5.2: Database Operations
**Goal:** Test data persistence across instances

```powershell
# Create a test user/lead through API
$body = @{
    name = "Test Lead"
    email = "test@example.com"
    phone = "1234567890"
} | ConvertTo-Json

curl -X POST http://localhost/api/leads `
  -Header "Content-Type: application/json" `
  -Body $body

# Retrieve data multiple times (from different instances)
for ($i = 1; $i -le 3; $i++) {
    curl http://localhost/api/leads
    Start-Sleep -Seconds 1
}
```

**Expected Result:** (Same data returned each time, even from different instances)
```
[{"id":"...", "name":"Test Lead", "email":"test@example.com"}]
```

---

# LEVEL 6: API Tests

## Test 6.1: Health Check Endpoint
**Goal:** Verify health endpoint responds from all instances

```powershell
# Test health endpoint
curl http://localhost/api/health

# Check full response
$response = curl -i http://localhost/api/health
Write-Host $response
```

**Expected Result:**
```
HTTP/1.1 200 OK
Content-Type: text/plain

OK - Instance: 1, Port: 8080, Host: leadflow-backend-1
```

## Test 6.2: Authentication Flow
**Goal:** Test JWT authentication through load balancer

```powershell
# 1. Register/Login to get JWT token
$loginData = @{
    email = "admin@leadflow.com"
    password = "password123"
} | ConvertTo-Json

$loginResponse = curl -X POST http://localhost/api/auth/login `
  -Header "Content-Type: application/json" `
  -Body $loginData

# Extract token
$token = $loginResponse | Select-String -Pattern '"token"\s*:\s*"([^"]+)"' -AllMatches
$jwt = $token.Matches[0].Groups[1].Value

Write-Host "JWT Token: $jwt"

# 2. Use token to access protected endpoint
curl -H "Authorization: Bearer $jwt" http://localhost/api/leads
```

**Expected Result:**
```
JWT Token: eyJhbGciOiJIUzI1NiIs...

[{"id":"...", "name":"...", "email":"..."}]
```

## Test 6.3: Cross-Instance Request Handling
**Goal:** Verify single request doesn't require sticky sessions

```powershell
# Create a request that spans multiple instances
$requestData = @{
    action = "get-leads"
    userId = "user-123"
} | ConvertTo-Json

# Send 5 requests without session cookie
for ($i = 1; $i -le 5; $i++) {
    $response = curl -X POST http://localhost/api/leads `
      -Header "Content-Type: application/json" `
      -Body $requestData
    
    Write-Host "Request $i succeeded: $(if ($response) { 'Yes' } else { 'No' })"
}
```

**Expected Result:**
```
Request 1 succeeded: Yes
Request 2 succeeded: Yes
Request 3 succeeded: Yes
Request 4 succeeded: Yes
Request 5 succeeded: Yes
```

---

# LEVEL 7: Failover Tests

## Test 7.1: Stop Single Instance
**Goal:** Verify requests still work when one instance is down

```powershell
# Stop backend-2
docker-compose stop leadflow-backend-2

# Wait 10 seconds for health check
Start-Sleep -Seconds 10

# Send 10 requests
$instances = @()
for ($i = 1; $i -le 10; $i++) {
    $response = curl http://localhost/api/health
    $instances += $response
    Write-Host "Request $i : $response"
}

# Count distribution (should only show 1 and 3)
$instances | Select-String "Instance: [13]" | Group-Object | Format-Table Value, @{N="Count";E={$_.Count}}

# Restart backend-2
docker-compose start leadflow-backend-2
```

**Expected Result:**
```
Request 1  : OK - Instance: 1, Port: 8080, Host: leadflow-backend-1
Request 2  : OK - Instance: 3, Port: 8080, Host: leadflow-backend-3
Request 3  : OK - Instance: 1, Port: 8080, Host: leadflow-backend-1
...

Instance: 1  Count: 5
Instance: 3  Count: 5

(No requests to Instance 2)
```

## Test 7.2: Instance Recovery
**Goal:** Verify instance rejoin after restart

```powershell
# Verify all 3 instances are up
docker-compose ps | Select-String "leadflow-backend"

# Send requests to verify backend-2 is serving again
for ($i = 1; $i -le 6; $i++) {
    $response = curl http://localhost/api/health
    Write-Host "Request $i : $response"
}
```

**Expected Result:**
```
leadflow-backend-1      Up (healthy)
leadflow-backend-2      Up (healthy)
leadflow-backend-3      Up (healthy)

Request 1 : OK - Instance: 1, ...
Request 2 : OK - Instance: 2, ...
Request 3 : OK - Instance: 3, ...
Request 4 : OK - Instance: 1, ...
Request 5 : OK - Instance: 2, ...
Request 6 : OK - Instance: 3, ...
```

## Test 7.3: All Instances Down (Negative Test)
**Goal:** Verify proper error when all instances are down

```powershell
# Stop all backends
docker-compose stop leadflow-backend-1 leadflow-backend-2 leadflow-backend-3

# Try to access
$response = curl -o $null -s -w "%{http_code}" http://localhost/api/health

Write-Host "HTTP Status Code: $response"

# Restart all
docker-compose start leadflow-backend-1 leadflow-backend-2 leadflow-backend-3
Start-Sleep -Seconds 10
```

**Expected Result:**
```
HTTP Status Code: 502 (Bad Gateway)

(After restart)
HTTP Status Code: 200
```

---

# LEVEL 8: Performance Tests

## Test 8.1: Throughput Test
**Goal:** Measure requests per second

```powershell
# Install Apache Bench or use Invoke-WebRequest in loop
$startTime = Get-Date
$successCount = 0
$totalRequests = 100

for ($i = 1; $i -le $totalRequests; $i++) {
    try {
        $response = curl -s http://localhost/api/health
        if ($response) { $successCount++ }
    }
    catch {}
}

$endTime = Get-Date
$duration = ($endTime - $startTime).TotalSeconds
$throughput = $successCount / $duration

Write-Host "Completed: $successCount / $totalRequests requests"
Write-Host "Duration: $duration seconds"
Write-Host "Throughput: $throughput requests/second"
```

**Expected Result:**
```
Completed: 100 / 100 requests
Duration: 5.23 seconds
Throughput: 19.12 requests/second
```

## Test 8.2: Response Time Distribution
**Goal:** Measure average, min, max response times

```powershell
$responseTimes = @()

for ($i = 1; $i -le 50; $i++) {
    $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
    curl -s http://localhost/api/health | Out-Null
    $stopwatch.Stop()
    $responseTimes += $stopwatch.ElapsedMilliseconds
}

$avg = ($responseTimes | Measure-Object -Average).Average
$min = ($responseTimes | Measure-Object -Minimum).Minimum
$max = ($responseTimes | Measure-Object -Maximum).Maximum

Write-Host "Min Response Time: $min ms"
Write-Host "Avg Response Time: $avg ms"
Write-Host "Max Response Time: $max ms"
```

**Expected Result:**
```
Min Response Time: 5 ms
Avg Response Time: 12 ms
Max Response Time: 45 ms
```

## Test 8.3: Concurrent Load Test
**Goal:** Simulate 50 concurrent users

```powershell
# Create 50 concurrent requests
$jobs = @()

$startTime = Get-Date

for ($i = 1; $i -le 50; $i++) {
    $job = Start-Job -ScriptBlock {
        for ($j = 1; $j -le 5; $j++) {
            curl -s http://localhost/api/health | Out-Null
        }
    }
    $jobs += $job
}

# Wait for completion
$jobs | Wait-Job | Out-Null

$endTime = Get-Date
$duration = ($endTime - $startTime).TotalSeconds

Write-Host "50 concurrent users × 5 requests each = 250 total requests"
Write-Host "Completed in: $duration seconds"
Write-Host "Overall throughput: $(250 / $duration) requests/second"

# Cleanup
$jobs | Remove-Job
```

**Expected Result:**
```
50 concurrent users × 5 requests each = 250 total requests
Completed in: 8.42 seconds
Overall throughput: 29.69 requests/second
```

---

# LEVEL 9: Monitoring Tests

## Test 9.1: Docker Container Resource Usage
**Goal:** Monitor CPU and Memory usage

```powershell
# Real-time stats for all containers
docker stats --no-stream

# Specific container
docker stats --no-stream leadflow-backend-1
```

**Expected Result:**
```
CONTAINER                CPU %     MEM USAGE / LIMIT       MEM %
leadflow-backend-1       0.5%      256 MB / 2 GB           12.5%
leadflow-backend-2       0.4%      245 MB / 2 GB           12.2%
leadflow-backend-3       0.6%      268 MB / 2 GB           13.4%
leadflow-redis           0.1%      8 MB / 2 GB             0.4%
leadflow-mongo           2.3%      512 MB / 2 GB           25.6%
leadflow-nginx           0.1%      12 MB / 2 GB            0.6%
```

## Test 9.2: Log Monitoring
**Goal:** Monitor logs from all services

```powershell
# Follow all logs
docker-compose logs -f

# Follow specific service
docker-compose logs -f leadflow-backend-1

# Show last 100 lines
docker-compose logs --tail=100 leadflow-backend-1

# Show logs with timestamps
docker-compose logs -t leadflow-backend-1
```

**Expected Result:**
```
2024-05-27T12:30:45.123Z leadflow-backend-1 | [INFO] Request received
2024-05-27T12:30:46.234Z leadflow-backend-2 | [INFO] Request received
2024-05-27T12:30:47.345Z leadflow-backend-3 | [INFO] Request received
```

## Test 9.3: Verify Health Checks Working
**Goal:** Ensure Docker health checks are functioning

```powershell
# Check health status
docker-compose ps --format="table {{.Names}}\t{{.Status}}"

# Detailed health info
docker inspect --format='{{json .State.Health}}' leadflow-backend-1 | ConvertFrom-Json
```

**Expected Result:**
```
NAME                    STATUS
leadflow-nginx          Up (healthy)
leadflow-backend-1      Up (healthy)
leadflow-backend-2      Up (healthy)
leadflow-backend-3      Up (healthy)
leadflow-redis          Up (healthy)
leadflow-mongo          Up (healthy)

Health Status: healthy
Failing Count: 0
```

---

# Quick Test Script

## Run All Tests Automatically

Create `run-all-tests.ps1`:

```powershell
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "LOAD BALANCING COMPREHENSIVE TEST SUITE" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Test 1: Container Status
Write-Host "`n[TEST 1] Container Status" -ForegroundColor Green
docker-compose ps

# Test 2: Health Check
Write-Host "`n[TEST 2] Health Check" -ForegroundColor Green
for ($i = 1; $i -le 6; $i++) {
    curl http://localhost/api/health
}

# Test 3: Load Distribution
Write-Host "`n[TEST 3] Load Distribution" -ForegroundColor Green
$instances = @()
for ($i = 1; $i -le 9; $i++) {
    $response = curl -s http://localhost/api/health
    $instances += ($response | Select-String "Instance: \d" -AllMatches).Matches.Value
}
$instances | Group-Object | Format-Table Value, @{N="Count";E={$_.Count}}

# Test 4: Concurrent Requests
Write-Host "`n[TEST 4] Concurrent Requests (30)" -ForegroundColor Green
$jobs = @()
for ($i = 1; $i -le 30; $i++) {
    $job = Start-Job -ScriptBlock { curl -s http://localhost/api/health }
    $jobs += $job
}
$results = $jobs | Wait-Job | Receive-Job
$results | Select-String "Instance: \d" | Group-Object | Format-Table

# Test 5: Failover
Write-Host "`n[TEST 5] Failover Test - Stopping backend-2" -ForegroundColor Green
docker-compose stop leadflow-backend-2
Start-Sleep -Seconds 5
for ($i = 1; $i -le 6; $i++) {
    curl -s http://localhost/api/health
}
docker-compose start leadflow-backend-2

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "ALL TESTS COMPLETED" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
```

Run with:
```powershell
.\run-all-tests.ps1
```

---

# Test Results Documentation

Create a test results file to track findings:

**Create `TEST_RESULTS.md`:**

```markdown
# Load Balancing Test Results

## Date: [DATE]
## Tester: [NAME]

### Level 1: Pre-Deployment ✅
- [ ] Environment variables verified
- [ ] Docker installation verified
- [ ] Ports available

### Level 2: Container Health ✅
- [ ] Images built successfully
- [ ] All containers running
- [ ] No startup errors

### Level 3: Load Balancing ✅
- [ ] Round-robin working (1, 2, 3, 1, 2, 3...)
- [ ] Concurrent requests distributed evenly
- [ ] Nginx logs show distribution

### Level 4: Session Persistence ✅
- [ ] Session stored in Redis
- [ ] Session accessible across instances
- [ ] No session data loss

### Level 5: Database ✅
- [ ] MongoDB connectivity confirmed
- [ ] Data persists across requests
- [ ] Consistent reads from all instances

### Level 6: API ✅
- [ ] Health endpoint working
- [ ] Authentication working
- [ ] Cross-instance requests working

### Level 7: Failover ✅
- [ ] Service continues when 1 instance down
- [ ] Failed instance auto-recovers
- [ ] All instances properly rejoin

### Level 8: Performance ✅
- [ ] Throughput: X requests/second
- [ ] Avg response time: X ms
- [ ] Handled 50 concurrent users

### Level 9: Monitoring ✅
- [ ] Resource usage normal
- [ ] Logs accessible
- [ ] Health checks passing

## Issues Found
- None / List any issues

## Notes
- Overall system is stable and production-ready
```

---

# Summary

✅ **Test Coverage:**
- Container initialization
- Load distribution verification
- Session persistence
- Database consistency
- API functionality
- Failover scenarios
- Performance metrics
- Resource monitoring

✅ **Pass Criteria:**
- All 3 instances receiving equal traffic
- Sessions persist across instances
- Requests continue when instance fails
- Response times < 50ms average
- Zero errors in logs

Would you like me to create an automated test script or help troubleshoot any specific test?
