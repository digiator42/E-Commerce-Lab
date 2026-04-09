# test-jwt-security.ps1
param(
    [string]$BaseUrl = "http://localhost:8080"
)

Write-Host "JWT Security Test Suite" -ForegroundColor Cyan
Write-Host "======================" -ForegroundColor Cyan

$loginBody = @{
    email    = "hassan@example.com"
    password = "password123"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "$BaseUrl/api/auth/login" -Method Post -Body $loginBody -ContentType "application/json" -TimeoutSec 5
    $validToken = $loginResponse.token
    Write-Host "[PASS] Obtained valid JWT token" -ForegroundColor Green
} catch {
    Write-Host "[FAIL] Could not obtain token. Please ensure test user exists." -ForegroundColor Red
    Write-Output ([PSCustomObject]@{
        TestName        = "JWT Security"
        Status          = "FAIL"
        Score           = 0; MaxScore = 1; ScorePercent = 0
        Details         = @{ Error = "Could not obtain login token" }
        Vulnerabilities = @("Login failed - cannot test JWT")
    })
    exit 1
}

$testResults = @()

# Test 1: None Algorithm
Write-Host "`nTest 1: None Algorithm Attack" -ForegroundColor Yellow
$noneToken = "eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwiaWF0IjoxNTE2MjM5MDIyfQ."
try {
    $null = Invoke-RestMethod -Uri "$BaseUrl/api/auth/is-logged-in" -Method Get -Headers @{Authorization = "Bearer $noneToken"} -TimeoutSec 5
    Write-Host "  [FAIL] VULNERABLE: Token with 'none' algorithm was accepted!" -ForegroundColor Red
    $testResults += [PSCustomObject]@{ Test = "None Algorithm"; Status = "FAIL"; Details = "Accepted token with none algorithm" }
} catch {
    Write-Host "  [PASS] Secure: Token with 'none' algorithm rejected" -ForegroundColor Green
    $testResults += [PSCustomObject]@{ Test = "None Algorithm"; Status = "PASS"; Details = "None algorithm rejected" }
}

# Test 2: Expired Token
Write-Host "`nTest 2: Expired Token" -ForegroundColor Yellow
$expiredToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjE1MTYyMzkwMjJ9"
try {
    $null = Invoke-RestMethod -Uri "$BaseUrl/api/auth/is-logged-in" -Method Get -Headers @{Authorization = "Bearer $expiredToken"} -TimeoutSec 5
    Write-Host "  [FAIL] VULNERABLE: Expired token was accepted!" -ForegroundColor Red
    $testResults += [PSCustomObject]@{ Test = "Expired Token"; Status = "FAIL"; Details = "Expired token accepted" }
} catch {
    Write-Host "  [PASS] Secure: Expired token rejected" -ForegroundColor Green
    $testResults += [PSCustomObject]@{ Test = "Expired Token"; Status = "PASS"; Details = "Expired token rejected" }
}

# Test 3: Tampered Token
Write-Host "`nTest 3: Token Tampering" -ForegroundColor Yellow
$parts = $validToken.Split('.')
if ($parts.Length -eq 3) {
    $tamperedPayload = [Convert]::ToBase64String([System.Text.Encoding]::UTF8.GetBytes('{"sub":"admin@example.com","role":"ADMIN"}'))
    $tamperedToken   = "$($parts[0]).$tamperedPayload.$($parts[2])"
    try {
        $null = Invoke-RestMethod -Uri "$BaseUrl/api/auth/is-logged-in" -Method Get -Headers @{Authorization = "Bearer $tamperedToken"} -TimeoutSec 5
        Write-Host "  [FAIL] VULNERABLE: Tampered token was accepted!" -ForegroundColor Red
        $testResults += [PSCustomObject]@{ Test = "Token Tampering"; Status = "FAIL"; Details = "Tampered token accepted" }
    } catch {
        Write-Host "  [PASS] Secure: Tampered token rejected" -ForegroundColor Green
        $testResults += [PSCustomObject]@{ Test = "Token Tampering"; Status = "PASS"; Details = "Tampered token rejected" }
    }
}

# Test 4: Missing Token
Write-Host "`nTest 4: Missing Token" -ForegroundColor Yellow
try {
    $null = Invoke-RestMethod -Uri "$BaseUrl/api/auth/is-logged-in" -Method Get -TimeoutSec 5
    Write-Host "  [FAIL] INSECURE: Endpoint accessible without token!" -ForegroundColor Red
    $testResults += [PSCustomObject]@{ Test = "Authentication Required"; Status = "FAIL"; Details = "Endpoint accessible without token" }
} catch {
    Write-Host "  [PASS] Secure: Token required for access" -ForegroundColor Green
    $testResults += [PSCustomObject]@{ Test = "Authentication Required"; Status = "PASS"; Details = "Token required" }
}

# Test 5: Wrong Signature
Write-Host "`nTest 5: Wrong Signature" -ForegroundColor Yellow
$wrongSignatureToken = $validToken.Substring(0, $validToken.Length - 5) + "xxxxx"
try {
    $null = Invoke-RestMethod -Uri "$BaseUrl/api/auth/is-logged-in" -Method Get -Headers @{Authorization = "Bearer $wrongSignatureToken"} -TimeoutSec 5
    Write-Host "  [FAIL] VULNERABLE: Token with wrong signature was accepted!" -ForegroundColor Red
    $testResults += [PSCustomObject]@{ Test = "Signature Validation"; Status = "FAIL"; Details = "Wrong signature accepted" }
} catch {
    Write-Host "  [PASS] Secure: Wrong signature rejected" -ForegroundColor Green
    $testResults += [PSCustomObject]@{ Test = "Signature Validation"; Status = "PASS"; Details = "Signature validated" }
}

# Test 6: Privilege Escalation
Write-Host "`nTest 6: Privilege Escalation" -ForegroundColor Yellow
try {
    $null = Invoke-RestMethod -Uri "$BaseUrl/api/admin/users" -Method Get -Headers @{Authorization = "Bearer $validToken"} -TimeoutSec 5
    Write-Host "  [FAIL] VULNERABLE: Regular user accessed admin endpoint!" -ForegroundColor Red
    $testResults += [PSCustomObject]@{ Test = "Privilege Escalation"; Status = "FAIL"; Details = "Regular user accessed admin area" }
} catch {
    if ($_.Exception.Response.StatusCode -eq 403) {
        Write-Host "  [PASS] Secure: Admin access properly restricted" -ForegroundColor Green
        $testResults += [PSCustomObject]@{ Test = "Privilege Escalation"; Status = "PASS"; Details = "Role-based access enforced" }
    } else {
        Write-Host "  [WARN] Admin access check: HTTP $($_.Exception.Response.StatusCode)" -ForegroundColor Yellow
        $testResults += [PSCustomObject]@{ Test = "Privilege Escalation"; Status = "WARN"; Details = "Unexpected response: $($_.Exception.Response.StatusCode)" }
    }
}

# Test 7: Token Blacklisting
Write-Host "`nTest 7: Token Blacklisting" -ForegroundColor Yellow
try {
    Invoke-RestMethod -Uri "$BaseUrl/api/auth/logout" -Method Post -Headers @{Authorization = "Bearer $validToken"} -TimeoutSec 5
    Write-Host "  [PASS] Logged out successfully" -ForegroundColor Green
    try {
        $null = Invoke-RestMethod -Uri "$BaseUrl/api/auth/is-logged-in" -Method Get -Headers @{Authorization = "Bearer $validToken"} -TimeoutSec 5
        Write-Host "  [FAIL] VULNERABLE: Blacklisted token was accepted!" -ForegroundColor Red
        $testResults += [PSCustomObject]@{ Test = "Token Blacklisting"; Status = "FAIL"; Details = "Token still valid after logout" }
    } catch {
        Write-Host "  [PASS] Secure: Blacklisted token rejected" -ForegroundColor Green
        $testResults += [PSCustomObject]@{ Test = "Token Blacklisting"; Status = "PASS"; Details = "Token properly invalidated" }
    }
} catch {
    Write-Host "  [WARN] Could not test blacklisting: $($_.Exception.Message)" -ForegroundColor Yellow
    $testResults += [PSCustomObject]@{ Test = "Token Blacklisting"; Status = "WARN"; Details = "Unable to test" }
}

$passed = ($testResults | Where-Object { $_.Status -eq "PASS" }).Count
$failed  = ($testResults | Where-Object { $_.Status -eq "FAIL" }).Count

Write-Host "`n========================================"  -ForegroundColor Cyan
Write-Host "JWT Security Test Summary"                  -ForegroundColor Cyan
Write-Host "========================================"  -ForegroundColor Cyan
Write-Host "Passed: $passed"  -ForegroundColor Green
Write-Host "Failed: $failed"  -ForegroundColor Red

if ($failed -gt 0) {
    Write-Host "`n[!] JWT SECURITY ISSUES DETECTED!" -ForegroundColor Red
    $testResults | Where-Object { $_.Status -eq "FAIL" } | Format-Table -AutoSize
} else {
    Write-Host "`n[OK] JWT implementation appears secure" -ForegroundColor Green
}

# ── Structured return for run-all.ps1 ──────────────────────────────────────
$total       = $testResults.Count
$scorePercent = if ($total -gt 0) { [math]::Round($passed / $total * 100) } else { 0 }
$detailsHash  = @{}
foreach ($r in $testResults) { $detailsHash[$r.Test] = $r.Status }
$vulns = ($testResults | Where-Object { $_.Status -eq "FAIL" } | Select-Object -ExpandProperty Test)

Write-Output ([PSCustomObject]@{
    TestName        = "JWT Security"
    Status          = if ($failed -eq 0) { "PASS" } elseif ($failed -le 2) { "WARNING" } else { "FAIL" }
    Score           = $passed
    MaxScore        = $total
    ScorePercent    = $scorePercent
    Details         = $detailsHash
    Vulnerabilities = @($vulns)
})
