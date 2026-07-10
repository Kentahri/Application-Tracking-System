# ================================================
# Init script cho Qdrant collections tren PowerShell
# ================================================
# Cach dung:
#   .\init-qdrant.ps1
# ================================================

$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$envPath = Join-Path $scriptDir "..\.env"

if (Test-Path $envPath) {
    Get-Content $envPath | ForEach-Object {
        $line = $_.Trim()
        if ($line -and -not $line.StartsWith("#") -and $line.Contains("=")) {
            $parts = $line.Split("=", 2)
            $name = $parts[0].Trim()
            $value = $parts[1].Trim()
            if ($name -and -not [Environment]::GetEnvironmentVariable($name)) {
                [Environment]::SetEnvironmentVariable($name, $value, "Process")
            }
        }
    }
}

$qdrantUrl = if ($env:QDRANT_URL) { $env:QDRANT_URL } else { "http://localhost:6333" }
$collection = if ($env:QDRANT_COLLECTION) { $env:QDRANT_COLLECTION } else { "jobs" }
$vectorSize = if ($env:QDRANT_VECTOR_SIZE) { [int]$env:QDRANT_VECTOR_SIZE } else { 1536 }
$distance = if ($env:QDRANT_DISTANCE) { $env:QDRANT_DISTANCE } else { "Cosine" }

Write-Host "========================================"
Write-Host "  ATS Qdrant Init"
Write-Host "========================================"
Write-Host "Qdrant URL:        $qdrantUrl"
Write-Host "Collection:        $collection"
Write-Host "Vector size:       $vectorSize"
Write-Host "Distance:          $distance"
Write-Host ""

Write-Host "[1/2] Kiem tra Qdrant..."
try {
    Invoke-RestMethod -Uri "$qdrantUrl/" -Method Get | Out-Null
    Write-Host "  [OK] Qdrant dang chay"
}
catch {
    Write-Host "  [ERROR] Khong ket noi duoc Qdrant tai $qdrantUrl"
    Write-Host "  Hay chay truoc: docker compose -f docker/docker-compose.yml up -d qdrant"
    exit 1
}

Write-Host ""
Write-Host "[2/2] Tao collection neu chua ton tai..."
$collectionExists = $false

try {
    Invoke-RestMethod -Uri "$qdrantUrl/collections/$collection" -Method Get | Out-Null
    $collectionExists = $true
}
catch {
    $collectionExists = $false
}

if ($collectionExists) {
    Write-Host "  [OK] Collection '$collection' da ton tai, bo qua"
}
else {
    $body = @{
        vectors = @{
            size = $vectorSize
            distance = $distance
        }
    } | ConvertTo-Json -Depth 4

    Invoke-RestMethod `
        -Uri "$qdrantUrl/collections/$collection" `
        -Method Put `
        -ContentType "application/json" `
        -Body $body | Out-Null

    Write-Host "  [OK] Da tao collection '$collection'"
}

Write-Host ""
Write-Host "========================================"
Write-Host "  Qdrant init hoan tat!"
Write-Host "========================================"
