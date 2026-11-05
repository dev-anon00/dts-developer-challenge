/// <reference types="vitest/config" />
import react from '@vitejs/plugin-react';
import { defineConfig } from 'vite';

// Forward /api/* to the backend so the browser doesn't need CORS
const proxy = {
  '/api': {
    target: process.env.BACKEND_URL ?? 'http://localhost:4000',
    rewrite: (path: string) => path.replace(/^\/api/, ''),
  },
};

export default defineConfig({
  plugins: [react()],
  server: { port: 3100, proxy },
  preview: { port: 3100, proxy },
  // govuk-frontend's CSS still contains old IE hacks that the minifier rejects
  css: { lightningcss: { errorRecovery: true } },
  test: { environment: 'jsdom', setupFiles: ['./src/test/setup.ts'] },
});
