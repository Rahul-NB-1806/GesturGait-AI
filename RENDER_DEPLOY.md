# GesturGait AI - Render Deployment Guide

I have prepared the backend for professional deployment on **Render**. Follow these steps to put your Parkinson's monitoring system in the cloud.

## **Step 1: Push Code to GitHub**
1. Create a new repository on [GitHub](https://github.com).
2. Push your project:
   ```bash
   git add .
   git commit -m "Prepare for Render deployment"
   git push origin main
   ```

## **Step 2: Create a Web Service on Render**
1. Sign in to [Render.com](https://render.com).
2. Click **New +** and select **Web Service**.
3. Connect your GitHub repository.
4. Set the following:
   - **Name**: `gesturgait-backend`
   - **Root Directory**: `backend`
   - **Runtime**: `Node`
   - **Build Command**: `npm install`
   - **Start Command**: `npm start`

## **Step 3: Add Environment Variables**
In the Render dashboard, go to the **Environment** tab and add:
- `MONGODB_URI`: *Paste your full mongodb+srv connection string here*
- `JWT_SECRET`: *Pick a random strong string*
- `PORT`: `5000` (Render will override this, but good to have)

## **Step 4: Update the Mobile App**
Once Render finishes deploying, it will give you a URL like `https://gesturgait-backend.onrender.com`.

1. Open `app/src/main/java/com/example/gesturgaitai/network/NetworkConfig.kt`
2. Set `USE_TUNNEL = true`.
3. Set `PUBLIC_URL = "https://gesturgait-backend.onrender.com"`.
4. Rebuild the Android app.

---
**Why is this better?**
* **No Tunneling**: You don't need to run `npm run tunnel` anymore.
* **Always On**: Your phone will connect to the cloud 24/7.
* **Secure**: Render provides automatic HTTPS for all connections.
