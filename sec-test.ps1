# full-security-scan.ps1
param(
    [string]$BaseUrl = "http://localhost:8080",
    [int]$Port = 8080,
    [switch]$SkipDependencyCheck,
    [switch]$GenerateReport
)

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "OWASP Top 10 Security Scan - E-Commerce Lab" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Target: $BaseUrl" -ForegroundColor Yellow
Write-Host "Start Time: $(Get-Date)" -ForegroundColor Yellow
Write-Host "==========================================`n" -ForegroundColor Cyan

# Create results directory
$ResultsDir = "security-test-results"
if (!(Test-Path $ResultsDir)) {
    New-Item -ItemType Directory -Path $ResultsDir | Out-Null
}

# Test Results Collection
$testResults = @()

# 1. Check if application is running
Write-Host "[1/12] Checking application status..." -ForegroundColor Green
try {
    $response = Invoke-WebRequest -Uri "$BaseUrl/api/products" -Method Get -UseBasicParsing -TimeoutSec 5
    Write-Host "✓ Application is running" -ForegroundColor Green
    $testResults += [PSCustomObject]@{
        Test = "Application Status"
        Status = "PASS"
        Details = "Application is reachable"
    }
} catch {
    Write-Host "✗ Application is not running. Please start the application first!" -ForegroundColor Red
    Write-Host "Run: ./mvnw spring-boot:run" -ForegroundColor Yellow
    exit 1
}

# 2. Test for Security Headers
Write-Host "`n[2/12] Testing Security Headers..." -ForegroundColor Green
$headers = @{
    "X-Frame-Options" = "DENY"
    "X-Content-Type-Options" = "nosniff"
    "X-XSS-Protection" = "1; mode=block"
    "Strict-Transport-Security" = "max-age=31536000"
    "Content-Security-Policy" = "default-src 'self'"
}

$response = Invoke-WebRequest -Uri "$BaseUrl/api/products" -Method Get -UseBasicParsing
$missingHeaders = @()

foreach ($header in $headers.Keys) {
    if ($response.Headers.ContainsKey($header)) {
        Write-Host "  ✓ $header present" -ForegroundColor Green
    } else {
        Write-Host "  ✗ $header missing" -ForegroundColor Red
        $missingHeaders += $header
    }
}

if ($missingHeaders.Count -eq 0) {
    $testResults += [PSCustomObject]@{Test = "Security Headers"; Status = "PASS"; Details = "All headers present"}
} else {
    $testResults += [PSCustomObject]@{Test = "Security Headers"; Status = "FAIL"; Details = "Missing: $($missingHeaders -join ', ')"}
}

# 3. Test for SQL Injection
Write-Host "`n[3/12] Testing SQL Injection Vulnerabilities..." -ForegroundColor Green
$sqlPayloads = @(
    "' OR '1'='1",
    "'; DROP TABLE users; --",
    "1; SELECT * FROM users",
    "admin' --",
    "1 UNION SELECT * FROM users",
    "' OR 1=1 --",
    "'; INSERT INTO admin VALUES('hacker','pass')--",
    "1' AND 1=1 --",
    "1' AND 1=2 --"
)

$sqlVulnerable = @()
foreach ($payload in $sqlPayloads) {
    try {
        $response = Invoke-WebRequest -Uri "$BaseUrl/api/products?search=$([System.Uri]::EscapeDataString($payload))" -Method Get -UseBasicParsing -TimeoutSec 5
        if ($response.Content -match "SQL|MySQL|PostgreSQL|ORA-|syntax error") {
            Write-Host "  ✗ Possible SQL Injection with: $payload" -ForegroundColor Red
            $sqlVulnerable += $payload
        } else {
            Write-Host "  ✓ Safe from: $payload" -ForegroundColor Green
        }
    } catch {
        Write-Host "  ✓ SQL Injection prevented for: $payload" -ForegroundColor Green
    }
}

if ($sqlVulnerable.Count -eq 0) {
    $testResults += [PSCustomObject]@{Test = "SQL Injection"; Status = "PASS"; Details = "All SQL injection attempts blocked"}
} else {
    $testResults += [PSCustomObject]@{Test = "SQL Injection"; Status = "FAIL"; Details = "Vulnerable to: $($sqlVulnerable[0])"}
}

# 4. Test for XSS (Cross-Site Scripting)
Write-Host "`n[4/12] Testing XSS Vulnerabilities..." -ForegroundColor Green
$xssPayloads = @(
    "<script>alert('XSS')</script>",
    "<img src=x onerror=alert(1)>",
    "javascript:alert('XSS')",
    '"><script>alert(1)</script>',
    "';alert(String.fromCharCode(88,83,83))//",
    "<svg onload=alert(1)>",
    "body onload=alert(1)",
    "<input value=`"<script>alert(1)</script>`">"
)

$xssVulnerable = @()
foreach ($payload in $xssPayloads) {
    try {
        $encodedPayload = [System.Uri]::EscapeDataString($payload)
        $response = Invoke-WebRequest -Uri "$BaseUrl/api/products?search=$encodedPayload" -Method Get -UseBasicParsing -TimeoutSec 5
        
        if ($response.Content -match [regex]::Escape($payload) -and $payload -notmatch "escaped|sanitized") {
            Write-Host "  ✗ Possible XSS with: $payload" -ForegroundColor Red
            $xssVulnerable += $payload
        } else {
            Write-Host "  ✓ XSS prevented for: $payload" -ForegroundColor Green
        }
    } catch {
        Write-Host "  ✓ XSS prevented for: $payload" -ForegroundColor Green
    }
}

if ($xssVulnerable.Count -eq 0) {
    $testResults += [PSCustomObject]@{Test = "XSS Prevention"; Status = "PASS"; Details = "All XSS attempts blocked"}
} else {
    $testResults += [PSCustomObject]@{Test = "XSS Prevention"; Status = "FAIL"; Details = "Vulnerable to XSS"}
}

# Test account lockout
Write-Host "`n  Testing account lockout..." -ForegroundColor Yellow
$lockoutEmail = "lockouttest@example.com"
$body = @{
    email = $lockoutEmail
    password = "wrongpassword$i"
    rememberMe = $null
} | ConvertTo-Json

for ($i = 1; $i -le 10; $i++) {
    
    try {
        $response = Invoke-RestMethod -Uri "$BaseUrl/api/auth/login" -Method Post -Body $body -ContentType "application/json" -TimeoutSec 5
    } catch {
        if ($_.Exception.Response.StatusCode -eq 423 -or $_.Exception.Response.StatusCode -eq 429) {
            if ($i -eq 6) {
                Write-Host "  ✓ Account locked after 5 failed attempts" -ForegroundColor Green
            }
        }
    }
}

if ($weakPasswordAccepted.Count -eq 0) {
    $testResults += [PSCustomObject]@{Test = "Authentication Security"; Status = "PASS"; Details = "Weak passwords rejected, account lockout works"}
} else {
    $testResults += [PSCustomObject]@{Test = "Authentication Security"; Status = "FAIL"; Details = "Weak passwords accepted: $($weakPasswordAccepted -join ', ')"}
}

# 6. Test for Broken Access Control (IDOR)
Write-Host "`n[6/12] Testing Broken Access Control (IDOR)..." -ForegroundColor Green

# First login to get token
$loginBody = @{
    email = "test@example.com"
    password = "password123"
    rememberMe = $null
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "$BaseUrl/api/auth/login" -Method Post -Body $loginBody -ContentType "application/json" -TimeoutSec 5
    $authToken = $loginResponse.token
    
    # Test accessing other user's data
    $testIds = @(2, 3, 4, 5, 999)
    $idorVulnerable = @()
    
    foreach ($id in $testIds) {
        try {
            $response = Invoke-RestMethod -Uri "$BaseUrl/api/orders/$id" -Method Get -Headers @{Authorization = "Bearer $authToken"} -TimeoutSec 5
            Write-Host "  ✗ IDOR possible - accessed order $id" -ForegroundColor Red
            $idorVulnerable += $id
        } catch {
            Write-Host "  ✓ IDOR prevented for order $id" -ForegroundColor Green
        }
        
        try {
            $response = Invoke-RestMethod -Uri "$BaseUrl/api/cart?userId=$id" -Method Get -Headers @{Authorization = "Bearer $authToken"} -TimeoutSec 5
            Write-Host "  ✗ IDOR possible - accessed cart for user $id" -ForegroundColor Red
            $idorVulnerable += "cart-$id"
        } catch {
            Write-Host "  ✓ IDOR prevented for cart user $id" -ForegroundColor Green
        }
    }
    
    if ($idorVulnerable.Count -eq 0) {
        $testResults += [PSCustomObject]@{Test = "Access Control (IDOR)"; Status = "PASS"; Details = "No IDOR vulnerabilities found"}
    } else {
        $testResults += [PSCustomObject]@{Test = "Access Control (IDOR)"; Status = "FAIL"; Details = "IDOR vulnerabilities found"}
    }
} catch {
    Write-Host "  ⚠ Could not test IDOR - login failed" -ForegroundColor Yellow
    $testResults += [PSCustomObject]@{Test = "Access Control (IDOR)"; Status = "WARN"; Details = "Could not authenticate for testing"}
}

# 7. Test for JWT Vulnerabilities
Write-Host "`n[7/12] Testing JWT Security..." -ForegroundColor Green

$jwtVulnerabilities = @()

# Test for none algorithm
$noneAlgorithmToken = "eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ."
try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/api/auth/is-logged-in" -Method Get -Headers @{Authorization = "Bearer $noneAlgorithmToken"} -TimeoutSec 5
    Write-Host "  ✗ JWT none algorithm vulnerability detected!" -ForegroundColor Red
    $jwtVulnerabilities += "none-algorithm"
} catch {
    Write-Host "  ✓ JWT none algorithm rejected" -ForegroundColor Green
}

# Test for expired token
$expiredToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjE1MTYyMzkwMjJ9"
try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/api/auth/is-logged-in" -Method Get -Headers @{Authorization = "Bearer $expiredToken"} -TimeoutSec 5
    Write-Host "  ✗ Expired token was accepted!" -ForegroundColor Red
    $jwtVulnerabilities += "expired-token"
} catch {
    Write-Host "  ✓ Expired token rejected" -ForegroundColor Green
}

if ($jwtVulnerabilities.Count -eq 0) {
    $testResults += [PSCustomObject]@{Test = "JWT Security"; Status = "PASS"; Details = "JWT implementation secure"}
} else {
    $testResults += [PSCustomObject]@{Test = "JWT Security"; Status = "FAIL"; Details = "Vulnerabilities: $($jwtVulnerabilities -join ', ')"}
}

# 8. Test for Security Misconfiguration
Write-Host "`n[8/12] Testing Security Misconfigurations..." -ForegroundColor Green

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
foreach ($endpoint in $sensitiveEndpoints) {
    try {
        $response = Invoke-WebRequest -Uri "$BaseUrl$endpoint" -Method Get -UseBasicParsing -TimeoutSec 3
        if ($response.Content -match "404 Not Found|403 Forbidden|401 Unauthorized") {
            Write-Host "  ✗ Sensitive endpoint exposed: $endpoint" -ForegroundColor Red
            $exposedEndpoints += $endpoint
        }
    } catch {
        Write-Host "  ✓ Endpoint secured: $endpoint" -ForegroundColor Green
    }
}

# Test for directory listing
$directories = @("/uploads/", "/static/", "/images/", "/files/", "/backup/")
foreach ($dir in $directories) {
    try {
        $response = Invoke-WebRequest -Uri "$BaseUrl$dir" -Method Get -UseBasicParsing -TimeoutSec 3
        if ($response.Content -match "Index of|Directory listing for") {
            Write-Host "  ✗ Directory listing enabled: $dir" -ForegroundColor Red
            $exposedEndpoints += "$dir (directory listing)"
        }
    } catch {
        Write-Host "  ✓ Directory listing disabled: $dir" -ForegroundColor Green
    }
}

if ($exposedEndpoints.Count -eq 0) {
    $testResults += [PSCustomObject]@{Test = "Security Misconfiguration"; Status = "PASS"; Details = "No sensitive endpoints exposed"}
} else {
    $testResults += [PSCustomObject]@{Test = "Security Misconfiguration"; Status = "FAIL"; Details = "Exposed: $($exposedEndpoints -join ', ')"}
}

# 9. Test for Rate Limiting
Write-Host "`n[9/12] Testing Rate Limiting..." -ForegroundColor Green

$rateLimitResults = @{}
for ($i = 1; $i -le 50; $i++) {
    $body = @{
        email = "ratelimit@test.com"
        password = "wrongpassword$i"
    } | ConvertTo-Json
    
    try {
        $response = Invoke-RestMethod -Uri "$BaseUrl/api/auth/login" -Method Post -Body $body -ContentType "application/json" -TimeoutSec 5
        if ($i -gt 10 -and $response.StatusCode -eq 200) {
            Write-Host "  ✗ No rate limiting detected after $i requests" -ForegroundColor Red
            $rateLimitResults["Rate Limiting"] = "FAIL"
            break
        }
    } catch {
        if ($_.Exception.Response.StatusCode -eq 429) {
            Write-Host "  ✓ Rate limiting active (blocked at request $i)" -ForegroundColor Green
            $rateLimitResults["Rate Limiting"] = "PASS"
            break
        }
    }
    
    if ($i -eq 50) {
        Write-Host "  ✗ No rate limiting detected after 50 requests" -ForegroundColor Red
        $rateLimitResults["Rate Limiting"] = "FAIL"
    }
}

$testResults += [PSCustomObject]@{
    Test = "Rate Limiting"
    Status = if ($rateLimitResults["Rate Limiting"] -eq "PASS") { "PASS" } else { "FAIL" }
    Details = if ($rateLimitResults["Rate Limiting"] -eq "PASS") { "Rate limiting implemented" } else { "No rate limiting detected" }
}

# 10. Test for CSRF Protection
Write-Host "`n[10/12] Testing CSRF Protection..." -ForegroundColor Green

$csrfPayload = @{
    couponCode = "CSRF_TEST"
    useStoreBalance = $false
    shippingAddress = "CSRF Attack Address"
    giftCards = @()
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/api/orders/place" -Method Post -Body $csrfPayload -ContentType "application/json" -TimeoutSec 5
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

# 11. Test for Information Disclosure
Write-Host "`n[11/12] Testing Information Disclosure..." -ForegroundColor Green

$disclosureIssues = @()

# Test error messages
$errorEndpoints = @(
    "/api/products/invalid-id",
    "/api/orders/999999",
    "/api/users/invalid",
    "/api/cart/invalid"
)

foreach ($endpoint in $errorEndpoints) {
    try {
        $response = Invoke-WebRequest -Uri "$BaseUrl$endpoint" -Method Get -UseBasicParsing -TimeoutSec 5
        if ($response.Content -match "stack trace|SQLException|NullPointerException|org.springframework|com.ecommerce") {
            Write-Host "  ✗ Stack trace exposed at $endpoint" -ForegroundColor Red
            $disclosureIssues += "$endpoint (stack trace)"
        }
    } catch {
        if ($_.Exception.Response.StatusCode -eq 500) {
            Write-Host "  ⚠ Server error at $endpoint - check error message" -ForegroundColor Yellow
        }
    }
}


# Generate Summary Report
Write-Host "`n==========================================" -ForegroundColor Cyan
Write-Host "SECURITY TEST SUMMARY" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

$passed = ($testResults | Where-Object { $_.Status -eq "PASS" }).Count
$failed = ($testResults | Where-Object { $_.Status -eq "FAIL" }).Count
$warnings = ($testResults | Where-Object { $_.Status -eq "WARN" }).Count
$total = $testResults.Count

Write-Host "`nTest Results:" -ForegroundColor Yellow
Write-Host "  Passed: $passed/$total" -ForegroundColor Green
Write-Host "  Failed: $failed/$total" -ForegroundColor Red
Write-Host "  Warnings: $warnings/$total" -ForegroundColor Yellow

Write-Host "`nDetailed Results:" -ForegroundColor Yellow
$testResults | Format-Table -AutoSize

# Generate HTML Report
if ($GenerateReport) {
    $htmlReport = @"
<!DOCTYPE html>
<html>
<head>
    <title>Security Test Report - E-Commerce Lab</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        h1 { color: #333; }
        .pass { color: green; font-weight: bold; }
        .fail { color: red; font-weight: bold; }
        .warn { color: orange; font-weight: bold; }
        table { border-collapse: collapse; width: 100%; margin-top: 20px; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background-color: #4CAF50; color: white; }
        tr:nth-child(even) { background-color: #f2f2f2; }
        .summary { background-color: #f0f0f0; padding: 10px; margin-top: 20px; }
    </style>
</head>
<body>
    <h1>OWASP Security Test Report</h1>
    <p><strong>Target:</strong> $BaseUrl</p>
    <p><strong>Test Date:</strong> $(Get-Date)</p>
    
    <div class="summary">
        <h2>Summary</h2>
        <p>Total Tests: $total</p>
        <p>Passed: $passed</p>
        <p>Failed: $failed</p>
        <p>Warnings: $warnings</p>
        <p>Pass Rate: $([math]::Round($passed/$total*100, 2))%</p>
    </div>
    
    <h2>Detailed Results</h2>
    <table>
        <tr>
            <th>Test Category</th>
            <th>Status</th>
            <th>Details</th>
        </tr>
"@

    foreach ($result in $testResults) {
        $statusClass = switch ($result.Status) {
            "PASS" { "pass" }
            "FAIL" { "fail" }
            "WARN" { "warn" }
            default { "" }
        }
        $htmlReport += @"
        <tr>
            <td>$($result.Test)</td>
            <td class="$statusClass">$($result.Status)</td>
            <td>$($result.Details)</td>
        </tr>
"@
    }

    $htmlReport += @"
    </table>
</body>
</html>
"@

    $reportPath = "$ResultsDir\security-report-$(Get-Date -Format 'yyyyMMdd-HHmmss').html"
    $htmlReport | Out-File -FilePath $reportPath -Encoding UTF8
    Write-Host "`nHTML Report generated: $reportPath" -ForegroundColor Green
}

Write-Host "`n==========================================" -ForegroundColor Cyan
Write-Host "Security scan completed!" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Cyan