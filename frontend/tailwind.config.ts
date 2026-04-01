import type { Config } from "tailwindcss";

const config: Config = {
  content: [
    "./src/pages/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/components/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/app/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/features/**/*.{js,ts,jsx,tsx,mdx}",
  ],
  theme: {
    extend: {
      colors: {
        theme: {
          bg: "#fdfbf7",
          text: "#2a2a2a",
          muted: "#737373",
          accent: "#ca9a5b",
        }
      },
      fontFamily: {
        sans: ["var(--font-inter)", "sans-serif"],
      }
    },
  },
  plugins: [],
};

export default config;
