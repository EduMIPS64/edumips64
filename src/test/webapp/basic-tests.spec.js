const { test, expect } = require('@playwright/test');

const targetUri = process.env.PLAYWRIGHT_TARGET_URL || "http://localhost:8080";

test('test', async ({ page }) => {
  console.log("Running tests against", targetUri);
  await page.goto(targetUri);

  // Ensure basic elements are available.
  await page.waitForSelector('#load-button');
  await page.waitForSelector('#main-grid');
  await page.waitForSelector('.monaco-editor');

  // Click text=Load/Reset
  await page.click('text=Load');

  // Click text=Single Step
  await page.click('text=Single Step');

  await page.close();
});