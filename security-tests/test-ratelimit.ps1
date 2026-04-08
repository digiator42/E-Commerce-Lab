# test-rate-limiting.ps1
param(
    [string]$BaseUrl = "http://localhost:8080",
    [int]$RequestCount = 100,
    [int]$ConcurrentUsers = 10
)

Write-Host "Rate Limiting Test Suite" -ForegroundColor Cyan
Write-Host "=======================" -ForegroundColor Cyan

$results = @()
$endpoints = @(
    "/api/auth/login",
    "/api/products",
    "/api/cart/add/1",
    "/api/orders/place"
)

foreach ($endpoint in $endpoints) {
    Write-Host "`nTesting rate limiting for: $endpoint" -ForegroundColor Yellow
    
    $statusCodes = @{}
    $responseTimes = @()
    
    for ($i = 1; $i -le $RequestCount; $i++) {
        $method = if ($endpoint -eq "/api/auth/login") { "Post" } else { "Get" }
        
        try {
            $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
            
            if ($method -eq "Post") {
                $body = @{
                    email = "ratelimit_$i@test.com"
                    password = "wrongpassword"
                } | ConvertTo-Json
                
                $response = Invoke-WebRequest -Uri "$BaseUrl$endpoint" -Method Post -Body $body -ContentType "application/json" -UseBasicParsing -TimeoutSec 5
            } else {
                $response = Invoke-WebRequest -Uri "$BaseUrl$endpoint" -Method Get -UseBasicParsing -TimeoutSec 5
            }
            
            $stopwatch.Stop()
            $responseTimes += $stopwatch.ElapsedMilliseconds
            
            $statusCode = $response.StatusCode
            $statusCodes[$statusCode] = ($statusCodes[$statusCode] + 1)
            
            if ($statusCode -eq 429) {
                Write-Host "  Rate limit hit at request $i (HTTP 429)" -ForegroundColor Yellow
                break
            }
            
            if ($i % 10 -eq 0) {
                Write-Host "  Completed $i requests..." -ForegroundColor Gray
            }
        } catch {
            $stopwatch.Stop()
            if ($_.Exception.Response.StatusCode -eq 429) {
                Write-Host "  Rate limit hit at request $i (HTTP 429)" -ForegroundColor Yellow
                $statusCodes[429] = ($statusCodes[429] + 1)
                break
            } else {
                $statusCodes[$_.Exception.Response.StatusCode] = ($statusCodes[$_.Exception.Response.StatusCode] + 1)
            }
        }
    }
    
    $avgResponseTime = if ($responseTimes.Count -gt 0) { [math]::Round(($responseTimes | Measure-Object -Average).Average, 2) } else { 0 }
    
    $results += [PSCustomObject]@{
        Endpoint = $endpoint
        TotalRequests = $RequestCount
        StatusCodes = ($statusCodes.Keys | ForEach-Object { "$($_):$($statusCodes[$_])" }) -join ", "
        HasRateLimit = [bool]$statusCodes[429]
        RateLimitAt = if ($statusCodes[429]) { "Request $($statusCodes[429])" } else { "Not triggered" }
        AvgResponseTime = "$avgResponseTime ms"
    }
    
    if ($statusCodes[429]) {
        Write-Host "  ✓ Rate limiting active for $endpoint" -ForegroundColor Green
    } else {
        Write-Host "  ⚠ No rate limiting detected for $endpoint" -ForegroundColor Red
    }
}

# Concurrent request test
Write-Host "`nTesting concurrent rate limiting..." -ForegroundColor Yellow

$runspacePool = [runspacefactory]::CreateRunspacePool(1, $ConcurrentUsers)
$runspacePool.Open()
$jobs = @()

for ($i = 1; $i -le $ConcurrentUsers; $i++) {
    $powershell = [powershell]::Create()
    $powershell.RunspacePool = $runspacePool
    [void]$powershell.AddScript({
        param($url)
        try {
            $response = Invoke-WebRequest -Uri $url -Method Get -UseBasicParsing -TimeoutSec 5
            return $response.StatusCode
        } catch {
            return $_.Exception.Response.StatusCode
        }
    }).AddArgument("$BaseUrl/api/products")
    
    $jobs += [PSCustomObject]@{
        PowerShell = $powershell
        Handle = $powershell.BeginInvoke()
    }
}

$concurrentStatusCodes = @{}
foreach ($job in $jobs) {
    $result = $job.PowerShell.EndInvoke($job.Handle)
    $concurrentStatusCodes[$result] = ($concurrentStatusCodes[$result] + 1)
    $job.PowerShell.Dispose()
}
$runspacePool.Dispose()

$results += [PSCustomObject]@{
    Endpoint = "/api/products (Concurrent)"
    TotalRequests = $ConcurrentUsers
    StatusCodes = ($concurrentStatusCodes.Keys | ForEach-Object { "$($_):$($concurrentStatusCodes[$_])" }) -join ", "
    HasRateLimit = [bool]$concurrentStatusCodes[429]
    RateLimitAt = if ($concurrentStatusCodes[429]) { "Detected" } else { "Not detected" }
    AvgResponseTime = "N/A"
}

# Summary
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "Rate Limiting Test Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$results | Format-Table -AutoSize

$endpointsWithRateLimit = ($results | Where-Object { $_.HasRateLimit -eq $true }).Count
$totalEndpoints = $results.Count

Write-Host "`nEndpoints with rate limiting: $endpointsWithRateLimit/$totalEndpoints" -ForegroundColor Yellow

if ($endpointsWithRateLimit -eq $totalEndpoints) {
    Write-Host "✓ Rate limiting properly configured for all endpoints" -ForegroundColor Green
} elseif ($endpointsWithRateLimit -gt 0) {
    Write-Host "⚠ Rate limiting configured for some endpoints" -ForegroundColor Yellow
} else {
    Write-Host "✗ No rate limiting detected - CRITICAL!" -ForegroundColor Red
}