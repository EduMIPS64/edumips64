const { test, expect } = require('@playwright/test');
const { targetUri, waitForPageReady } = require('./test-utils');

test('single step works', async ({ page }) => {
  await page.goto(targetUri);

  // Ensure basic elements are available.
  await waitForPageReady(page);
  await page.waitForSelector('#main-grid');

  // Click text=Load/Reset
  await page.click('#load-button');

  // Click text=Single Step
  await page.click('#step-button');

  await page.close();
});