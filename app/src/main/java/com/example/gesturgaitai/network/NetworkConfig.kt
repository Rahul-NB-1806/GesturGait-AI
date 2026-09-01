package com.example.gesturgaitai.network

/**
 * GesturGait AI - Central Network Configuration
 * 
 * To "remove the private room" and use the phone from any network:
 * 1. Run 'npm run tunnel' in the backend folder.
 * 2. Copy the URL generated (e.g., https://gesturgait.loca.lt).
 * 3. Replace PUBLIC_URL below with your tunnel URL.
 * 4. Set USE_TUNNEL to true.
 */
object NetworkConfig {
    private const val LOCAL_IP = "172.21.1.26"
    private const val PORT = "5000"
    
    // CHANGE THIS to your public tunnel URL
    private const val PUBLIC_URL = "https://wise-fox-73.loca.lt"
    
    // Set to true to use the Public URL (works on 5G/different Wi-Fi)
    // Set to false to use the Local Wi-Fi (only works on same Wi-Fi)
    private const val USE_TUNNEL = true

    val BASE_URL: String
        get() = if (USE_TUNNEL) PUBLIC_URL else "http://$LOCAL_IP:$PORT"
}
