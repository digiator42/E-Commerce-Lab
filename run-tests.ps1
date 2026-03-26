param (
    [Parameter(Mandatory=$false)]
    [ValidateSet("postgres", "mysql-primary")]
    [string]$Database = "mysql-primary",

    [Parameter(Mandatory=$false)]
    [string]$TestParam = ""
)

# Set environment variables for Spring Boot datasource
.\run-env.ps1

# Combine with 'dev' for DataSeeder and logic defaults
$ActiveProfiles = "test,$Database"

Write-Host "----------------------------------------------------" -ForegroundColor Cyan
Write-Host "📂 Active Profiles: $ActiveProfiles" -ForegroundColor Yellow
Write-Host "----------------------------------------------------"

# Build Maven command dynamically
$mvnCmd = "./mvnw test `"-Dspring.profiles.active=$ActiveProfiles`""

if ($TestParam -ne "") {
    $mvnCmd += " `"-Dtest=$TestParam`""
}

Write-Host "▶ Running: $mvnCmd" -ForegroundColor Cyan
Invoke-Expression $mvnCmd

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Tests Passed: Exit Criteria Met for $Database" -ForegroundColor Green 
} else {
    exit $LASTEXITCODE
}
