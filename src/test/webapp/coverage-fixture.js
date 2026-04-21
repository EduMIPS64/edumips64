/**
 * Playwright fixture that collects Istanbul code coverage (window.__coverage__)
 * after each test and writes it to .nyc_output/ for later merging.
 *
 * Tests import { test, expect } from this module instead of '@playwright/test'
 * when they want coverage collection.  Coverage is only collected when the
 * COVERAGE environment variable is set (e.g. by the `test:coverage` npm script).
 */
const { test: base, expect } = require('@playwright/test');
const fs = require('fs');
const path = require('path');

const test = base.extend({
  /**
   * Override the `page` fixture so that, after each test, coverage data
   * stored in window.__coverage__ by babel-plugin-istanbul is persisted to
   * a JSON file in .nyc_output/.
   */
  page: async ({ page }, use) => {
    await use(page);

    if (process.env.COVERAGE) {
      const coverage = await page.evaluate(() => window.__coverage__);
      if (coverage) {
        const outputDir = path.resolve('.nyc_output');
        if (!fs.existsSync(outputDir)) {
          fs.mkdirSync(outputDir, { recursive: true });
        }
        const filename = `coverage-${Date.now()}-${Math.random()
          .toString(36)
          .slice(2)}.json`;
        fs.writeFileSync(
          path.join(outputDir, filename),
          JSON.stringify(coverage)
        );
      }
    }
  },
});

module.exports = { test, expect };
