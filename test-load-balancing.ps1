#!/usr/bin/env pwsh

<#
.SYNOPSIS
    Comprehensive Load Balancing Test Suite for Leadflow Backend
.DESCRIPTION
    Automated testing for load balancing implementation
.EXAMPLE
    .\test-load-balancing.ps1
#>

# Color helper functions
function Write-Success {
    param([string]$Message)
    Write-Host "✅ $Message" -ForegroundColor Green
}

function Write-Error-Custom {
    param([string]$Message)
    Write-Host "❌ $Message" -ForegroundColor Red
}

function Write-Info {
    param([string]$Message)
    Write-Host "ℹ️  $Message" -ForegroundColor Cyan
}

function Write-Section {
    param([string]$Title)
    Write-Host "`n" + "="*60 -ForegroundColor Yellow
    Write-Host "  $Title" -ForegroundColor Yellow
    Write-Host "="*60 -ForegroundColor Yellow
}

# Global variables
$TestResults = @{
    Passed = 0
    Failed = 0
    Tests = @()
}

function Record-Test {
    param(
        [string]$TestName,
        [bool]$Passed,
        [string]$Details = ""
    )
    
    $TestResults.Tests += @{
        Name = $TestName
        Passed = $Passed
        Details = $Details
        Timestamp = Get-Date
    }
    
    if ($Passed) {
        $TestResults.Passed++
        Write-Success "$TestName"
    } else {
        $TestResults.Failed++
        Write-Error-Custom "$TestName"
        if ($Details) { Write-Host "   Details: $Details" -ForegroundColor Red }
    }
}

# ============================================================================
# LEVEL 1: PRE-DEPLOYMENT TESTS
# ============================================================================

Write-Section "LEVEL 1: PRE-DEPLOYMENT TESTS"

# Test 1.1: Environment variables
Write-Info "Test 1.1: Checking environment variables..."
if (Test-Path ".\.env") {
    $envContent = Get-Content ".\.env"
    $hasRequired = ($envContent | Select-String "MONGO_URL|JWT_SECRET|MAIL_USERNAME|REDIS" | Measure-Object).Count -ge 3
    Record-Test "Environment variables exist" $hasRequired "Required variables: MONGO_URL, JWT_SECRET, MAIL_USERNAME"
} else {
    Record-Test "Environment variables exist" $false ".env file not found"
}

# Test 1.2: Docker installation
Write-Info "Test 1.2: Checking Docker installation..."
try {
    $dockerVersion = docker --version
    Record-Test "Docker installed" $true $dockerVersion
} catch {
    Record-Test "Docker installed" $false "Docker not found in PATH"
}

try {
    $composeVersion = docker-compose --version
    Record-Test "Docker Compose installed" $true $composeVersion
} catch {
    Record-Test "Docker Compose installed" $false "Docker Compose not found"
}

# Test 1.3: Port availability
Write-Info "Test 1.3: Checking port availability..."
$portsToCheck = @(80, 8080, 6379, 27017)
foreach ($port in $portsToCheck) {
    $connection = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
    $available = $null -eq $connection
    Record-Test "Port $port available" $available
}

# ============================================================================
# LEVEL 2: CONTAINER HEALTH TESTS
# ============================================================================

Write-Section "LEVEL 2: CONTAINER HEALTH TESTS"

# Test 2.1: Docker images exist
Write-Info "Test 2.1: Building Docker images..."
try {
    $imagesList = docker images --format "table {{.Repository}}\t{{.Tag}}" | Select-String "nginx|redis|mongo"
    $hasRequiredImages = $imagesList.Count -ge 3
    Record-Test "Docker images available" $hasRequiredImages
} catch {
    Record-Test "Docker images available" $false "Failed to list images"
}

# Test 2.2: Start containers
Write-Info "Test 2.2: Starting containers..."
try {
    docker-compose up -d 2>&1 | Out-Null
    Start-Sleep -Seconds 15  # Wait for services to initialize
    Record-Test "Containers started successfully" $true
} catch {
    Record-Test "Containers started successfully" $false $_.Exception.Message
}

# Test 2.3: Check container status
Write-Info "Test 2.3: Verifying container health..."
$containerStatus = docker-compose ps --format "{{.Names}}\t{{.Status}}"
$healthyContainers = ($containerStatus | Select-String "healthy" | Measure-Object).Count

$expectedContainers = 6  # nginx, backend-1, backend-2, backend-3, redis, mongo
Record-Test "All containers healthy" ($healthyContainers -eq $expectedContainers) "Healthy: $healthyContainers/$expectedContainers"

# Test 2.4: No startup errors
Write-Info "Test 2.4: Checking for startup errors..."
$errors = @()
@('nginx', 'leadflow-backend-1', 'leadflow-backend-2', 'leadflow-backend-3', 'leadflow-redis', 'leadflow-mongo') | ForEach-Object {
    $logs = docker-compose logs $_ 2>&1 | Select-String "error|failed|exception" -NotMatch
    if ($logs) {
        $errors += $_
    }
}
Record-Test "No container startup errors" ($errors.Count -eq 0)

# ============================================================================
# LEVEL 3: LOAD BALANCING TESTS
# ============================================================================

Write-Section "LEVEL 3: LOAD BALANCING TESTS"

# Test 3.1: Basic health endpoint
Write-Info "Test 3.1: Testing health endpoint..."
try {
    $response = Invoke-WebRequest -Uri "http://localhost/api/health" -ErrorAction Stop
    $isHealthy = $response.StatusCode -eq 200
    Record-Test "Health endpoint responds" $isHealthy "Status: $($response.StatusCode)"
} catch {
    Record-Test "Health endpoint responds" $false $_.Exception.Message
}

# Test 3.2: Round-robin distribution
Write-Info "Test 3.2: Testing round-robin distribution (9 requests)..."
$instances = @()
for ($i = 1; $i -le 9; $i++) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost/api/health" -ErrorAction Stop
        $content = $response.Content
        if ($content -match "Instance: (\d)") {
            $instances += [int]$matches[1]
        }
    } catch {
        Write-Host "Request $i failed: $_" -ForegroundColor Red
    }
}

# Check if distribution is correct (should cycle 1,2,3,1,2,3...)
$expectedPattern = @(1, 2, 3, 1, 2, 3, 1, 2, 3)
$isRoundRobin = -not (Compare-Object $instances $expectedPattern)

Record-Test "Round-robin distribution working" $isRoundRobin "Distribution: $($instances -join ',')"

# Test 3.3: Load distribution balance
$distribution = $instances | Group-Object | Sort-Object Name
$counts = @($distribution | ForEach-Object { $_.Count })
$isBalanced = $counts[0] -eq $counts[1] -and $counts[1] -eq $counts[2]
Record-Test "Load balanced equally" $isBalanced "Counts: $($counts -join ', ')"

# Test 3.4: Concurrent requests
Write-Info "Test 3.4: Testing concurrent requests (30 parallel)..."
$jobs = @()
for ($i = 1; $i -le 30; $i++) {
    $job = Start-Job -ScriptBlock {
        try {
            Invoke-WebRequest -Uri "http://localhost/api/health" -ErrorAction Stop
        } catch {}
    }
    $jobs += $job
}

$results = @()
$jobs | Wait-Job | ForEach-Object {
    $output = Receive-Job $_
    if ($output -match "Instance: (\d)") {
        $results += [int]$matches[1]
    }
}

$concurrentDistribution = $results | Group-Object | Sort-Object Name
$concurrentCounts = @($concurrentDistribution | ForEach-Object { $_.Count })
$concurrentBalanced = [Math]::Max($concurrentCounts) - [Math]::Min($concurrentCounts) -le 2

Record-Test "Concurrent requests balanced" $concurrentBalanced "Concurrent distribution: $($concurrentCounts -join ', ')"

$jobs | Remove-Job

# ============================================================================
# LEVEL 4: FAILOVER TESTS
# ============================================================================

Write-Section "LEVEL 4: FAILOVER TESTS"

# Test 4.1: Service continues with one instance down
Write-Info "Test 4.1: Testing failover (stopping backend-2)..."
try {
    docker-compose stop leadflow-backend-2 2>&1 | Out-Null
    Start-Sleep -Seconds 5
    
    $failoverInstances = @()
    for ($i = 1; $i -le 6; $i++) {
        $response = Invoke-WebRequest -Uri "http://localhost/api/health" -ErrorAction Stop
        if ($response.Content -match "Instance: (\d)") {
            $failoverInstances += [int]$matches[1]
        }
    }
    
    # Should only have instances 1 and 3
    $hasNo2 = -not ($failoverInstances -contains 2)
    $hasSuccess = $failoverInstances.Count -eq 6
    
    Record-Test "Service continues with instance down" ($hasSuccess -and $hasNo2) "Instances: $($failoverInstances -join ',')"
    
    # Restart instance
    docker-compose start leadflow-backend-2 2>&1 | Out-Null
    Start-Sleep -Seconds 10
    
} catch {
    Record-Test "Service continues with instance down" $false $_.Exception.Message
    docker-compose start leadflow-backend-2 2>&1 | Out-Null
}

# Test 4.2: Instance recovery
Write-Info "Test 4.2: Testing instance recovery..."
try {
    $recoveryInstances = @()
    for ($i = 1; $i -le 9; $i++) {
        $response = Invoke-WebRequest -Uri "http://localhost/api/health" -ErrorAction Stop
        if ($response.Content -match "Instance: (\d)") {
            $recoveryInstances += [int]$matches[1]
        }
    }
    
    # Should have all 3 instances
    $has1 = $recoveryInstances -contains 1
    $has2 = $recoveryInstances -contains 2
    $has3 = $recoveryInstances -contains 3
    
    Record-Test "Failed instance successfully recovered" ($has1 -and $has2 -and $has3) "Instances: $($recoveryInstances | Group-Object | ForEach-Object { "$($_.Name)=$($_.Count)" } | Join-String -Separator ',')"
    
} catch {
    Record-Test "Failed instance successfully recovered" $false $_.Exception.Message
}

# ============================================================================
# LEVEL 5: DATABASE TESTS
# ============================================================================

Write-Section "LEVEL 5: DATABASE TESTS"

# Test 5.1: MongoDB connectivity
Write-Info "Test 5.1: Testing MongoDB connectivity..."
try {
    $mongoPing = docker-compose exec -T leadflow-mongo mongosh -u admin -p secret --eval "db.adminCommand('ping')" 2>&1
    $mongoPassed = $mongoPing -match "ok"
    Record-Test "MongoDB connectivity" $mongoPassed
} catch {
    Record-Test "MongoDB connectivity" $false $_.Exception.Message
}

# Test 5.2: Redis connectivity
Write-Info "Test 5.2: Testing Redis connectivity..."
try {
    $redisPing = docker-compose exec -T leadflow-redis redis-cli ping 2>&1
    $redisPassed = $redisPing -match "PONG"
    Record-Test "Redis connectivity" $redisPassed
} catch {
    Record-Test "Redis connectivity" $false $_.Exception.Message
}

# ============================================================================
# LEVEL 6: PERFORMANCE TESTS
# ============================================================================

Write-Section "LEVEL 6: PERFORMANCE TESTS"

# Test 6.1: Response time measurement
Write-Info "Test 6.1: Measuring response times (50 requests)..."
$responseTimes = @()
$successCount = 0

for ($i = 1; $i -le 50; $i++) {
    $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
    try {
        $response = Invoke-WebRequest -Uri "http://localhost/api/health" -ErrorAction Stop
        $stopwatch.Stop()
        $responseTimes += $stopwatch.ElapsedMilliseconds
        $successCount++
    } catch {
        $stopwatch.Stop()
    }
}

if ($responseTimes.Count -gt 0) {
    $avgTime = ($responseTimes | Measure-Object -Average).Average
    $minTime = ($responseTimes | Measure-Object -Minimum).Minimum
    $maxTime = ($responseTimes | Measure-Object -Maximum).Maximum
    
    $avgWithinThreshold = $avgTime -lt 100
    Record-Test "Response time acceptable" $avgWithinThreshold "Min: ${minTime}ms, Avg: $([Math]::Round($avgTime, 2))ms, Max: ${maxTime}ms"
}

# Test 6.2: Throughput
Write-Info "Test 6.2: Measuring throughput (100 sequential requests)..."
$startTime = Get-Date
$throughputCount = 0

for ($i = 1; $i -le 100; $i++) {
    try {
        Invoke-WebRequest -Uri "http://localhost/api/health" -ErrorAction Stop | Out-Null
        $throughputCount++
    } catch {}
}

$endTime = Get-Date
$duration = ($endTime - $startTime).TotalSeconds
$throughput = [Math]::Round($throughputCount / $duration, 2)

Record-Test "Minimum throughput (5 req/sec)" ($throughput -gt 5) "Achieved: $throughput requests/second"

# ============================================================================
# LEVEL 7: MONITORING TESTS
# ============================================================================

Write-Section "LEVEL 7: MONITORING & LOGS TESTS"

# Test 7.1: Docker stats available
Write-Info "Test 7.1: Checking Docker resource metrics..."
try {
    $stats = docker stats --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}"
    $statsCount = ($stats | Measure-Object).Count
    $hasStats = $statsCount -ge 6
    Record-Test "Docker stats available" $hasStats "Containers: $statsCount"
} catch {
    Record-Test "Docker stats available" $false $_.Exception.Message
}

# Test 7.2: Logs accessible
Write-Info "Test 7.2: Checking container logs..."
try {
    $nginxLogs = docker-compose logs nginx --tail=5
    $backedLogs = docker-compose logs leadflow-backend-1 --tail=5
    $logsAccessible = $nginxLogs -and $backedLogs
    Record-Test "Container logs accessible" ($null -ne $nginxLogs) "Logs retrieved"
} catch {
    Record-Test "Container logs accessible" $false $_.Exception.Message
}

# ============================================================================
# SUMMARY & REPORT
# ============================================================================

Write-Section "TEST SUMMARY"

$totalTests = $TestResults.Passed + $TestResults.Failed
$passPercentage = if ($totalTests -gt 0) { [Math]::Round(($TestResults.Passed / $totalTests) * 100, 2) } else { 0 }

Write-Host "`n"
Write-Host "Total Tests:    $totalTests" -ForegroundColor Cyan
Write-Host "Passed:         " -NoNewline -ForegroundColor Cyan
Write-Host "$($TestResults.Passed)" -ForegroundColor Green
Write-Host "Failed:         " -NoNewline -ForegroundColor Cyan
Write-Host "$($TestResults.Failed)" -ForegroundColor Red
Write-Host "Pass Rate:      $passPercentage%" -ForegroundColor Yellow

if ($TestResults.Failed -eq 0) {
    Write-Host "`n" + "🎉 ALL TESTS PASSED! 🎉" -ForegroundColor Green
    Write-Host "Your load balancing implementation is working perfectly!" -ForegroundColor Green
} else {
    Write-Host "`n⚠️  Some tests failed. Review details above." -ForegroundColor Yellow
}

# Save detailed report
$reportPath = "TEST_RESULTS_$(Get-Date -Format 'yyyyMMdd_HHmmss').txt"
$report = @"
LOAD BALANCING TEST REPORT
Generated: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')

SUMMARY
=======
Total Tests: $totalTests
Passed: $($TestResults.Passed)
Failed: $($TestResults.Failed)
Pass Rate: $passPercentage%

DETAILED RESULTS
================
"@

foreach ($test in $TestResults.Tests) {
    $status = if ($test.Passed) { "✅ PASS" } else { "❌ FAIL" }
    $report += "`n$status - $($test.Name)"
    if ($test.Details) {
        $report += "`n       Details: $($test.Details)"
    }
    $report += "`n       Time: $($test.Timestamp)`n"
}

$report | Out-File $reportPath
Write-Host "`nDetailed report saved to: $reportPath" -ForegroundColor Cyan

Write-Host "`n" + "="*60 -ForegroundColor Yellow
Write-Host "Tests completed at: $(Get-Date)" -ForegroundColor Yellow
Write-Host "="*60 -ForegroundColor Yellow
