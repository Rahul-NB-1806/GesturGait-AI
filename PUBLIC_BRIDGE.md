# GesturGait AI - Public Bridge Guide

I have implemented a "Public Bridge" system that allows your phone to talk to your laptop from **anywhere in the world** (5G, external Wi-Fi, etc.), removing the "private room" restriction.

## **Step 1: Start the Public Bridge**
1. Open a new terminal on your laptop.
2. Go to the backend folder: `cd backend`
3. Run the bridge command:
   ```bash
   npm run tunnel
   ```
4. You will see a URL like: `https://gesturgait.loca.lt`
   *Note: If it asks for an IP, go to [localtunnel.me](https://localtunnel.me) and find the "Tunnel Password".*

## **Step 2: Update the Mobile App**
You only need to change one line to switch from "Private" to "Public":

### **In Kotlin (Android):**
Open `app/src/main/java/com/example/gesturgaitai/network/NetworkConfig.kt`
1. Set `USE_TUNNEL = true`.
2. Update `PUBLIC_URL` with the URL from Step 1.

### **In React Native (Mobile):**
Open `mobile/src/api/NetworkConfig.js`
1. Set `USE_TUNNEL: true`.
2. Update `PUBLIC_URL` with the URL from Step 1.

## **Step 3: Test Everywhere**
1. Rebuild and deploy the app to your phone.
2. Turn off Wi-Fi on your phone and use **5G**.
3. Log in! The app will now connect through the bridge to your laptop.

---
**Why use this?**
* No more "Connection Error" when Wi-Fi changes.
* You can demo the app to others without carrying your router.
* It mimics a "Real Deployment" before you pay for cloud hosting.
