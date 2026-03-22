param (
    [Parameter(Mandatory=$false)]
    [ValidateSet("postgres", "mysql-primary")]
    [string]$Database = "mysql-primary"
)

# Combine with 'dev' for DataSeeder and logic defaults
$ActiveProfiles = "dev,$Database"

Write-Host "----------------------------------------------------" -ForegroundColor Cyan
Write-Host "📂 Active Profiles: $ActiveProfiles" -ForegroundColor Yellow
Write-Host "----------------------------------------------------"

# Execute Maven test with the dynamic profile flag
./mvnw test "-Dspring.profiles.active=$ActiveProfiles"

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Tests Passed: Exit Criteria Met for $Database" -ForegroundColor Green
} else {
    Write-Host "❌ Tests Failed: Check logs for defects" -ForegroundColor Red
    exit $LASTEXITCODE
}