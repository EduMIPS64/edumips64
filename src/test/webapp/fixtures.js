/**
 * Custom Playwright fixtures that collect Istanbul coverage data
 * (window.__coverage__) after each test when COVERAGE=true is set.
 *
 * All spec files should import { test, expect } from this module instead of
 * '@playwright/test' so that coverage is transparently collected without
 * requiring per-test changes.
 */

const { test: baseTest, expect } = require('@playwright/test');
const path = require('path');
const fs = require('fs');

const COVERAGE_DIR = path.join(process.cwd(), '.nyc_output');

const test = baseTest.extend({
  page: async ({ page }, use) => {
    await use(page);

    if (process.env.COVERAGE) {
      try {
        const coverage = await page.evaluate(() => window.__coverage__ || null);
        if (coverage) {
          if (!fs.existsSync(COVERAGE_DIR)) {
            fs.mkdirSync(COVERAGE_DIR, { recursive: true });
          }
          const unique = `${Date.now()}-${Math.random().toString(36).slice(2)}`;
          const file = path.join(COVERAGE_DIR, `${unique}.json`);
          fs.writeFileSync(file, JSON.stringify(coverage));
        }
      } catch (_) {
        // page may have been closed explicitly by the test; ignore
      }
    }
  },
});

module.exports = { test, expect };
