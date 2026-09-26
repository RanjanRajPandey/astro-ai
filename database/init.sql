-- Astro-AI PostgreSQL Initialization Script
-- Ensures UUID support is available prior to Flyway schema migrations.
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
