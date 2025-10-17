import {defineConfig} from "vite"
import react from "@vitejs/plugin-react"
import path from "path"

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      "/api": {
        target: "http://localhost:8100",
        changeOrigin: true,
        secure: false,
      },
    },
  },
  build: {
    // 🏗️ Output directory for production build
    outDir: path.resolve(__dirname, "../src/main/resources/static"),

    // 🧹 Optional: clean the output folder before building
    emptyOutDir: true,
  },
})
