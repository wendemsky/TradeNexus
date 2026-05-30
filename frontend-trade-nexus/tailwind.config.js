/** @type {import('tailwindcss').Config} */
module.exports = {
  darkMode: 'class',
  content: ['./src/**/*.{html,ts}'],
  theme: {
    extend: {
      colors: {
        background:        'var(--color-background)',
        surface:           'var(--color-surface)',
        'surface-raised':  'var(--color-surface-raised)',
        border:            'var(--color-border)',
        'text-primary':    'var(--color-text-primary)',
        'text-secondary':  'var(--color-text-secondary)',
        brand:             'var(--color-brand)',
        'brand-hover':     'var(--color-brand-hover)',
        positive:          'var(--color-positive)',
        negative:          'var(--color-negative)',
        caution:           'var(--color-caution)',
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
        mono: ['JetBrains Mono', 'Fira Code', 'monospace'],
      },
    },
  },
  plugins: [],
};
