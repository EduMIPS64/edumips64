const { test, expect } = require('./fixtures');
const { targetUri, waitForPageReady } = require('./test-utils');

/**
 * Playwright e2e tests for the unified version list in the About tab.
 *
 * The web UI fetches a single /versions.json indexing every retained build
 * (promoted versions + pending candidates), each addressed by its commit SHA
 * and served from /c/<sha>/. The About tab renders two sub-lists:
 *   - #about-promoted-versions  (prominent)
 *   - #about-candidate-versions (pending candidates, newer than current)
 *
 * Route-mocking mirrors the prior specs: mock GET /versions.json before
 * page.goto so the fetch is intercepted on load.
 */

const PROMOTED_SHA = 'a'.repeat(40);
const OLD_PROMOTED_SHA = 'b'.repeat(40);
const CANDIDATE_SHA = 'c'.repeat(40);

const mockVersions = {
  current: PROMOTED_SHA,
  versions: [
    {
      sha: CANDIDATE_SHA,
      shortsha: CANDIDATE_SHA.slice(0, 7),
      seq: 1186,
      build: 'build-1186',
      targetRelease: '1.5.0',
      pushedAt: '2026-06-14T08:00:00Z',
      promoted: false,
    },
    {
      sha: PROMOTED_SHA,
      shortsha: PROMOTED_SHA.slice(0, 7),
      seq: 1185,
      build: 'build-1185',
      targetRelease: '1.5.0',
      pushedAt: '2026-06-13T08:00:00Z',
      promoted: true,
      promotedAt: '2026-06-13T09:00:00Z',
      promotedBy: 'lupino3',
    },
    {
      sha: OLD_PROMOTED_SHA,
      shortsha: OLD_PROMOTED_SHA.slice(0, 7),
      seq: 1100,
      build: 'build-1100',
      targetRelease: '1.4.0',
      pushedAt: '2026-05-01T08:00:00Z',
      promoted: true,
      promotedAt: '2026-05-01T09:00:00Z',
      promotedBy: 'lupino3',
    },
  ],
};

async function openAboutTab(page) {
  await page.click('#help-button');
  await page.waitForSelector('.help-title');
  // Move mouse away to avoid tooltip intercepting the tab click.
  await page.mouse.move(0, 0);
  await page.click('#help-tab-2');
}

test.describe('unified version list in About tab', () => {
  test('A: shows promoted and candidate sections when versions.json is available', async ({
    page,
  }) => {
    await page.route('**/versions.json', (route) =>
      route.fulfill({
        contentType: 'application/json',
        body: JSON.stringify(mockVersions),
      }),
    );

    await page.goto(targetUri);
    await waitForPageReady(page);
    await openAboutTab(page);

    const section = page.locator('#about-versions');
    await expect(section).toHaveCount(1);

    // Two promoted versions, one candidate.
    const promoted = page.locator('#about-promoted-versions [data-version]');
    await expect(promoted).toHaveCount(2);
    const candidates = page.locator('#about-candidate-versions [data-version]');
    await expect(candidates).toHaveCount(1);

    // The current promoted version must NOT be a link and must show "current".
    const currentItem = section.locator(`[data-version="${PROMOTED_SHA}"]`);
    await expect(currentItem.locator('a')).toHaveCount(0);
    await expect(currentItem.locator('.MuiChip-root')).toHaveCount(1);

    // An older promoted version links to /c/<sha>/.
    const oldItem = section.locator(`[data-version="${OLD_PROMOTED_SHA}"]`);
    const oldLink = oldItem.locator('a');
    await expect(oldLink).toHaveCount(1);
    expect(await oldLink.getAttribute('href')).toBe(`/c/${OLD_PROMOTED_SHA}/`);

    // The candidate links to /c/<sha>/ and carries a "candidate" chip.
    const candItem = section.locator(`[data-version="${CANDIDATE_SHA}"]`);
    const candLink = candItem.locator('a');
    await expect(candLink).toHaveCount(1);
    expect(await candLink.getAttribute('href')).toBe(`/c/${CANDIDATE_SHA}/`);

    await page.close();
  });

  test('B: section is absent when versions.json returns 404', async ({
    page,
  }) => {
    await page.route('**/versions.json', (route) =>
      route.fulfill({ status: 404 }),
    );

    await page.goto(targetUri);
    await waitForPageReady(page);
    await openAboutTab(page);

    await expect(page.locator('#about-versions')).toHaveCount(0);

    await page.close();
  });

  test('C: section is absent when versions array is empty', async ({
    page,
  }) => {
    await page.route('**/versions.json', (route) =>
      route.fulfill({
        contentType: 'application/json',
        body: JSON.stringify({ current: null, versions: [] }),
      }),
    );

    await page.goto(targetUri);
    await waitForPageReady(page);
    await openAboutTab(page);

    await expect(page.locator('#about-versions')).toHaveCount(0);

    await page.close();
  });
});
