# full-security-scan.ps1  (init-test.ps1)
param(
    [string]$BaseUrl = "http://localhost:8080",
    [int]   $Port    = 8080,
    [switch]$SkipDependencyCheck,
    [switch]$GenerateReport
)

Write-Host "Init Security Scan - E-Commerce Lab" -ForegroundColor Cyan
Write-Host "======================" -ForegroundColor Cyan

$ResultsDir = "security-test-results"
if (!(Test-Path $ResultsDir)) { New-Item -ItemType Directory -Path $ResultsDir | Out-Null }

$testResults = @()

# ── Security Headers ────────────────────────────────────────────────────────
Write-Host "`nTesting Security Headers..." -ForegroundColor Green
$expectedHeaders = @{
    "X-Frame-Options"           = "DENY"
    "X-Content-Type-Options"    = "nosniff"
    "X-XSS-Protection"          = "1; mode=block"
    "Strict-Transport-Security" = "max-age=31536000"
    "Content-Security-Policy"   = "default-src 'self'"
}

$response       = Invoke-WebRequest -Uri "$BaseUrl/api/products" -Method Get -UseBasicParsing
$missingHeaders = @()

foreach ($header in $expectedHeaders.Keys) {
    if ($response.Headers.ContainsKey($header)) {
        Write-Host "  [PASS] $header present" -ForegroundColor Green
    } else {
        Write-Host "  [FAIL] $header missing" -ForegroundColor Red
        $missingHeaders += $header
    }
}

if ($missingHeaders.Count -eq 0) {
    $testResults += [PSCustomObject]@{ Test = "Security Headers"; Status = "PASS"; Details = "All headers present" }
} else {
    $testResults += [PSCustomObject]@{ Test = "Security Headers"; Status = "FAIL"; Details = "Missing: $($missingHeaders -join ', ')" }
}

# ── Account Lockout ─────────────────────────────────────────────────────────
Write-Host "`nTesting account lockout..." -ForegroundColor Green
$lockoutEmail = "lockouttest@example.com"
$locked       = $false

for ($i = 1; $i -le 6; $i++) {
    $body = @{ email = $lockoutEmail; password = "wrongpassword$i"; rememberMe = $null } | ConvertTo-Json
    try {
        $null = Invoke-RestMethod -Uri "$BaseUrl/api/auth/login" -Method Post -Body $body -ContentType "application/json" -TimeoutSec 5
    } catch {
        $sc = [int]$_.Exception.Response.StatusCode
        if ($sc -eq 423 -or $sc -eq 429) { $locked = $true; break }
    }
}

if ($locked) {
    Write-Host "  [PASS] Account locked after failed attempts" -ForegroundColor Green
    $testResults += [PSCustomObject]@{ Test = "Account Lockout"; Status = "PASS"; Details = "Account locked after 5 failed attempts" }
} else {
    Write-Host "  [FAIL] No account lockout detected after 6 attempts" -ForegroundColor Red
    $testResults += [PSCustomObject]@{ Test = "Account Lockout"; Status = "FAIL"; Details = "No account lockout detected" }
}

# ── Security Misconfiguration ───────────────────────────────────────────────
Write-Host "`nTesting Security Misconfigurations..." -ForegroundColor Green
$sensitiveEndpoints = @(
    "/actuator", 
    "/actuator/env",
    "/actuator/health",
    "/actuator/info",
    "/actuator/metrics",
    "/swagger-ui.html", 
    "/v3/api-docs",
    "/api/admin/routes", 
    "/h2-console", 
    "/console", 
    "/env", 
    "/config"
)

$exposedEndpoints = @()
foreach ($ep in $sensitiveEndpoints) {
    try {
        $r = Invoke-WebRequest -Uri "$BaseUrl$ep" -Method Get -UseBasicParsing -TimeoutSec 3
        if ($r.Content -match "404 Not Found|403 Forbidden|401 Unauthorized") {
            Write-Host "  [FAIL] Sensitive endpoint exposed: $ep" -ForegroundColor Red
            $exposedEndpoints += $ep
        } else {
            Write-Host "[Pass] endpoint not exposed:$ep" -ForegroundColor Green
        }
    } catch {
    }
}


foreach ($dir in @("/uploads/", "/static/", "/images/", "/files/", "/backup/")) {
    try {
        $r = Invoke-WebRequest -Uri "$BaseUrl$dir" -Method Get -UseBasicParsing -TimeoutSec 3
        if ($r.Content -match "Index of|Directory listing for") {
            Write-Host "  [FAIL] Directory listing enabled: $dir" -ForegroundColor Red
            $exposedEndpoints += "$dir (directory listing)"
        }
    } catch {
        Write-Host "  [PASS] Directory listing disabled: $dir" -ForegroundColor Green
    }
}

if ($exposedEndpoints.Count -eq 0) {
    $testResults += [PSCustomObject]@{ Test = "Security Misconfiguration"; Status = "PASS"; Details = "No sensitive endpoints exposed" }
} else {
    $testResults += [PSCustomObject]@{ Test = "Security Misconfiguration"; Status = "FAIL"; Details = "Exposed: $($exposedEndpoints -join ', ')" }
}

# Test for CSRF Protection
Write-Host "`nTesting CSRF Protection..." -ForegroundColor Green
$authToken = $null

$loginRequest = @{
    email = "hassan@example.com"
    password = "password123"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/api/auth/login" -Method Post -Body $loginRequest -ContentType "application/json" -TimeoutSec 5
    $authToken = $response.token
    Write-Host "  ✓ Authentication successful, token obtained" -ForegroundColor Green
    $response = Invoke-WebRequest -Uri "$BaseUrl/api/cart/add/24" -Headers @{Authorization = "Bearer $authToken"} -Method Post -UseBasicParsing -TimeoutSec 5
    Write-Host "  ✓ Item added to cart successfully" -ForegroundColor Green
}
catch {
    
}

$csrfPayload = @{
    couponCode = $null
    useStoreBalance = 0
    shippingAddress = @{
        street = "123 Main St"
        city = "Anytown"
        state = "CA"
        zipCode = "12345"
        country = "USA"
    } | ConvertTo-Json
    giftCards = @()
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/api/orders/place" -Headers @{Authorization = "Bearer $authToken"} -Method Post -Body $csrfPayload -ContentType "application/json" -TimeoutSec 5
    Write-Host "  ✗ No CSRF protection detected - request accepted without token!" -ForegroundColor Red
    $testResults += [PSCustomObject]@{Test = "CSRF Protection"; Status = "FAIL"; Details = "No CSRF token required"}
} catch {
    if ($_.Exception.Response.StatusCode -eq 403 || $_.Exception.Response.StatusCode -eq 401) {
        Write-Host "  ✓ CSRF protection active" -ForegroundColor Green
        $testResults += [PSCustomObject]@{Test = "CSRF Protection"; Status = "PASS"; Details = "CSRF protection implemented"}
    } else {
        $statusCode = $_.Exception.Response.StatusCode
        Write-Host "  ⚠ CSRF protection status unclear {$statusCode}" -ForegroundColor Yellow
        $testResults += [PSCustomObject]@{Test = "CSRF Protection"; Status = "WARN"; Details = "Unable to determine CSRF status"}
    }
}

# ── Summary ─────────────────────────────────────────────────────────────────
$passed   = ($testResults | Where-Object { $_.Status -eq "PASS" }).Count
$failed   = ($testResults | Where-Object { $_.Status -eq "FAIL" }).Count
$warnings = ($testResults | Where-Object { $_.Status -eq "WARN" }).Count
$total    = $testResults.Count

Write-Host "`n==========================================" -ForegroundColor Cyan
Write-Host "SECURITY TEST SUMMARY"                       -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Passed  : $passed/$total"   -ForegroundColor Green
Write-Host "Failed  : $failed/$total"   -ForegroundColor Red
Write-Host "Warnings: $warnings/$total" -ForegroundColor Yellow
$testResults | Format-Table -AutoSize

# ── Structured return for run-all.ps1 ──────────────────────────────────────
$scorePercent = if ($total -gt 0) { [math]::Round($passed / $total * 100) } else { 0 }
$detailsHash  = @{}
foreach ($r in $testResults) { $detailsHash[$r.Test] = $r.Status }
$vulns = ($testResults | Where-Object { $_.Status -ne "PASS" } | ForEach-Object { "$($_.Test): $($_.Details)" })

Write-Output ([PSCustomObject]@{
    TestName        = "Initial Security Scan"
    Status          = if ($failed -eq 0 -and $warnings -eq 0) { "PASS" } elseif ($failed -eq 0) { "WARNING" } else { "FAIL" }
    Score           = $passed
    MaxScore        = $total
    ScorePercent    = $scorePercent
    Details         = $detailsHash
    Vulnerabilities = @($vulns)
})
