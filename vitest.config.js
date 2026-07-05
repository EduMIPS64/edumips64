import { defineConfig } from 'vitest/config';

export default defineConfig({
  // Component tests are .tsx files; transpile their JSX with the automatic
  // runtime, matching the "jsx": "react-jsx" setting in tsconfig.json.
  esbuild: { jsx: 'automatic' },
  test: {
    // Only pick up unit tests in the dedicated unit-test directory.
    // The Playwright specs in src/test/webapp are excluded intentionally:
    // they use @playwright/test globals and expect a real browser.
    // Include .js/.ts/.tsx test files — vitest handles TS natively.
    include: ['src/test/webapp-unit/**/*.test.[jt]s?(x)'],
  },
});
