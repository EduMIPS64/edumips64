import { defineConfig } from 'vitest/config';

export default defineConfig({
  test: {
    // Only pick up unit tests in the dedicated unit-test directory.
    // The Playwright specs in src/test/webapp are excluded intentionally:
    // they use @playwright/test globals and expect a real browser.
    include: ['src/test/webapp-unit/**/*.test.js'],
  },
});
