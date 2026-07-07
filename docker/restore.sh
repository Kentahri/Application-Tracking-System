#!/bin/bash
# ================================================
# Restore script cho SQL Server
# ================================================
# Cach dung:
#   ./restore.sh ats_backup_20250615_143000
# ================================================

set -e

if [ -z "$1" ]; then
  echo "Loi: Vui long cung cap ten backup!"
  echo "Cach dung: ./restore.sh <backup_name>"
  echo ""
  echo "Cac backup co san:"
  ls -1 ./backups/ 2>/dev/null || echo "  (khong co backup nao)"
  exit 1
fi

BACKUP_NAME="$1"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKUP_DIR="${SCRIPT_DIR}/backups"
BACKUP_PATH="${BACKUP_DIR}/${BACKUP_NAME}"

if [ ! -d "${BACKUP_PATH}" ]; then
  echo "Loi: Khong tim thay backup '${BACKUP_NAME}'"
  echo "Cac backup co san:"
  ls -1 "${BACKUP_DIR}/" 2>/dev/null || echo "  (khong co backup nao)"
  exit 1
fi

# Doc bien tu .env
source "${SCRIPT_DIR}/../.env" 2>/dev/null || true
DB_PASS="${DB_PASS:-123456}"
MSSQL_DB_NAME="${MSSQL_DB_NAME:-ATS}"

echo "========================================"
echo "  ATS Restore - ${BACKUP_NAME}"
echo "========================================"
echo ""
read -p "Canh bao: Du lieu hien tai se bi ghi de! Tiep tuc? (y/n): " -n 1 -r
echo ""
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
  echo "Da huy."
  exit 1
fi

echo ""
echo "[1/1] Dang restore SQL Server..."

docker exec ats_sqlserver mkdir -p /var/opt/mssql/backup

BACKUP_FILE=$(ls "${BACKUP_PATH}"/*.bak 2>/dev/null | head -1)
if [ -n "${BACKUP_FILE}" ]; then
  docker cp "${BACKUP_FILE}" ats_sqlserver:/var/opt/mssql/backup/restore_temp.bak

  docker exec ats_sqlserver /opt/mssql-tools/bin/sqlcmd \
    -S localhost -U sa -P "${DB_PASS}" \
    -Q "RESTORE DATABASE [${MSSQL_DB_NAME}] FROM DISK = '/var/opt/mssql/backup/restore_temp.bak' WITH REPLACE, RECOVERY"

  docker exec ats_sqlserver rm -f /var/opt/mssql/backup/restore_temp.bak
  echo "  [OK] Da restore tu: $(basename ${BACKUP_FILE})"
else
  echo "  [ERROR] Khong tim thay file .bak trong thu muc backup"
  exit 1
fi

echo ""
echo "========================================"
echo "  Restore hoan tat!"
echo "========================================"
