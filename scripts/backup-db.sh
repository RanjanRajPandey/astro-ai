#!/usr/bin/env bash
# ==============================================================================
# Astro-AI PostgreSQL Automated Backup Script (backup-db.sh)
# Dumps PostgreSQL database from docker container with gzip compression.
# ==============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
BACKUP_DIR="${ROOT_DIR}/database/backups"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_FILE="${BACKUP_DIR}/astro_ai_backup_${TIMESTAMP}.sql.gz"

mkdir -p "${BACKUP_DIR}"

CONTAINER_NAME="${POSTGRES_CONTAINER:-astro-ai-prod-postgres}"
DB_USER="${POSTGRES_USER:-astro_admin}"
DB_NAME="${POSTGRES_DB:-astro_ai_prod}"

echo "[INFO] Starting database backup for '${DB_NAME}' from container '${CONTAINER_NAME}'..."

if ! docker ps --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
    # Fallback to dev container if running
    if docker ps --format '{{.Names}}' | grep -q "^astro-ai-postgres$"; then
        CONTAINER_NAME="astro-ai-postgres"
        DB_USER="astro_user"
        DB_NAME="astro_ai"
        echo "[INFO] Falling back to dev container: ${CONTAINER_NAME}"
    else
        echo "[ERROR] Postgres container '${CONTAINER_NAME}' is not running."
        exit 1
    fi
fi

docker exec -t "${CONTAINER_NAME}" pg_dump -U "${DB_USER}" "${DB_NAME}" | gzip > "${BACKUP_FILE}"

FILE_SIZE=$(du -h "${BACKUP_FILE}" | cut -f1)
echo "[SUCCESS] Backup completed successfully: ${BACKUP_FILE} (${FILE_SIZE})"

# Retention: Remove backups older than 30 days
find "${BACKUP_DIR}" -type f -name "astro_ai_backup_*.sql.gz" -mtime +30 -delete
echo "[INFO] Cleaned up backups older than 30 days."
