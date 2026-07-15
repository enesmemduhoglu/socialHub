import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    proxy: {
      // Dev'de CORS/env gerektirmeden backend'e ulaş; prod'da nginx aynı yönlendirmeyi yapacak
      '/api': 'http://localhost:8080',
    },
  },
})
