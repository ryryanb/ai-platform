import react from '@vitejs/plugin-react';
import path from 'path';
import { defineConfig } from 'vite';

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  build: {
    // Raise the warning limit a little if you’re comfortable with it
    chunkSizeWarningLimit: 800,   // ← uncommented

    rollupOptions: {
      output: {
        // Manual chunking strategy
        manualChunks(id) {
          if (id.includes('node_modules')) {
            // React + React‑DOM (already bundled as reactVendor by default)
            if (id.includes('react') || id.includes('react-dom')) {
              return 'reactVendor';
            }
            // Chart.js and any of your own chart utils
            if (id.includes('chart.js') || id.includes('/charts/')) {
              return 'charts';
            }
            // Date‑fns – you can split it if you like
            if (id.includes('date-fns')) {
              return 'date-fns';
            }
            // ───── NEW ─────
            // Lodash – split out its own chunk
            if (id.includes('lodash')) {
              return 'lodash';
            }
            // Moment.js – often large, put in its own file
            if (id.includes('moment')) {
              return 'moment';
            }
            // Axios – keep separate from the generic vendor chunk
            if (id.includes('axios')) {
              return 'axios';
            }
            // ───── END NEW ─────

            // Fallback for any other big third‑party libs
            return 'vendor';
          }
        }
      }
    }
  }
});