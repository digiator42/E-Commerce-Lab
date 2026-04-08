# test-xss.ps1
param(
    [string]$BaseUrl = "http://localhost:8080"
)

Write-Host "XSS (Cross-Site Scripting) Test Suite" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan

$xssPayloads = @{
    "Reflected XSS" = @(
        "<script>alert('XSS')</script>",
        "<img src=x onerror=alert(1)>",
        "<svg onload=alert(1)>",
        "javascript:alert('XSS')",
        '"><script>alert(1)</script>',
        "';alert(String.fromCharCode(88,83,83))//",
        "<body onload=alert(1)>",
        '<input value="<script>alert(1)</script>">' 
    )
    
    "DOM-Based XSS" = @(
        "#<script>alert(1)</script>",
        "#<img src=x onerror=alert(1)>",
        "#javascript:alert(1)",
        "#<svg/onload=alert(1)>"
    )
    
    "Stored XSS Simulation" = @(
        "User<script>alert(1)</script>",
        "Comment <img src=x onerror=alert(1)>",
        "Product <svg onload=alert(1)>",
        "Review <body onload=alert(1)>"
    )
    
    "Advanced XSS" = @(
        "<scr<script>ipt>alert(1)</scr</script>ipt>",
        "<SCRIPT>alert(String.fromCharCode(88,83,83))</SCRIPT>",
        "`"><img src=`"x`" onerror=`"alert(1)`">",
        ''';!--\"<XSS>=&{()}',
        "<IMG SRC=javascript:alert('XSS')>"
    )
}

$results = @()

foreach ($category in $xssPayloads.Keys) {
    Write-Host "`nTesting $category..." -ForegroundColor Yellow
    
    foreach ($payload in $xssPayloads[$category]) {
        # Test reflected XSS in search parameters
        $encodedPayload = [System.Uri]::EscapeDataString($payload)
        $url = "$BaseUrl/api/products?search=$encodedPayload"
        
        try {
            $response = Invoke-WebRequest -Uri $url -Method Get -UseBasicParsing -TimeoutSec 10
            
            # Check if payload is reflected without sanitization
            if ($response.Content -match [regex]::Escape($payload)) {
                Write-Host "  ✗ XSS VULNERABLE: $payload" -ForegroundColor Red
                Write-Host "    Payload reflected in response" -ForegroundColor Red
                $results += [PSCustomObject]@{
                    Category = $category
                    Payload = $payload
                    Endpoint = $url
                    Status = "VULNERABLE"
                    Reason = "Payload reflected unescaped"
                }
            } else {
                Write-Host "  ✓ Safe: $payload" -ForegroundColor Green
                $results += [PSCustomObject]@{
                    Category = $category
                    Payload = $payload
                    Endpoint = $url
                    Status = "SAFE"
                    Reason = "Payload sanitized"
                }
            }
        } catch {
            Write-Host "  ✓ Blocked: $payload" -ForegroundColor Green
            $results += [PSCustomObject]@{
                Category = $category
                Payload = $payload
                Endpoint = $url
                Status = "BLOCKED"
                Reason = "Request rejected"
            }
        }
        
        # Test registration with XSS payload
        if ($category -eq "Stored XSS Simulation") {
            $registerBody = @{
                displayName = $payload
                username = "xssuser"
                address = "Test Address"
                age = 25
                email = "xss_$(Get-Random)@test.com"
                password = "Password123!"
            } | ConvertTo-Json
            
            try {
                $response = Invoke-RestMethod -Uri "$BaseUrl/api/auth/register" -Method Post -Body $registerBody -ContentType "application/json" -TimeoutSec 10
                Write-Host "  ⚠ Stored XSS possible with: $payload" -ForegroundColor Yellow
                $results += [PSCustomObject]@{
                    Category = $category
                    Payload = $payload
                    Endpoint = "/api/auth/register"
                    Status = "WARNING"
                    Reason = "Payload stored - verify display"
                }
            } catch {
                Write-Host "  ✓ Stored XSS prevented: $payload" -ForegroundColor Green
            }
        }
    }
}

# Generate Summary
$vulnerableCount = ($results | Where-Object { $_.Status -eq "VULNERABLE" }).Count
$warningCount = ($results | Where-Object { $_.Status -eq "WARNING" }).Count

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "XSS Test Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Vulnerable: $vulnerableCount" -ForegroundColor Red
Write-Host "Warnings: $warningCount" -ForegroundColor Yellow

if ($vulnerableCount -gt 0) {
    Write-Host "`n⚠ XSS VULNERABILITIES DETECTED!" -ForegroundColor Red
    $results | Where-Object { $_.Status -eq "VULNERABLE" } | Format-Table -AutoSize
} else {
    Write-Host "`n✓ No XSS vulnerabilities detected" -ForegroundColor Green
}
