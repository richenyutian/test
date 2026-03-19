import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  build: {
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (
            id.includes('node_modules/element-plus') ||
            id.includes('node_modules/@element-plus/icons-vue')
          ) {
            return 'element-plus'
          }

          if (
            id.includes('node_modules/vue/') ||
            id.includes('node_modules/vue-router') ||
            id.includes('node_modules/pinia')
          ) {
            return 'vue-vendor'
          }
        },
      },
    },
  },
})
