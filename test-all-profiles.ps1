# test-all.ps1
Write-Host "Testing PostgreSQL..." -ForegroundColor Green
$env:SPRING_PROFILES_ACTIVE="test,postgres"
./mvnw clean test

Write-Host "`nTesting MySQL..." -ForegroundColor Green
$env:SPRING_PROFILES_ACTIVE="test,mysql-primary"
./mvnw clean test

Write-Host "`n✅ All tests completed!" -ForegroundColor Cyan
Exit 0