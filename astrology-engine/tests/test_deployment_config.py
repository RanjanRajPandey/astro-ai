import os
import re
import pytest

ROOT_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))

def test_dockerfiles_integrity():
    """Verify all microservice Dockerfiles exist and contain hardened production configurations."""
    backend_dockerfile = os.path.join(ROOT_DIR, "docker", "Dockerfile.backend")
    engine_dockerfile = os.path.join(ROOT_DIR, "docker", "Dockerfile.astrology-engine")
    frontend_dockerfile = os.path.join(ROOT_DIR, "docker", "Dockerfile.frontend")

    for path in [backend_dockerfile, engine_dockerfile, frontend_dockerfile]:
        assert os.path.isfile(path), f"Missing Dockerfile: {path}"
        with open(path, "r", encoding="utf-8") as f:
            content = f.read()
            assert "FROM " in content
            assert "EXPOSE " in content
            assert "HEALTHCHECK " in content

    # Test backend non-root user and JVM flags
    with open(backend_dockerfile, "r", encoding="utf-8") as f:
        backend_content = f.read()
        assert "USER astro" in backend_content
        assert "-XX:+UseG1GC" in backend_content
        assert "-XX:MaxRAMPercentage=75.0" in backend_content

    # Test astrology engine non-root user and multi-worker
    with open(engine_dockerfile, "r", encoding="utf-8") as f:
        engine_content = f.read()
        assert "USER astrouser" in engine_content
        assert "--workers" in engine_content

def test_production_compose_integrity():
    """Verify docker-compose.prod.yml adheres to security, isolation, and resource constraints."""
    prod_compose_path = os.path.join(ROOT_DIR, "docker-compose.prod.yml")
    assert os.path.isfile(prod_compose_path)

    with open(prod_compose_path, "r", encoding="utf-8") as f:
        content = f.read()

        # Check required services
        assert "postgres:" in content
        assert "astrology-engine:" in content
        assert "backend:" in content
        assert "frontend:" in content

        # Check network isolation: internal backend network
        assert "astro-backend-net:" in content
        assert "internal: true" in content

        # Check resource quotas
        assert "limits:" in content
        assert "reservations:" in content
        assert "cpus:" in content
        assert "memory:" in content

        # Check health checks
        assert "healthcheck:" in content
        assert "service_healthy" in content

        # Check log rotation
        assert "max-size:" in content
        assert "max-file:" in content

def test_nginx_configuration():
    """Verify Nginx reverse proxy has SPA fallback, SSE headers, and security headers."""
    nginx_conf_path = os.path.join(ROOT_DIR, "docker", "nginx", "conf.d", "default.conf")
    assert os.path.isfile(nginx_conf_path)

    with open(nginx_conf_path, "r", encoding="utf-8") as f:
        content = f.read()
        assert "try_files $uri $uri/ /index.html;" in content
        assert "proxy_pass http://backend_upstream;" in content
        assert "proxy_buffering off;" in content  # SSE support
        assert "X-Frame-Options" in content
        assert "X-Content-Type-Options" in content

def test_deployment_scripts():
    """Verify deployment, backup, and health-check scripts exist in scripts/."""
    scripts = [
        "deploy-prod.sh",
        "deploy-prod.ps1",
        "health-check.sh",
        "health-check.ps1",
        "backup-db.sh",
        "backup-db.ps1"
    ]
    for s in scripts:
        path = os.path.join(ROOT_DIR, "scripts", s)
        assert os.path.isfile(path), f"Missing script: {s}"
        assert os.path.getsize(path) > 100, f"Script is empty: {s}"

def test_production_environment_template():
    """Verify .env.production.example contains all required production parameters."""
    env_example_path = os.path.join(ROOT_DIR, ".env.production.example")
    assert os.path.isfile(env_example_path)

    with open(env_example_path, "r", encoding="utf-8") as f:
        content = f.read()
        assert "POSTGRES_PASSWORD" in content
        assert "JWT_SECRET" in content
        assert "CORS_ALLOWED_ORIGINS" in content
        assert "DB_MAX_POOL_SIZE" in content
        assert "AI_PROVIDER" in content
