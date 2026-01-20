import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  define: {
    global: 'globalThis',
  },
  server: {
    port: 5173,
    // Proxy is optional - if API Gateway is on different port/domain, configure via VITE_API_BASE_URL
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        // Keep /api prefix for API Gateway
      }
    }
  }
})

