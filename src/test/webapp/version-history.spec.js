const { test, expect } = require('./fixtures');
const { targetUri, waitForPageReady } = require('./test-utils');

const mockManifest = {
  current: 3,
  prev: 2,
  sha: 'abc1234',
  build: 'build-003',
  targetRelease: '1.5.0',
  promotedAt: '2026-05-01T12:00:00Z',
  promotedBy: 'tank',
  history: [
    {
      n: 3,
      build: 'build-003',
      sha: 'abc1234',
      targetRelease: '1.5.0',
      promotedAt: '2026-05-01T12:00:00Z',
      promotedBy: 'tank',
    },
    {
      n: 2,
      build: 'build-002',
      sha: 'def5678',
      targetRelease: '1.4.0',
      promotedAt: '2026-04-01T12:00:00Z',
      promotedBy: 'tank',
    },
    {
      n: 1,
      build: 'build-001',
      sha: 'ghi9012',
      targetRelease: '1.3.0',
      promotedAt: '2026-03-01T12:00:00Z',
      promotedBy: 'tank',
    },
  ],
};

async function openAboutTab(page) {
  await page.click('#help-button');
  await page.waitForSelector('.help-title');
  await page.click('#help-tab-1');
}

test.describe('version history in About tab', () => {
  test('A: shows previous versions section when manifest is available', async ({
    page,
  }) => {
    // Mock manifest BEFORE page.goto so the fetch is intercepted on load.
    await page.route('**/manifest.json', (route) =>
      route.fulfill({
        contentType: 'application/json',
        body: JSON.stringify(mockManifest),
      }),
    );

    await page.goto(targetUri);
    await waitForPageReady(page);
    await openAboutTab(page);

    // Section must be present
    const section = page.locator('#about-previous-versions');
    await expect(section).toHaveCount(1);

    // Should list exactly 3 version items (one per history entry)
    const items = section.locator('[data-version]');
    await expect(items).toHaveCount(3);

    // The current version (n=3) must NOT be a link
    const currentItem = section.locator('[data-version="3"]');
    await expect(currentItem.locator('a')).toHaveCount(0);
    await expect(currentItem.locator('.MuiChip-root')).toHaveCount(1);

    // A non-current entry (n=1) must have a link whose href contains /v/1/
    const v1Item = section.locator('[data-version="1"]');
    const v1Link = v1Item.locator('a');
    await expect(v1Link).toHaveCount(1);
    const href = await v1Link.getAttribute('href');
    expect(href).toContain('/v/1/');

    await page.close();
  });

  test('B: section is absent when no valid manifest is available', async ({
    page,
  }) => {
    // Intercept manifest.json so it always returns 404, ensuring fetchManifest()
    // returns null regardless of whether production serves a real manifest.
    await page.route('**/manifest.json', (route) =>
      route.fulfill({ status: 404 }),
    );

    await page.goto(targetUri);
    await waitForPageReady(page);
    await openAboutTab(page);

    const section = page.locator('#about-previous-versions');
    await expect(section).toHaveCount(0);

    await page.close();
  });
});
