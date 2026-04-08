# test-sql-injection.ps1
param(
    [string]$BaseUrl = "http://localhost:8080"
)

Write-Host "SQL Injection Test Suite" -ForegroundColor Cyan
Write-Host "========================" -ForegroundColor Cyan

$sqlPayloads = @{
    "Authentication Bypass" = @(
        "' OR '1'='1",
        "admin' --",
        "admin' #",
        "admin'/*",
        "' OR 1=1 --",
        "' OR '1'='1' --"
    )
    
    "Union-Based" = @(
        "1 UNION SELECT null--",
        "1 UNION SELECT null,null--",
        "1 UNION SELECT 1,2,3--",
        "1 UNION SELECT user,password FROM users--"
    )
    
    "Boolean-Based" = @(
        "1 AND 1=1",
        "1 AND 1=2",
        "1' AND '1'='1",
        "1' AND '1'='2"
    )
    
    "Time-Based" = @(
        "1; WAITFOR DELAY '0:0:5'--",
        "1 AND SLEEP(5)",
        "1' AND SLEEP(5)--",
        "1'; WAITFOR DELAY '0:0:5'--"
    )
    
    "Database Enumeration" = @(
        "1; SELECT @@version--",
        "1' UNION SELECT version()--",
        "1' UNION SELECT database()--",
        "1' UNION SELECT user()--"
    )
}

$results = @()

foreach ($category in $sqlPayloads.Keys) {
    Write-Host "`nTesting $category..." -ForegroundColor Yellow
    
    foreach ($payload in $sqlPayloads[$category]) {
        $encodedPayload = [System.Uri]::EscapeDataString($payload)
        $url = "$BaseUrl/api/products?search=$encodedPayload"
        
        try {
            $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
            $response = Invoke-WebRequest -Uri $url -Method Get -UseBasicParsing -TimeoutSec 10
            $stopwatch.Stop()
            
            $vulnerable = $false
            $reason = ""
            
            # Check for database error messages
            if ($response.Content -match "SQL syntax|MySQL|PostgreSQL|ORA-|SQLite|syntax error") {
                $vulnerable = $true
                $reason = "Database error message exposed"
            }
            
            # Check for time-based vulnerability
            if ($stopwatch.ElapsedMilliseconds -gt 5000 -and $payload -match "SLEEP|WAITFOR|DELAY") {
                $vulnerable = $true
                $reason = "Time-based injection successful ($($stopwatch.ElapsedMilliseconds)ms)"
            }
            
            # Check for content differences
            if ($payload -match "AND 1=1" -or $payload -match "AND 1=2") {
                $response2 = Invoke-WebRequest -Uri $url.Replace("AND 1=1", "AND 1=2") -Method Get -UseBasicParsing -TimeoutSec 10
                if ($response.Content.Length -ne $response2.Content.Length) {
                    $vulnerable = $true
                    $reason = "Boolean-based injection possible (content length difference)"
                }
            }
            
            if ($vulnerable) {
                Write-Host "  ✗ VULNERABLE: $payload" -ForegroundColor Red
                Write-Host "    Reason: $reason" -ForegroundColor Red
                $results += [PSCustomObject]@{
                    Category = $category
                    Payload = $payload
                    Status = "VULNERABLE"
                    Reason = $reason
                    ResponseTime = $stopwatch.ElapsedMilliseconds
                }
            } else {
                Write-Host "  ✓ Safe: $payload" -ForegroundColor Green
                $results += [PSCustomObject]@{
                    Category = $category
                    Payload = $payload
                    Status = "SAFE"
                    Reason = "Injection blocked"
                    ResponseTime = $stopwatch.ElapsedMilliseconds
                }
            }
        } catch {
            Write-Host "  ✓ Blocked: $payload" -ForegroundColor Green
            $results += [PSCustomObject]@{
                Category = $category
                Payload = $payload
                Status = "BLOCKED"
                Reason = "Request rejected"
                ResponseTime = $null
            }
        }
    }
}

# Generate Report
$vulnerableCount = ($results | Where-Object { $_.Status -eq "VULNERABLE" }).Count
$safeCount = ($results | Where-Object { $_.Status -eq "SAFE" }).Count
$blockedCount = ($results | Where-Object { $_.Status -eq "BLOCKED" }).Count

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "SQL Injection Test Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Vulnerable: $vulnerableCount" -ForegroundColor Red
Write-Host "Safe: $safeCount" -ForegroundColor Green
Write-Host "Blocked: $blockedCount" -ForegroundColor Yellow

if ($vulnerableCount -gt 0) {
    Write-Host "`n⚠ SQL INJECTION VULNERABILITIES DETECTED!" -ForegroundColor Red
    $results | Where-Object { $_.Status -eq "VULNERABLE" } | Format-Table -AutoSize
} else {
    Write-Host "`n✓ No SQL injection vulnerabilities detected" -ForegroundColor Green
}

# Save results
# $results | Export-Csv -Path "sql-injection-results.csv" -NoTypeInformation
# Write-Host "`nResults saved to sql-injection-results.csv" -ForegroundColor Green