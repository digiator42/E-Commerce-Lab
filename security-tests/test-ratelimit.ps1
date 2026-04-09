# test-rate-limiting.ps1
param(
    [string]$BaseUrl        = "http://localhost:8080",
    [int]   $RequestCount   = 100,
    [int]   $ConcurrentUsers = 10
)

Write-Host "Rate Limiting Test Suite" -ForegroundColor Cyan
Write-Host "=======================" -ForegroundColor Cyan

$results   = @()
$endpoints = @(
    "/api/auth/login",
    "/api/products",
    "/api/cart/add/1",
    "/api/orders/place"
)

foreach ($endpoint in $endpoints) {
    Write-Host "`nTesting rate limiting for: $endpoint" -ForegroundColor Yellow

    $statusCodes   = @{}
    $responseTimes = @()

    for ($i = 1; $i -le $RequestCount; $i++) {
        $method = if ($endpoint -eq "/api/auth/login") { "Post" } else { "Get" }

        try {
            $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()

            if ($method -eq "Post") {
                $body = @{ email = "ratelimit_$i@test.com"; password = "wrongpassword" } | ConvertTo-Json
                $response = Invoke-WebRequest -Uri "$BaseUrl$endpoint" -Method Post -Body $body -ContentType "application/json" -UseBasicParsing -TimeoutSec 5
            } else {
                $response = Invoke-WebRequest -Uri "$BaseUrl$endpoint" -Method Get -UseBasicParsing -TimeoutSec 5
            }

            $stopwatch.Stop()
            $responseTimes += $stopwatch.ElapsedMilliseconds
            $sc = [int]$response.StatusCode
            $statusCodes[$sc] = ($statusCodes[$sc] + 1)

            if ($sc -eq 429) {
                Write-Host "  Rate limit hit at request $i (HTTP 429)" -ForegroundColor Yellow
                break
            }

            if ($i % 10 -eq 0) { Write-Host "  Completed $i requests..." -ForegroundColor Gray }
        } catch {
            $stopwatch.Stop()
            $exSc = [int]$_.Exception.Response.StatusCode
            if ($exSc -eq 429) {
                Write-Host "  Rate limit hit at request $i (HTTP 429)" -ForegroundColor Yellow
                $statusCodes[429] = ($statusCodes[429] + 1)
                break
            } else {
                $statusCodes[$exSc] = ($statusCodes[$exSc] + 1)
            }
        }
    }

    $avgResponseTime = if ($responseTimes.Count -gt 0) { [math]::Round(($responseTimes | Measure-Object -Average).Average, 2) } else { 0 }
    $hasRateLimit    = [bool]$statusCodes[429]

    $results += [PSCustomObject]@{
        Endpoint        = $endpoint
        TotalRequests   = $RequestCount
        StatusCodes     = ($statusCodes.Keys | ForEach-Object { "$($_):$($statusCodes[$_])" }) -join ", "
        HasRateLimit    = $hasRateLimit
        RateLimitAt     = if ($hasRateLimit) { "Request $($statusCodes[429])" } else { "Not triggered" }
        AvgResponseTime = "$avgResponseTime ms"
    }

    if ($hasRateLimit) {
        Write-Host "  [PASS] Rate limiting active for $endpoint" -ForegroundColor Green
    } else {
        Write-Host "  [WARN] No rate limiting detected for $endpoint" -ForegroundColor Red
    }
}

# Concurrent request test
Write-Host "`nTesting concurrent rate limiting..." -ForegroundColor Yellow

$runspacePool = [runspacefactory]::CreateRunspacePool(1, $ConcurrentUsers)
$runspacePool.Open()
$jobs = @()

for ($i = 1; $i -le $ConcurrentUsers; $i++) {
    $ps = [powershell]::Create()
    $ps.RunspacePool = $runspacePool
    [void]$ps.AddScript({
        param($url)
        try {
            $r = Invoke-WebRequest -Uri $url -Method Get -UseBasicParsing -TimeoutSec 5
            return [int]$r.StatusCode
        } catch {
            return [int]$_.Exception.Response.StatusCode
        }
    }).AddArgument("$BaseUrl/api/products")
    $jobs += [PSCustomObject]@{ PowerShell = $ps; Handle = $ps.BeginInvoke() }
}

$concurrentSc = @{}
foreach ($job in $jobs) {
    $r = $job.PowerShell.EndInvoke($job.Handle)
    $concurrentSc[$r] = ($concurrentSc[$r] + 1)
    $job.PowerShell.Dispose()
}
$runspacePool.Dispose()

$results += [PSCustomObject]@{
    Endpoint        = "/api/products (Concurrent)"
    TotalRequests   = $ConcurrentUsers
    StatusCodes     = ($concurrentSc.Keys | ForEach-Object { "$($_):$($concurrentSc[$_])" }) -join ", "
    HasRateLimit    = [bool]$concurrentSc[429]
    RateLimitAt     = if ($concurrentSc[429]) { "Detected" } else { "Not detected" }
    AvgResponseTime = "N/A"
}

$endpointsWithRL = ($results | Where-Object { $_.HasRateLimit -eq $true }).Count
$totalEndpoints  = $results.Count

Write-Host "`n========================================"  -ForegroundColor Cyan
Write-Host "Rate Limiting Test Summary"                 -ForegroundColor Cyan
Write-Host "========================================"  -ForegroundColor Cyan
$results | Format-Table -AutoSize
Write-Host "`nEndpoints with rate limiting: $endpointsWithRL/$totalEndpoints" -ForegroundColor Yellow

if ($endpointsWithRL -eq $totalEndpoints) {
    Write-Host "[OK] Rate limiting properly configured for all endpoints" -ForegroundColor Green
} elseif ($endpointsWithRL -gt 0) {
    Write-Host "[WARN] Rate limiting configured for some endpoints" -ForegroundColor Yellow
} else {
    Write-Host "[FAIL] No rate limiting detected - CRITICAL!" -ForegroundColor Red
}

# ── Structured return for run-all.ps1 ──────────────────────────────────────
$scorePercent = if ($totalEndpoints -gt 0) { [math]::Round($endpointsWithRL / $totalEndpoints * 100) } else { 0 }
$vulns        = @()
if ($endpointsWithRL -lt $totalEndpoints) {
    $vulns += "Rate limiting not implemented on all endpoints ($endpointsWithRL/$totalEndpoints protected)"
}

Write-Output ([PSCustomObject]@{
    TestName        = "Rate Limiting"
    Status          = if ($endpointsWithRL -eq $totalEndpoints -and $totalEndpoints -gt 0) { "PASS" }
                      elseif ($endpointsWithRL -gt 0) { "WARNING" }
                      else { "FAIL" }
    Score           = $endpointsWithRL
    MaxScore        = $totalEndpoints
    ScorePercent    = $scorePercent
    Details         = @{ "Protected Endpoints" = $endpointsWithRL; "Total Endpoints" = $totalEndpoints }
    Vulnerabilities = $vulns
})
