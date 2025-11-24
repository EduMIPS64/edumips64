const { test, expect } = require('@playwright/test');

const targetUri = process.env.PLAYWRIGHT_TARGET_URL || "http://localhost:8080";

test('help dialog shows embedded documentation', async ({ page }) => {
  console.log("Running help dialog tests against", targetUri);
  await page.goto(targetUri);

  // Wait for the page to load
  await page.waitForSelector('#load-button');

  // Click the help button
  await page.click('.help-button');

  // Wait for the help dialog to appear
  await page.waitForSelector('.help-title');

  // Verify the dialog title
  const title = await page.textContent('.help-title');
  expect(title).toContain('EduMIPS64');

  // Check that the User Manual tab is present
  await page.waitForSelector('#help-tab-0');

  // Check that the About tab is present
  await page.waitForSelector('#help-tab-1');

  // Verify the iframe with documentation is present
  await page.waitForSelector('#help-iframe');

  // Verify the language selector is present
  await page.waitForSelector('#language-select');

  // Test language switching
  await page.click('#language-select');
  await page.click('li[data-value="it"]');
  
  // Wait for iframe src to change to Italian
  await page.waitForFunction(
    () => {
      const iframe = document.querySelector('#help-iframe');
      return iframe && iframe.src.includes('docs/it/html/index.html');
    },
    { timeout: 5000 }
  );
  
  // Verify the iframe src changed to Italian
  const iframeSrc = await page.getAttribute('#help-iframe', 'src');
  expect(iframeSrc).toContain('docs/it/html/index.html');

  // Switch to About tab
  await page.click('#help-tab-1');
  
  // Verify About content is displayed
  const aboutContent = await page.textContent('.help-content');
  expect(aboutContent).toContain('Version:');
  expect(aboutContent).toContain('Quick Start');

  // Close the dialog
  await page.click('text=Close');

  await page.close();
});

test('help documentation loads correctly in iframe', async ({ page }) => {
  console.log("Running documentation loading test against", targetUri);
  await page.goto(targetUri);

  // Wait for the page to load
  await page.waitForSelector('#load-button');

  // Click the help button
  await page.click('.help-button');

  // Wait for the iframe
  await page.waitForSelector('#help-iframe');

  // Get the iframe
  const iframe = await page.frameLocator('#help-iframe');

  // Wait for documentation content to load in iframe
  await iframe.locator('text=EduMIPS64').first().waitFor({ timeout: 10000 });

  // Verify documentation content is present
  const iframeContent = await iframe.locator('body').textContent();
  expect(iframeContent).toContain('EduMIPS64');

  await page.close();
});
