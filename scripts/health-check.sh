#!/usr/bin/env bash
# ==============================================================================
# Astro-AI Multi-Service Health Check Script (health-check.sh)
# Probes each service layer across the deployment stack.
# ==============================================================================

set -euo pipefail

GATEWAY_URL="${1:-http://localhost}"
BACKEND_URL="${2:-http://localhost:8080}"
ASTRO_URL="${3:-http://localhost:8000}"

echo "=========================================================="
echo " Running Astro-AI Full-Stack Health Audit"
echo " Gateway: ${GATEWAY_URL}"
echo " Backend: ${BACKEND_URL}"
echo " Astrology Engine: ${ASTRO_URL}"
echo "=========================================================="

FAILED=0

# 1. Astrology Calculation Engine
echo -n "[1/3] Testing Python Astrology Engine (/health)... "
if curl -fs -m 5 "${ASTRO_URL}/health" > /dev/null 2>&1; then
    echo "OK"
else
    echo "FAILED (cannot reach ${ASTRO_URL}/health)"
    FAILED=$((FAILED + 1))
fi

# 2. Spring Boot Core Backend
echo -n "[2/3] Testing Spring Boot Backend (/actuator/health)... "
BACKEND_RESP=$(curl -fs -m 5 "${BACKEND_URL}/actuator/health" 2>/dev/null || echo "DOWN")
if echo "${BACKEND_RESP}" | grep -q '"status":"UP"'; then
    echo "OK (Status: UP)"
else
    # Try through gateway
    GATEWAY_BACKEND=$(curl -fs -m 5 "${GATEWAY_URL}/actuator/health" 2>/dev/null || echo "DOWN")
    if echo "${GATEWAY_BACKEND}" | grep -q '"status":"UP"'; then
        echo "OK via Gateway (Status: UP)"
    else
        echo "FAILED (${BACKEND_RESP})"
        FAILED=$((FAILED + 1))
    fi
fi

# 3. Frontend Web Gateway
echo -n "[3/3] Testing Frontend Web Gateway (/)... "
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -m 5 "${GATEWAY_URL}/" || echo "000")
if [ "${HTTP_CODE}" = "200" ]; then
    echo "OK (HTTP 200)"
else
    echo "FAILED (HTTP ${HTTP_CODE})"
    FAILED=$((FAILED + 1))
fi

echo "=========================================================="
if [ $FAILED -eq 0 ]; then
    echo " All Astro-AI Services are Healthy and Operational! "
    echo "=========================================================="
    exit 0
else
    echo " Detected ${FAILED} failing service(s). Please inspect logs."
    echo "=========================================================="
    exit 1
fi
