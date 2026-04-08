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

# Check if application is running
Write-Host "Checking application status..." -ForegroundColor Green
try {
    $response = Invoke-WebRequest -Uri "$BaseUrl/api/products" -Method Get -UseBasicParsing -TimeoutSec 5
    Write-Host "✓ Application is running" -ForegroundColor Green
    $testResults += [PSCustomObject]@{
        Test = "Application Status"
        Status = "PASS"
        Details = "Application is reachable"
    }
} catch {
    Write-Host "✗ Application is not running. Starting the application..." -ForegroundColor Red
    exit 1
}

# Test for Security Headers
Write-Host "`nTesting Security Headers..." -ForegroundColor Green
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


# Test account lockout
Write-Host "`nTesting account lockout..." -ForegroundColor Green
$lockoutEmail = "lockouttest@example.com"
$body = @{
    email = $lockoutEmail
    password = "wrongpassword$i"
    rememberMe = $null
} | ConvertTo-Json

$i = 1
while ($i -lt 6) {
    
    try {
        $response = Invoke-RestMethod -Uri "$BaseUrl/api/auth/login" -Method Post -Body $body -ContentType "application/json" -TimeoutSec 5
    } catch {
        if ($_.Exception.Response.StatusCode -eq 423 -or $_.Exception.Response.StatusCode -eq 429) {
            if ($i -eq 6) {
                Write-Host "  ✓ Account locked after 5 failed attempts" -ForegroundColor Green
            }
        }
    }
    $i++
}

if ($i -eq 6) {
    write-Host "  ✗ No account lockout detected after 5 failed attempts" -ForegroundColor Red
    $testResults += [PSCustomObject]@{Test = "Account Lockout"; Status = "FAIL"; Details = "No account lockout detected"}
} else {
    $testResults += [PSCustomObject]@{Test = "Account Lockout"; Status = "PASS"; Details = "Account locked after 5 failed attempts"}
}

# Test for Rate Limiting
Write-Host "`nTesting Rate Limiting..." -ForegroundColor Green

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

# Test for Security Misconfiguration
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

# Test for Information Disclosure
Write-Host "`nTesting Information Disclosure..." -ForegroundColor Green

$disclosureIssues = @()

# Test error messages
$errorEndpoints = @(
    "/api/products/invalid-id",
    "/api/orders/999999",
    "/api/users/invalid",
    "/api/cart/add/invalid"
)

foreach ($endpoint in $errorEndpoints) {
    $response = Invoke-WebRequest -Uri "$BaseUrl$endpoint" -Headers @{Authorization = "Bearer $authToken"} -Method Get -TimeoutSec 5 -SkipHttpErrorCheck

    Write-Host "Status: $($response.StatusCode)"

    if ($response.Content -match "stack trace|SQLException|NullPointerException|org.springframework|com.ecommerce") {
        Write-Host "  ✗ Stack trace exposed at $endpoint" -ForegroundColor Red
        $disclosureIssues += "$endpoint (stack trace)"
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