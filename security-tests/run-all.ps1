# run-all-security-tests.ps1
param(
    [string]$BaseUrl    = "http://localhost:8080",
    [switch]$SkipAppCheck
)

Write-Host @"
╔══════════════════════════════════════════════════════════════╗
║     E-Commerce Lab - OWASP Top 10 Security Test Suite        ║
╚══════════════════════════════════════════════════════════════╝
"@ -ForegroundColor Cyan

Write-Host "Target    : $BaseUrl"       -ForegroundColor Yellow
Write-Host "Start Time: $(Get-Date)"    -ForegroundColor Yellow
Write-Host ""

# ── Results directory ───────────────────────────────────────────────────────
$resultsDir = "security-test-results-$(Get-Date -Format 'yyyyMMdd-HHmmss')"
New-Item -ItemType Directory -Path $resultsDir -Force | Out-Null

# ── Pre-flight: is the app up? ──────────────────────────────────────────────
if (-not $SkipAppCheck) {
    Write-Host "Checking if application is running..." -ForegroundColor Yellow
    try {
        $null = Invoke-WebRequest -Uri "$BaseUrl/api/products" -Method Get -UseBasicParsing -TimeoutSec 5 -SkipCertificateCheck
        Write-Host "[OK] Application is running" -ForegroundColor Green
    } catch {
        Write-Host "[ERROR] Application is not running at $BaseUrl" -ForegroundColor Red
        Write-Host "Please start the application first with: ./mvnw spring-boot:run" -ForegroundColor Yellow
        exit 1
    }
}

# ── Helper: cast whatever a child script returned into a safe result object ─
function Resolve-TestResult {
    param($RawOutput, [string]$FallbackName)

    # Find the last PSCustomObject that looks like a result (has TestName + Status)
    $structured = $RawOutput | Where-Object {
        $_ -is [PSCustomObject] -and
        $_.PSObject.Properties['TestName'] -and
        $_.PSObject.Properties['Status']
    } | Select-Object -Last 1

    if ($structured) { return $structured }

    # Fallback: nothing usable came back
    return [PSCustomObject]@{
        TestName        = $FallbackName
        Status          = "ERROR"
        Score           = 0
        MaxScore        = 1
        ScorePercent    = 0
        Details         = @{ Error = "Script produced no structured result" }
        Vulnerabilities = @("Script did not return a result object")
    }
}

# ── Test suites ─────────────────────────────────────────────────────────────
$testSuites = @(
    @{ Name = "Init Security Scan"; Script = "init-test.ps1"       },
    @{ Name = "SQL Injection";   Script = "test-sqli.ps1"       },
    @{ Name = "XSS Testing";     Script = "test-xss.ps1"        },
    @{ Name = "JWT Security";    Script = "test-jwt.ps1"        },
    @{ Name = "Rate Limiting";   Script = "test-ratelimit.ps1"  }
)

$allTestResults = @()

foreach ($suite in $testSuites) {
    Write-Host "`n========================================" -ForegroundColor Cyan
    Write-Host "Running: $($suite.Name)"                  -ForegroundColor Cyan
    Write-Host "========================================"  -ForegroundColor Cyan

    $scriptPath = Join-Path $PSScriptRoot $suite.Script

    if (-not (Test-Path $scriptPath)) {
        Write-Host "[WARN] Script not found: $scriptPath" -ForegroundColor Yellow
        $allTestResults += [PSCustomObject]@{
            TestName        = $suite.Name
            Status          = "ERROR"
            Score           = 0; MaxScore = 1; ScorePercent = 0
            Details         = @{ Error = "Script file not found" }
            Vulnerabilities = @("Script not found: $($suite.Script)")
        }
        continue
    }

    # KEY FIX: pipe through a clean child scope so Write-Host goes to console
    # and Write-Output (PSCustomObject) is captured in $rawOutput.
    $rawOutput = & $scriptPath -BaseUrl $BaseUrl

    $testResult = Resolve-TestResult -RawOutput $rawOutput -FallbackName $suite.Name
    $allTestResults += $testResult

    # Per-suite console summary
    $color = switch ($testResult.Status) { "PASS" { "Green" } "WARNING" { "Yellow" } default { "Red" } }
    Write-Host "`n--- Summary: $($suite.Name) ---"                                            -ForegroundColor Yellow
    Write-Host "Status : $($testResult.Status)"                                              -ForegroundColor $color
    Write-Host "Score  : $($testResult.Score)/$($testResult.MaxScore) ($($testResult.ScorePercent)%)" -ForegroundColor Cyan
    if ($testResult.Vulnerabilities.Count -gt 0) {
        Write-Host "Issues : $($testResult.Vulnerabilities.Count)" -ForegroundColor Red
    }
}

# ── Aggregate statistics ────────────────────────────────────────────────────
$totalTests   = $allTestResults.Count
$passedTests  = ($allTestResults | Where-Object { $_.Status -eq "PASS" }).Count
$warningTests = ($allTestResults | Where-Object { $_.Status -eq "WARNING" }).Count
$failedTests  = ($allTestResults | Where-Object { $_.Status -in @("FAIL","ERROR") }).Count
$overallScore = if ($totalTests -gt 0) { [math]::Round($passedTests / $totalTests * 100, 2) } else { 0 }

$allVulnerabilities = @()
foreach ($r in $allTestResults) { $allVulnerabilities += $r.Vulnerabilities }

# ── HTML Report ─────────────────────────────────────────────────────────────
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "Generating Security Report"               -ForegroundColor Cyan
Write-Host "========================================"  -ForegroundColor Cyan

$scoreOffset = [math]::Round(440 - (440 * $overallScore / 100))

$htmlReport = @"
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>OWASP Security Test Report - E-Commerce Lab</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh; padding: 20px;
        }
        .container {
            max-width: 1400px; margin: 0 auto; background: white;
            border-radius: 15px; box-shadow: 0 20px 60px rgba(0,0,0,.3); overflow: hidden;
        }
        .header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white; padding: 30px; text-align: center;
        }
        .header h1 { font-size: 2.5em; margin-bottom: 10px; }
        .header p  { opacity: .9; }
        .content { padding: 30px; }

        .summary-cards {
            display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 20px; margin-bottom: 30px;
        }
        .card {
            background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
            padding: 20px; border-radius: 10px; text-align: center; transition: transform .3s;
        }
        .card:hover { transform: translateY(-5px); }
        .card h3 { color: #333; margin-bottom: 10px; font-size: 1.1em; }
        .card .number { font-size: 2.5em; font-weight: bold; margin: 10px 0; }
        .card.pass    .number { color: #4caf50; }
        .card.warning .number { color: #ff9800; }
        .card.fail    .number { color: #f44336; }
        .card.total   .number { color: #667eea; }

        .score-section {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            padding: 30px; border-radius: 10px; margin-bottom: 30px;
            text-align: center; color: white;
        }
        .score-circle { width: 150px; height: 150px; margin: 20px auto; position: relative; }
        .score-circle svg { width: 150px; height: 150px; transform: rotate(-90deg); }
        .score-circle circle { fill: none; stroke-width: 10; }
        .score-circle .bg       { stroke: rgba(255,255,255,.2); }
        .score-circle .progress { stroke: #4caf50; stroke-dasharray: 440; stroke-dashoffset: 440; }
        .score-text {
            position: absolute; top: 50%; left: 50%;
            transform: translate(-50%,-50%); font-size: 2em; font-weight: bold;
        }

        .test-results { margin-bottom: 30px; overflow-x: auto; }
        table { width: 100%; border-collapse: collapse; background: white; box-shadow: 0 2px 10px rgba(0,0,0,.1); }
        th, td { padding: 15px; text-align: left; border-bottom: 1px solid #ddd; }
        th { background: #667eea; color: white; font-weight: 600; }
        tr:hover { background: #f5f5f5; }

        .status-badge { display: inline-block; padding: 5px 12px; border-radius: 20px; font-size: .85em; font-weight: bold; }
        .status-pass    { background: #4caf50; color: white; }
        .status-warning { background: #ff9800; color: white; }
        .status-fail    { background: #f44336; color: white; }
        .status-error   { background: #9e9e9e; color: white; }

        .vulnerabilities {
            background: #fff3e0; border-left: 4px solid #ff9800;
            padding: 20px; border-radius: 8px; margin-bottom: 30px;
        }
        .vulnerabilities h3 { color: #e65100; margin-bottom: 15px; }
        .vulnerability-list { list-style: none; }
        .vulnerability-list li {
            padding: 8px 0; border-bottom: 1px solid #ffe0b2;
            font-family: monospace; font-size: .9em;
        }
        .vulnerability-list li::before { content: "⚠️ "; margin-right: 10px; }

        .details { margin-bottom: 30px; }
        .details h3 { color: #3949ab; margin-bottom: 15px; }

        .detail-card { background: #fff; border: 1px solid #e0e0e0; border-radius: 10px; margin-bottom: 14px; overflow: hidden; }
        .detail-card-header {
            display: flex; align-items: center; justify-content: space-between;
            padding: 12px 16px; border-bottom: 1px solid #e0e0e0;
        }
        .detail-card-header .dc-title { font-size: 14px; font-weight: 600; color: #1a1a2e; }
        .detail-card-header .dc-meta  { display: flex; align-items: center; gap: 10px; }
        .detail-card-header .dc-score { font-size: 12px; color: #888; }
        .detail-card-body { padding: 12px 16px; display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 8px; }

        .check-item { display: flex; align-items: center; gap: 8px; padding: 7px 10px; border-radius: 6px; font-size: 13px; }
        .check-item.ci-pass { background: #e8f5e9; color: #2e7d32; }
        .check-item.ci-fail { background: #ffebee; color: #c62828; }
        .check-item.ci-warn { background: #fff8e1; color: #e65100; }
        .check-item.ci-info { background: #e3f2fd; color: #1565c0; }
        .check-icon { flex-shrink: 0; width: 16px; height: 16px; }

        .progress-bar  { width: 100%; background: #e0e0e0; border-radius: 10px; overflow: hidden; margin-top: 5px; }
        .progress-fill { height: 20px; border-radius: 10px; background: #4caf50; }
        .progress-fill.warning { background: #ff9800; }
        .progress-fill.fail    { background: #f44336; }

        .footer { background: #f5f5f5; padding: 20px; text-align: center; color: #666; font-size: .85em; }
    </style>
</head>
<body>
<div class="container">
    <div class="header">
        <h1>🔒 OWASP Top 10 Security Test Report</h1>
        <p>E-Commerce Lab - Automated Security Testing Suite</p>
        <p><strong>Target:</strong> $BaseUrl</p>
        <p><strong>Test Date:</strong> $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')</p>
    </div>

    <div class="content">
        <div class="summary-cards">
            <div class="card total">  <h3>Total Tests</h3>  <div class="number">$totalTests</div>  </div>
            <div class="card pass">   <h3>✅ Passed</h3>    <div class="number">$passedTests</div>  </div>
            <div class="card warning"><h3>⚠️ Warnings</h3>  <div class="number">$warningTests</div> </div>
            <div class="card fail">   <h3>❌ Failed</h3>    <div class="number">$failedTests</div>  </div>
        </div>

        <div class="score-section">
            <h2>Overall Security Score</h2>
            <div class="score-circle">
                <svg>
                    <circle class="bg"       cx="75" cy="75" r="70"></circle>
                    <circle class="progress" cx="75" cy="75" r="70"
                        style="stroke-dashoffset: $scoreOffset"></circle>
                </svg>
                <div class="score-text">$overallScore%</div>
            </div>
            <p>Based on $totalTests security test categories</p>
        </div>

        <div class="test-results">
            <h2>📊 Detailed Test Results</h2>
            <table>
                <thead>
                    <tr><th>Test Category</th><th>Status</th><th>Score</th><th>Progress</th></tr>
                </thead>
                <tbody>
"@

foreach ($result in $allTestResults) {
    $sBadge  = ($result.Status).ToLower() -replace "warning","warning" -replace "error","error"
    $sBadge  = if ($sBadge -notin @("pass","warning","fail","error")) { "error" } else { $sBadge }
    $scoreT  = if ($result.MaxScore -gt 0) { "$($result.Score)/$($result.MaxScore) ($($result.ScorePercent)%)" } else { "N/A" }
    $pClass  = if ($result.ScorePercent -ge 80) { "" } elseif ($result.ScorePercent -ge 50) { "warning" } else { "fail" }

    $htmlReport += @"
                    <tr>
                        <td><strong>$($result.TestName)</strong></td>
                        <td><span class="status-badge status-$sBadge">$($result.Status)</span></td>
                        <td>$scoreT</td>
                        <td><div class="progress-bar"><div class="progress-fill $pClass" style="width:$($result.ScorePercent)%"></div></div></td>
                    </tr>
"@
}

$htmlReport += @"
                </tbody>
            </table>
        </div>

        <div class="vulnerabilities">
            <h3>🚨 Vulnerabilities &amp; Issues Found ($($allVulnerabilities.Count))</h3>
            <ul class="vulnerability-list">
"@

if ($allVulnerabilities.Count -gt 0) {
    foreach ($v in ($allVulnerabilities | Select-Object -First 50)) {
        $ev = $v -replace '&','&amp;' -replace '<','&lt;' -replace '>','&gt;' -replace '"','&quot;'
        $htmlReport += "                <li>$ev</li>`n"
    }
} else {
    $htmlReport += "                <li>✅ No vulnerabilities detected!</li>`n"
}

$htmlReport += @"
            </ul>
        </div>

        <div class="details">
            <h3>📋 Detailed Test Results by Category</h3>
"@

# SVG icon snippets reused across check items
$iconPass = '<svg class="check-icon" viewBox="0 0 16 16" fill="none"><circle cx="8" cy="8" r="8" fill="#2e7d32"/><path d="M4.5 8l2.5 2.5 5-5" stroke="#e8f5e9" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>'
$iconFail = '<svg class="check-icon" viewBox="0 0 16 16" fill="none"><circle cx="8" cy="8" r="8" fill="#c62828"/><path d="M5 5l6 6M11 5l-6 6" stroke="#ffebee" stroke-width="1.5" stroke-linecap="round"/></svg>'
$iconWarn = '<svg class="check-icon" viewBox="0 0 16 16" fill="none"><circle cx="8" cy="8" r="8" fill="#e65100"/><path d="M8 5v4M8 11v.5" stroke="#fff8e1" stroke-width="1.5" stroke-linecap="round"/></svg>'
$iconInfo = '<svg class="check-icon" viewBox="0 0 16 16" fill="none"><circle cx="8" cy="8" r="8" fill="#1565c0"/><path d="M8 7v5M8 5v.5" stroke="#e3f2fd" stroke-width="1.5" stroke-linecap="round"/></svg>'

foreach ($result in $allTestResults) {
    $sBadge  = ($result.Status).ToLower()
    $sBadge  = if ($sBadge -notin @("pass","warning","fail","error")) { "error" } else { $sBadge }
    $scoreLabel = if ($result.MaxScore -gt 0) { "$($result.Score) / $($result.MaxScore) passed" } else { "N/A" }

    $htmlReport += @"
            <div class="detail-card">
                <div class="detail-card-header">
                    <span class="dc-title">$($result.TestName)</span>
                    <div class="dc-meta">
                        <span class="dc-score">$scoreLabel</span>
                        <span class="status-badge status-$sBadge">$($result.Status)</span>
                    </div>
                </div>
                <div class="detail-card-body">
"@

    foreach ($key in $result.Details.Keys) {
        $val = $result.Details[$key]
        $ek  = ([string]$key) -replace '&','&amp;' -replace '<','&lt;' -replace '>','&gt;'
        $ev  = ([string]$val) -replace '&','&amp;' -replace '<','&lt;' -replace '>','&gt;'

        # Pick pill style + icon based on the value
        switch -Wildcard ($val) {
            "PASS"    { $cls = "ci-pass"; $ico = $iconPass }
            "FAIL"    { $cls = "ci-fail"; $ico = $iconFail }
            "WARN"    { $cls = "ci-warn"; $ico = $iconWarn }
            "WARNING" { $cls = "ci-warn"; $ico = $iconWarn }
            "ERROR"   { $cls = "ci-fail"; $ico = $iconFail }
            default {
                # Numeric values (counts) shown as info pills
                $cls = "ci-info"; $ico = $iconInfo
                $ev  = "$ek : $ev"
                $ek  = ""
            }
        }

        if ($ek) {
            $htmlReport += "                <div class=`"check-item $cls`">$ico $ek</div>`n"
        } else {
            $htmlReport += "                <div class=`"check-item $cls`">$ico $ev</div>`n"
        }
    }

    $htmlReport += @"
                </div>
            </div>
"@
}

$htmlReport += @"
        </div>
    </div>

    <div class="footer">
        <p>Generated by E-Commerce Lab Security Test Suite | OWASP Top 10 Compliant</p>
        <p>Results saved to: <code>$resultsDir</code></p>
    </div>
</div>

<script>
    document.addEventListener('DOMContentLoaded', function () {
        var circle = document.querySelector('.score-circle .progress');
        if (!circle) return;
        var circumference = 2 * Math.PI * 70;
        circle.style.strokeDasharray  = circumference;
        circle.style.strokeDashoffset = circumference;
        setTimeout(function () {
            circle.style.transition    = 'stroke-dashoffset 1s ease';
            circle.style.strokeDashoffset = circumference - (circumference * $overallScore / 100);
        }, 150);
    });
</script>
</body>
</html>
"@

$reportPath = "$resultsDir\Security-Report.html"
$htmlReport | Out-File -FilePath $reportPath -Encoding UTF8

# ── Final console summary ───────────────────────────────────────────────────
Write-Host "`n========================================"     -ForegroundColor Cyan
Write-Host "Security Testing Completed!"                   -ForegroundColor Green
Write-Host "========================================"     -ForegroundColor Cyan
Write-Host ""
Write-Host "Summary:" -ForegroundColor Cyan
Write-Host "  Total Tests       : $totalTests"            -ForegroundColor White
Write-Host "  Passed            : $passedTests"           -ForegroundColor Green
Write-Host "  Warnings          : $warningTests"          -ForegroundColor Yellow
Write-Host "  Failed/Error      : $failedTests"           -ForegroundColor Red
Write-Host "  Overall Score     : $overallScore%"         -ForegroundColor Cyan
Write-Host "  Total Issues Found: $($allVulnerabilities.Count)" `
           -ForegroundColor $(if ($allVulnerabilities.Count -gt 0) { "Red" } else { "Green" })
Write-Host ""
Write-Host "HTML Report: $reportPath" -ForegroundColor Green
Write-Host ""
Write-Host "End Time: $(Get-Date)" -ForegroundColor Yellow
Write-Host "========================================"     -ForegroundColor Cyan

Start-Process $reportPath