const { test, expect } = require('./fixtures');

const targetUri = process.env.PLAYWRIGHT_TARGET_URL || 'http://localhost:8080';

test('help dialog shows embedded documentation with navigation', async ({
  page,
}) => {
  await page.goto(targetUri);

  // Wait for the page to load
  await page.waitForSelector('#load-button');

  // Click the help button
  await page.click('#help-button');

  // Wait for the help dialog to appear
  await page.waitForSelector('.help-title');

  // The dialog must use most of the viewport for readability: it is styled to
  // 90vh tall and (near) full width via slotProps.paper. Guard against
  // regressions such as the MUI v9 `PaperProps` removal (which collapsed the
  // height to the content's ~2/5 of the viewport) and a `width: 'auto'` Paper
  // override (which shrank the manual to less than half the viewport width).
  const paperDims = await page.evaluate(() => {
    const paper = document.querySelector('.MuiDialog-paper');
    if (!paper) return { height: 0, width: 0 };
    const rect = paper.getBoundingClientRect();
    return {
      height: rect.height / window.innerHeight,
      width: rect.width / window.innerWidth,
    };
  });
  expect(paperDims.height).toBeGreaterThan(0.8);
  expect(paperDims.width).toBeGreaterThan(0.8);

  // Verify the dialog title
  const title = await page.textContent('.help-title');
  expect(title).toContain('EduMIPS64');

  // Check that the User Manual tab is present
  await page.waitForSelector('#help-tab-0');

  // Check that the Shortcuts tab is present (now tab 1)
  await page.waitForSelector('#help-tab-1');

  // Check that the About tab is present (now tab 2)
  await page.waitForSelector('#help-tab-2');

  // Verify the iframe with documentation is present
  await page.waitForSelector('#help-iframe');

  // Verify the language selector is present
  await page.waitForSelector('#language-select');

  // Verify navigation is present (check for at least one navigation item)
  const navigationItems = await page.$$('#toc-item-introduction');
  expect(navigationItems.length).toBeGreaterThan(0);

  // Test language switching
  await page.click('#language-select');
  await page.click('li[data-value="it"]');

  // Wait for iframe src to change to Italian
  await page.waitForFunction(
    () => {
      const iframe = document.querySelector('#help-iframe');
      return iframe?.src.includes('docs/it/html/index.html');
    },
    { timeout: 5000 },
  );

  // Verify the iframe src changed to Italian
  const iframeSrc = await page.getAttribute('#help-iframe', 'src');
  expect(iframeSrc).toContain('docs/it/html/index.html');

  // Switch to About tab (now tab 2)
  await page.click('#help-tab-2');

  // Verify About content is displayed
  const aboutContent = await page.textContent('.help-content');
  expect(aboutContent).toContain('Version:');
  expect(aboutContent).toContain('Quick Start');

  // Close the dialog
  await page.click('#help-close-button');

  await page.close();
});

test('help documentation loads correctly in iframe', async ({ page }) => {
  await page.goto(targetUri);

  // Wait for the page to load
  await page.waitForSelector('#load-button');

  // Click the help button
  await page.click('#help-button');

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

test('help navigation allows browsing different sections', async ({ page }) => {
  await page.goto(targetUri);

  // Wait for the page to load
  await page.waitForSelector('#load-button');

  // Click the help button
  await page.click('#help-button');

  // Wait for the iframe
  await page.waitForSelector('#help-iframe');

  // Click to expand "Instruction Set" section
  await page.click('#toc-item-the-instruction-set');

  // Wait for the submenu to expand
  await page.waitForTimeout(500);

  // Click on a sub-item "ALU Instructions"
  await page.click('#toc-item-alu-instructions');

  // Wait for navigation
  await page.waitForTimeout(500);

  // Verify the iframe src changed
  const iframeSrc = await page.getAttribute('#help-iframe', 'src');
  expect(iframeSrc).toContain(
    'docs/en/html/instructions.html#alu-instructions',
  );

  await page.close();
});
