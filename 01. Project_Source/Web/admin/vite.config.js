import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import { SERVERURL  } from './src/utils/constants.js';

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/member': {
        target: `${SERVERURL}`,
        changeOrigin: true,
        secure: false
      },
      '/cert': {
        target: `${SERVERURL}`,
        changeOrigin: true,
        secure: false
      },
      '/place': {
        target: `${SERVERURL}`,
        changeOrigin: true,
        secure: false
      },
      '/places': {
        target: `${SERVERURL}`,
        changeOrigin: true,
        secure: false
      }
    }
  },
  historyApiFallback: true

})
