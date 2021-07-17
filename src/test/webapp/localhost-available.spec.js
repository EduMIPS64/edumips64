const { test, expect } = require('@playwright/test');

test('test', async ({ page }) => {

  // Go to http://localhost:8080/
  await page.goto('http://localhost:8080/');

  // Ensure basic elements are available.
  await page.waitForSelector('#load-button');
  await page.waitForSelector('#widgetGrid');
  await page.waitForSelector('.monaco-editor');

  // Click text=Load/Reset
  await page.click('text=Load/Reset');

  // Click text=Single Step
  await page.click('text=Single Step');

  await page.close();
});