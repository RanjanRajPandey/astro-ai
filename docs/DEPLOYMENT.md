# Astro-AI: Production Deployment & Operations Guide

This guide details the complete production deployment architecture, security hardening, automated pipelines, database backup strategies, and operational runbooks for the **Astro-AI** enterprise Vedic Astrology & Kundli platform.

---

## 1. System Topology & Architecture

Astro-AI utilizes a multi-tiered, microservice architecture isolated into distinct network zones:

```text
               INTERNET / USERS
                      │
                      ▼ HTTPS (443) / HTTP (80)
         ┌─────────────────────────┐
         │      Nginx Gateway      │  (Public Network: astro-frontend-net)
         │  (Reverse Proxy + SPA)  │  - SSL/TLS Termination
         └────────────┬────────────┘  - Gzip / Security Headers / Caching
                      │
           ┌──────────┴──────────┐
           │                     │  /api/ (Reverse Proxy + SSE Streams)
           ▼                     ▼
┌──────────────────┐    ┌──────────────────────────────────────────────┐
│  React Frontend  │    │     Spring Boot Core Backend (Java 21)       │
│  (Static Build)  │    │ - Spring Security (JWT) & Rate Limiting      │
└──────────────────┘    │ - Flyway Schema Migrations                   │
                        │ - Evidence, Temporal & Reasoning Engines     │
                        └──────────────┬───────────────────────────────┘
                                       │ (Private Network: astro-backend-net)
                       ┌───────────────┴───────────────┐
                       │                               │
                       ▼                               ▼
        ┌─────────────────────────────┐   ┌───────────────────────────┐
        │ Python Astrology Engine     │   │   PostgreSQL 16 Database  │
        │ (FastAPI / Swiss Ephemeris) │   │   - Persistent NVMe Volume│
        │ - 4 Multi-worker Uvicorn    │   │   - pgcrypto & UUIDs      │
        │ - LRU Ephemeris Cache       │   │   - Hikari Connection Pool│
        └─────────────────────────────┘   └───────────────────────────┘
```

### Security & Network Isolation
1. **No Direct Ingress to Data**: The PostgreSQL database and Python Calculation Engine have **zero public port exposure**. They reside on the isolated internal Docker bridge network `astro-backend-net`.
2. **Non-Root Execution**:
   - Backend runs as non-root user `astro` (UID 10001).
   - Astrology Engine runs as non-root user `astrouser` (UID 10001).
   - Nginx runs unprivileged worker processes under user `nginx`.
3. **Graceful Shutdown**: Spring Boot is configured with `server.shutdown: graceful` and a 20-second drain window to ensure active reasoning calculations and SSE streams finish cleanly before pod termination.

---

## 2. Infrastructure Sizing Guidelines

| Component | Minimum Sizing | Recommended Enterprise Sizing |
| :--- | :--- | :--- |
| **CPU** | 2 vCPUs | 4 – 8 vCPUs |
| **RAM** | 4 GB | 8 – 16 GB |
| **Disk** | 25 GB SSD | 80 GB+ NVMe SSD |
| **OS** | Ubuntu 22.04/24.04 LTS / Debian 12 | Ubuntu 24.04 LTS / RHEL 9 |
| **Container Engine** | Docker Engine 26.0+ | Docker Engine 26.0+ with Docker Compose v2.26+ |

---

## 3. Production Deployment Step-by-Step

### Step 1: Clone Repository
```bash
git clone https://github.com/Rraj222004/astro-ai.git /opt/astro-ai
cd /opt/astro-ai
```

### Step 2: Configure Production Environment Secrets
Copy the production environment template:
```bash
cp .env.production.example .env.production
chmod 600 .env.production
```

Generate cryptographically secure secrets:
```bash
# Generate 256-bit base64 secret for JWT Signing:
openssl rand -base64 48

# Generate secure password for PostgreSQL:
openssl rand -base64 24
```

Edit `.env.production` and populate:
- `POSTGRES_PASSWORD`: The generated Postgres password.
- `JWT_SECRET`: The generated 256-bit base64 secret.
- `CORS_ALLOWED_ORIGINS`: Your production domains (e.g. `https://kundli.yourdomain.com`).
- `AI_PROVIDER`: `MOCK`, `GEMINI`, `OPENAI`, or `ANTHROPIC` along with the corresponding API key.

### Step 3: Run Automated Deployment Pipeline
Execute the automated zero-downtime deployment script:
```bash
chmod +x scripts/*.sh
./scripts/deploy-prod.sh
```
*(On Windows Server environments, run `.\scripts\deploy-prod.ps1`)*

The script automatically:
1. Validates that default placeholder secrets have been replaced.
2. Builds all multi-stage containers with dependency pre-caching.
3. Spawns isolated internal networks and persistent storage volumes.
4. Waits for PostgreSQL readiness and Flyway database migrations.
5. Verifies Spring Boot `/actuator/health` and FastAPI `/health` endpoints.

---

## 4. SSL / TLS Setup (Let's Encrypt / Certbot)

For standalone servers terminating TLS directly:

### 1. Install Certbot
```bash
sudo apt-get update
sudo apt-get install -y certbot python3-certbot-nginx
```

### 2. Obtain Certificate
```bash
sudo certbot certonly --standalone -d kundli.yourdomain.com
```

### 3. Nginx HTTPS Configuration
Mount certificates into `docker-compose.prod.yml` under `frontend`:
```yaml
    volumes:
      - /etc/letsencrypt:/etc/letsencrypt:ro
    ports:
      - "80:80"
      - "443:443"
```
And add HTTPS listener in `frontend/nginx/default.conf`:
```nginx
server {
    listen 80;
    server_name kundli.yourdomain.com;
    return 301 https://$host$request_uri;
}

server {
    listen 443 ssl http2;
    server_name kundli.yourdomain.com;

    ssl_certificate /etc/letsencrypt/live/kundli.yourdomain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/kundli.yourdomain.com/privkey.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;

    # ... remaining location blocks ...
}
```

---

## 5. Database Backups & Disaster Recovery

### Automated Backup Script
Run the automated backup script:
```bash
./scripts/backup-db.sh
```
*(On Windows PowerShell: `.\scripts\backup-db.ps1`)*

Backups are saved to `database/backups/astro_ai_backup_YYYYMMDD_HHMMSS.sql.gz` with automatic 30-day retention cleanup.

### Scheduled Nightly Backup (Cron)
Add a crontab entry for nightly automated execution at 02:00 UTC:
```bash
crontab -e
```
```cron
0 2 * * * /opt/astro-ai/scripts/backup-db.sh >> /var/log/astro_backup.log 2>&1
```

### Database Restoration Procedure
To restore from a backup:
```bash
gunzip -c /opt/astro-ai/database/backups/astro_ai_backup_20261002_120000.sql.gz | \
  docker exec -i astro-ai-prod-postgres psql -U astro_admin -d astro_ai_prod
```

---

## 6. Zero-Downtime Rolling Upgrades

To update to a new version without service disruption:

```bash
cd /opt/astro-ai
git pull origin main

# Build updated images without stopping existing containers
docker compose --env-file .env.production -f docker-compose.prod.yml build --pull

# Recreate containers with rolling updates
docker compose --env-file .env.production -f docker-compose.prod.yml up -d --no-deps --remove-orphans

# Verify stack health
./scripts/health-check.sh
```

---

## 7. Observability & Operational Health Checks

Probe all platform services at any time using:
```bash
./scripts/health-check.sh http://localhost:80 http://localhost:8080 http://localhost:8000
```

### Direct Health Check Endpoints:
- **Frontend SPA**: `GET http://<host>/` (HTTP 200)
- **Spring Boot Actuator**: `GET http://<host>/actuator/health` (`{"status":"UP"}`)
- **Swiss Ephemeris Python Engine**: `GET http://<astro-engine>:8000/health` (`{"status":"ok"}`)
- **Prometheus Metrics**: `GET http://<host>/actuator/metrics`

---

## 8. Incident Troubleshooting Runbook

| Symptom | Diagnostic Command | Typical Root Cause & Resolution |
| :--- | :--- | :--- |
| **Backend fails on startup** | `docker compose -f docker-compose.prod.yml logs backend` | Database not ready or Flyway migration lock. Check `postgres` logs. Verify `POSTGRES_PASSWORD` matches between backend and db. |
| **Astrology calculations return 500** | `docker compose -f docker-compose.prod.yml logs astrology-engine` | Swiss Ephemeris C extension memory fault or invalid coordinates. Check `/health`. |
| **Nginx returns 502 Bad Gateway** | `docker compose -f docker-compose.prod.yml logs frontend` | Backend container is booting or unhealthy. Ensure `upstream backend_upstream` port is 8080. |
| **SSE Streaming disconnects** | Check browser console for network timeouts | Proxy buffering enabled. Verify `proxy_buffering off;` and `proxy_read_timeout 3600s;` in Nginx config. |
