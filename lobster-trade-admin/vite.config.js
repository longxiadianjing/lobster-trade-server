import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'
import history from 'connect-history-api-fallback'

export default defineConfig({
  plugins: [vue()],

  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    port: 8090,
    configureServer(server) {
      server.middlewares.use(history({
        disableDotRule: false,
        htmlAcceptHeaders: ['text/html', 'application/xhtml+xml']
      }))
    },
    proxy: {
      '/api/admin': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (p) => p.replace(/^\/api\/admin/, '/api/admin')
      },
      '/api/certification': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/api/order': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/api/product': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/api/im': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/api/user': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/api/cs': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/api/hot-search': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/api/stats': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/api/wallet': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/api/games': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/api/announcement': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/api/notification': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/api/payment': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
