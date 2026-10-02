#!/usr/bin/env bash
# ==============================================================================
# Astro-AI Production Deployment Automation Script (deploy-prod.sh)
# Performs pre-flight checks, verifies environment secrets, builds containers,
# launches services with docker-compose.prod.yml, and runs health verification.
# ==============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

echo "=========================================================="
echo " Starting Astro-AI Production Deployment Pipeline"
echo " Working Directory: ${ROOT_DIR}"
echo " Timestamp: $(date -u +"%Y-%m-%dT%H:%M:%SZ")"
echo "=========================================================="

cd "${ROOT_DIR}"

# 1. Pre-flight Environment Validation
ENV_FILE="${ROOT_DIR}/.env.production"
if [ ! -f "${ENV_FILE}" ]; then
    if [ -f "${ROOT_DIR}/.env" ]; then
        echo "[INFO] .env.production not found, falling back to .env"
        ENV_FILE="${ROOT_DIR}/.env"
    else
        echo "[ERROR] Neither .env.production nor .env was found."
        echo "Please copy .env.production.example to .env.production and configure secrets."
        exit 1
    fi
fi

echo "[INFO] Using environment file: ${ENV_FILE}"

# Check for required tools
for tool in docker docker-compose; do
    if ! command -v "$tool" &> /dev/null; then
        if [ "$tool" = "docker-compose" ] && docker compose version &> /dev/null; then
            DOCKER_COMPOSE_CMD="docker compose"
        else
            echo "[ERROR] Required tool '$tool' is not installed or not in PATH."
            exit 1
        fi
    else
        DOCKER_COMPOSE_CMD="docker-compose"
    fi
done

# 2. Secret Integrity Checks
echo "[INFO] Validating production secrets..."
if grep -q "REPLACE_WITH" "${ENV_FILE}" || grep -q "change_me_in_production" "${ENV_FILE}"; then
    echo "[WARNING] Found default placeholder values in ${ENV_FILE}."
    echo "Make sure POSTGRES_PASSWORD and JWT_SECRET are securely generated!"
fi

# 3. Build & Orchestration
echo "[INFO] Pulling base images and building production containers..."
${DOCKER_COMPOSE_CMD} --env-file "${ENV_FILE}" -f docker-compose.prod.yml build --pull

echo "[INFO] Launching production stack in detached mode..."
${DOCKER_COMPOSE_CMD} --env-file "${ENV_FILE}" -f docker-compose.prod.yml up -d --remove-orphans

# 4. Service Health Verification
echo "[INFO] Awaiting container initialization and database migrations..."
MAX_ATTEMPTS=30
ATTEMPT=1
BACKEND_HEALTHY=false

while [ $ATTEMPT -le $MAX_ATTEMPTS ]; do
    echo "  Attempt ${ATTEMPT}/${MAX_ATTEMPTS}: Verifying stack health..."
    
    # Check docker status
    UNHEALTHY_COUNT=$(${DOCKER_COMPOSE_CMD} -f docker-compose.prod.yml ps --format json 2>/dev/null | grep -i "unhealthy" || true)
    
    if [ -z "$UNHEALTHY_COUNT" ]; then
        STATUS=$(${DOCKER_COMPOSE_CMD} -f docker-compose.prod.yml ps 2>&1)
        echo "${STATUS}"
    fi

    # Check backend actuator endpoint directly from container
    if docker exec astro-ai-prod-backend wget --spider -q http://127.0.0.1:8080/actuator/health 2>/dev/null; then
        echo "[SUCCESS] Spring Boot Backend is UP and Healthy!"
        BACKEND_HEALTHY=true
        break
    fi

    sleep 5
    ATTEMPT=$((ATTEMPT + 1))
done

if [ "$BACKEND_HEALTHY" = false ]; then
    echo "[ERROR] Deployment health check timed out. Inspecting logs:"
    ${DOCKER_COMPOSE_CMD} -f docker-compose.prod.yml logs --tail=50
    exit 1
fi

echo "=========================================================="
echo " Astro-AI Production Deployment Completed Successfully!   "
echo " Frontend Gateway: http://localhost:80                     "
echo " Backend Actuator: http://localhost:80/actuator/health    "
echo "=========================================================="
