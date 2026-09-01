/**
 * GesturGait AI - Central Network Configuration (React Native)
 *
 * Set USE_TUNNEL to true and provide your PUBLIC_URL to use the app
 * from any network (5G, external Wi-Fi).
 */
const NetworkConfig = {
  LOCAL_IP: '172.21.1.26',
  PORT: '5000',

  // CHANGE THIS to your public tunnel URL
  PUBLIC_URL: 'https://gesturgait.loca.lt',

  // Toggle this switch
  USE_TUNNEL: true,

  getBaseUrl() {
    return this.USE_TUNNEL ? this.PUBLIC_URL : `http://${this.LOCAL_IP}:${this.PORT}`;
  }
};

export default NetworkConfig;
