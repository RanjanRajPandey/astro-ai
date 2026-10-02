# Astro-AI: Free 24/7 Cloud Deployment Guide

This guide walks you through deploying **Astro-AI** completely **FREE** (no credit card required) using:
1. **Neon.tech** (Free PostgreSQL Database)
2. **Render.com** (Free Backend & Astrology Engine Services)
3. **Vercel** (Free Fast Worldwide Frontend CDN)

---

## Architecture of Free Cloud Stack

```text
  Users (Anywhere on Web/Mobile)
                │
                ▼ HTTPS
   [ Vercel: React SPA Frontend ]
   https://astro-ai.vercel.app
                │
                ▼ HTTPS API Requests
   [ Render: Spring Boot API ] ──────────► [ Render: Swiss Ephemeris Engine ]
   https://astro-ai-backend.onrender.com    https://astro-ai-engine.onrender.com
                │
                ▼ Secure TLS
   [ Neon.tech: Free PostgreSQL ]
   ep-xxxx.neon.tech
```

---

## Step 1: Create Free Database on Neon.tech (2 Minutes)

1. Open [**https://neon.tech**](https://neon.tech) and click **Sign Up** (Sign in with your GitHub account).
2. Click **Create Project**:
   * **Project Name**: `astro-ai-db`
   * **Region**: Select the closest region to you (e.g. AWS Europe or Singapore or US).
3. Once created, on the Dashboard, look for **Connection Details**:
   * Switch the dropdown to **JDBC** or copy the **Connection string**.
   * It will look like:
     ```text
     jdbc:postgresql://ep-example-123456.us-east-2.aws.neon.tech/neondb?sslmode=require
     ```
   * Note down the **Database URL**, **User**, and **Password**.

---

## Step 2: Deploy Backend & Engine on Render.com (3 Minutes)

1. Open [**https://render.com**](https://render.com) and click **Sign In with GitHub**.
2. Click the **New +** button at the top right and select **Blueprint**.
3. Connect your GitHub repository: `Rraj222004/astro-ai`.
4. Render will automatically read the [`render.yaml`](file:///C:/Users/ranja/.gemini/antigravity/scratch/astro-ai/render.yaml) file we configured and show:
   * `astro-ai-engine` (FastAPI calculation engine)
   * `astro-ai-backend` (Spring Boot API)
5. Fill in the three database parameters when prompted:
   * `SPRING_DATASOURCE_URL`: Paste your Neon JDBC URL (e.g., `jdbc:postgresql://ep-xxx.neon.tech/neondb?sslmode=require`).
   * `SPRING_DATASOURCE_USERNAME`: Your Neon username.
   * `SPRING_DATASOURCE_PASSWORD`: Your Neon password.
6. Click **Apply**.
7. Render will build and launch both services for free!
8. Copy your backend's public URL once live (e.g., `https://astro-ai-backend.onrender.com`).

---

## Step 3: Deploy Frontend on Vercel (2 Minutes)

1. Open [**https://vercel.com**](https://vercel.com) and click **Continue with GitHub**.
2. Click **Add New...** -> **Project**.
3. Import your repository: `Rraj222004/astro-ai`.
4. Configure the project settings:
   * **Framework Preset**: `Vite`
   * **Root Directory**: Click *Edit* and select `frontend`.
5. Under **Environment Variables**, add:
   * **Key**: `VITE_API_BASE_URL`
   * **Value**: `https://astro-ai-backend.onrender.com/api` *(replace with your Render backend URL)*
6. Click **Deploy**.
7. In ~60 seconds, Vercel will give you a live production link:
   👉 `https://astro-ai-xxxx.vercel.app`!

---

## Step 4: Add Custom Domain (Optional & Free)

Both Vercel and Render allow adding any custom domain (e.g. `kundli.yourdomain.com`) for free with automatic SSL/HTTPS certificates.

---

## Automatic Updates

Whenever you or I make any changes, improve the code, or add new astrological features and push them to GitHub:
```bash
git push origin main
```
* **Vercel** automatically rebuilds and refreshes your live frontend in seconds!
* **Render** automatically rebuilds and refreshes your backend containers with zero manual effort!
