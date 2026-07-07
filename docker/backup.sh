#!/bin/bash
# ================================================
# Backup script cho SQL Server
# ================================================
# Cach dung:
#   ./backup.sh              # backup ngay lap tuc
#   ./backup.sh auto        # chay tu dong, tu xoa backup cu (giu 7 ban gan nhat)
# ================================================

set -e

# Cau hinh
BACKUP_DIR="./backups"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_PATH="${BACKUP_DIR}/ats_backup_${TIMESTAMP}"

# Doc bien tu .env
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/../.env" 2>/dev/null || true
DB_PASS="${DB_PASS:-123456}"
MSSQL_DB_NAME="${MSSQL_DB_NAME:-ATS}"

# Tao thu muc backup
mkdir -p "${BACKUP_PATH}"

echo "========================================"
echo "  ATS Backup - ${TIMESTAMP}"
echo "========================================"

echo ""
echo "[1/1] Dang backup SQL Server..."

docker exec ats_sqlserver mkdir -p /var/opt/mssql/backup

docker exec ats_sqlserver /opt/mssql-tools/bin/sqlcmd \
  -S localhost -U sa -P "${DB_PASS}" \
  -Q "BACKUP DATABASE [${MSSQL_DB_NAME}] TO DISK = '/var/opt/mssql/backup/ats_backup_${TIMESTAMP}.bak' WITH COMPRESSION, CHECKSUM, INIT"

docker cp ats_sqlserver:/var/opt/mssql/backup/ats_backup_${TIMESTAMP}.bak "${BACKUP_PATH}/"

echo "  [OK] Backup xong: ${BACKUP_PATH}/ats_backup_${TIMESTAMP}.bak"

# Tao file manifest
cat > "${BACKUP_PATH}/manifest.txt" << EOF
ATS Backup Manifest
===================
Timestamp: ${TIMESTAMP}
Database: ${MSSQL_DB_NAME}

Restore Command:
  ./restore.sh ats_backup_${TIMESTAMP}
EOF

# Xoa backup cu (neu chay mode auto)
if [ "$1" == "auto" ]; then
  BACKUP_COUNT=$(ls -1d "${BACKUP_DIR}"/ats_backup_* 2>/dev/null | wc -l)
  if [ "${BACKUP_COUNT}" -gt 7 ]; then
    echo ""
    echo "[Cleanup] Xoa backup cu (giu 7 ban gan nhat)..."
    ls -1td "${BACKUP_DIR}"/ats_backup_* | tail -n +8 | xargs rm -rf
    echo "  [OK] Da xoa backup cu"
  fi
fi

echo ""
echo "========================================"
echo "  Backup hoan tat!"
echo "  Duong dan: ${BACKUP_PATH}"
echo "========================================"
