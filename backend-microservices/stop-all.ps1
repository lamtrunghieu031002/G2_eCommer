# ============================================================
#  TAT toan bo he thong e-commerce microservices
# ============================================================
Write-Host "==== TAT HE THONG ====" -ForegroundColor Cyan

# Tat cac tien trinh java cua 6 service (theo port)
$ports = 8081, 8082, 8083, 8084, 8085, 8089
foreach ($p in $ports) {
    $conn = Get-NetTCPConnection -LocalPort $p -State Listen -ErrorAction SilentlyContinue
    if ($conn) {
        $procId = $conn.OwningProcess | Select-Object -First 1
        Stop-Process -Id $procId -Force -ErrorAction SilentlyContinue
        Write-Host "  Da tat service o port $p (PID $procId)" -ForegroundColor Green
    }
}

# Tat PostgreSQL (giu nguyen du lieu trong volume)
docker compose -f "$PSScriptRoot\docker-compose.yml" stop | Out-Null   # postgres + redis + kafka + kafka-ui
Write-Host "  Da dung PostgreSQL (du lieu van con)" -ForegroundColor Green
Write-Host "`nDa tat xong. Chay lai bang .\start-all.ps1" -ForegroundColor Cyan
