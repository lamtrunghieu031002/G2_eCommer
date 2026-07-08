# ============================================================
#  Khoi dong TOAN BO he thong e-commerce microservices
#  Cach dung: chuot phai file -> Run with PowerShell
#           hoac trong terminal:  .\start-all.ps1
# ============================================================

$ErrorActionPreference = "Stop"
$root = $PSScriptRoot
Write-Host "==== KHOI DONG HE THONG E-COMMERCE ====" -ForegroundColor Cyan

# --- 1. Ha tang: PostgreSQL trong Docker ---
Write-Host "`n[1/4] Khoi dong PostgreSQL (Docker)..." -ForegroundColor Yellow
docker compose -f "$root\docker-compose.yml" up -d | Out-Null   # postgres + redis + kafka + kafka-ui

Write-Host "      Cho database san sang..." -NoNewline
for ($i = 0; $i -lt 30; $i++) {
    $status = docker inspect --format "{{.State.Health.Status}}" ecom-postgres 2>$null
    if ($status -eq "healthy") { break }
    Start-Sleep -Seconds 2
    Write-Host "." -NoNewline
}
Write-Host " OK" -ForegroundColor Green

# --- 2. Seed bang roles (chi chay lan dau, da co thi bo qua) ---
Write-Host "[2/4] Kiem tra du lieu roles..." -ForegroundColor Yellow
docker exec ecom-postgres psql -U postgres -d user_db -c "INSERT INTO roles (id, name) VALUES (1,'ADMIN'),(2,'USER') ON CONFLICT DO NOTHING;" 2>$null | Out-Null

# --- 3. Khoi dong 6 service, moi service 1 cua so rieng ---
Write-Host "[3/4] Khoi dong 6 microservice (moi service 1 cua so)..." -ForegroundColor Yellow
$services = @(
    @{ name = "user-service";    jar = "user-service-0.0.1-SNAPSHOT.jar";    port = 8081 },
    @{ name = "product-service"; jar = "product-service-0.0.1-SNAPSHOT.jar"; port = 8082 },
    @{ name = "order-service";   jar = "order-service-0.0.1-SNAPSHOT.jar";   port = 8083 },
    @{ name = "payment-service"; jar = "payment-service-0.0.1-SNAPSHOT.jar"; port = 8084 },
    @{ name = "chatbot-service"; jar = "chatbot-service-0.0.1-SNAPSHOT.jar"; port = 8085 },
    @{ name = "api-gateway";     jar = "api-gateway-0.0.1-SNAPSHOT.jar";     port = 8089 }
)
foreach ($s in $services) {
    $dir = Join-Path $root $s.name
    $jarPath = Join-Path $dir "target\$($s.jar)"
    if (-not (Test-Path $jarPath)) {
        Write-Host "      THIEU $jarPath - hay chay: mvn clean package -DskipTests" -ForegroundColor Red
        continue
    }
    Start-Process powershell -ArgumentList @(
        "-NoExit", "-Command",
        "cd '$dir'; Write-Host 'ĐANG CHAY $($s.name) (port $($s.port))' -ForegroundColor Green; java -jar 'target\$($s.jar)'"
    )
    Write-Host "      -> $($s.name) (port $($s.port))"
    Start-Sleep -Seconds 6   # gian cach de user-service len truoc
}

# --- 4. Xong ---
Write-Host "`n[4/4] XONG! Cho khoang 30-40 giay cho cac service len het." -ForegroundColor Green
Write-Host "`n  Backend (qua gateway): http://localhost:8089/api/v1" -ForegroundColor Cyan
Write-Host "  Kiem tra:              http://localhost:8089/api/v1/healthcheck/health" -ForegroundColor Cyan
Write-Host "`n  De chay giao dien: mo terminal moi va go:" -ForegroundColor Cyan
Write-Host "     cd ..\frontend ; npm start   (roi mo http://localhost:4200)" -ForegroundColor White
Write-Host "`n  De TAT tat ca: chay .\stop-all.ps1" -ForegroundColor Cyan
