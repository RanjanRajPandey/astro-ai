/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        cosmic: {
          950: '#070913',
          900: '#0D1124',
          800: '#161D3A',
          700: '#232E56',
          gold: '#D4AF37',
          amber: '#F59E0B',
          parchment: '#FAF6EE',
        }
      }
    },
  },
  plugins: [],
}
