const { test, expect } = require('./fixtures');
const { targetUri, waitForPageReady } = require('./test-utils');

/**
 * Playwright e2e tests for the Candidate Builds section in the About tab.
 *
 * Mirrors version-history.spec.js's route-mocking approach:
 *   - Mock GET /candidates.json before page.goto so the fetch is intercepted on load.
 *   - Assert the About tab renders (or hides) #about-candidate-builds and
 *     [data-candidate] items with correct share hrefs.
 *
 * §6.3 coverage: tests 1, 3, and 4.
 * Test 2 (candidate banner when served from a candidate URL) requires hostname
 * spoofing beyond what a localhost harness supports and is deferred to CI.
 */

const mockCandidates = {
  candidates: [
    {
      date: '2026-06-13',
      n: 2,
      sha: 'abc1234def5678901234567890123456789012ab',
      shortsha: 'abc1234',
      path: '/2026-06-13/2-abc1234/',
      build: 'v2.0.1-14-gabc1234',
      targetRelease: '2.0.2',
      deployedAt: '2026-06-13T14:32:01Z',
    },
    {
      date: '2026-06-13',
      n: 1,
      sha: 'def5678901234567890123456789012abcdef56',
      shortsha: 'def5678',
      path: '/2026-06-13/1-def5678/',
      build: 'v2.0.1-13-gdef5678',
      targetRelease: '2.0.2',
      deployedAt: '2026-06-13T10:00:00Z',
    },
    {
      date: '2026-06-12',
      n: 1,
      sha: 'ghi9012345678901234567890123456789012ghi',
      shortsha: 'ghi9012',
      path: '/2026-06-12/1-ghi9012/',
      build: 'v2.0.1-12-gghi9012',
      targetRelease: '2.0.2',
      deployedAt: '2026-06-12T09:00:00Z',
    },
  ],
  retentionDays: 14,
};

async function openAboutTab(page) {
  await page.click('#help-button');
  await page.waitForSelector('.help-title');
  // Move mouse away to avoid tooltip intercepting tab click
  await page.mouse.move(0, 0);
  await page.click('#help-tab-1');
}

test.describe('candidate builds section in About tab', () => {
  test('A: shows candidate builds section when candidates.json is available', async ({
    page,
  }) => {
    // Mock candidates.json BEFORE page.goto so the fetch is intercepted on load.
    await page.route('**/candidates.json', (route) =>
      route.fulfill({
        contentType: 'application/json',
        body: JSON.stringify(mockCandidates),
      }),
    );

    await page.goto(targetUri);
    await waitForPageReady(page);
    await openAboutTab(page);

    // Section must be present
    const section = page.locator('#about-candidate-builds');
    await expect(section).toHaveCount(1);

    // Should list exactly 3 candidate items (one per entry)
    const items = section.locator('[data-candidate]');
    await expect(items).toHaveCount(3);

    await page.close();
  });

  test('B: section is absent when candidates.json returns 404', async ({
    page,
  }) => {
    await page.route('**/candidates.json', (route) =>
      route.fulfill({ status: 404 }),
    );

    await page.goto(targetUri);
    await waitForPageReady(page);
    await openAboutTab(page);

    const section = page.locator('#about-candidate-builds');
    await expect(section).toHaveCount(0);

    await page.close();
  });

  test('C: share links have correct /<date>/<n>-<sha>/ hrefs', async ({
    page,
  }) => {
    await page.route('**/candidates.json', (route) =>
      route.fulfill({
        contentType: 'application/json',
        body: JSON.stringify(mockCandidates),
      }),
    );

    await page.goto(targetUri);
    await waitForPageReady(page);
    await openAboutTab(page);

    const section = page.locator('#about-candidate-builds');
    await expect(section).toHaveCount(1);

    // Assert each [data-candidate] item has a link with the correct href
    for (const c of mockCandidates.candidates) {
      const dataAttr = `${c.date}-${c.n}`;
      const item = section.locator(`[data-candidate="${dataAttr}"]`);
      await expect(item).toHaveCount(1);

      const link = item.locator('a');
      await expect(link).toHaveCount(1);
      const href = await link.getAttribute('href');
      expect(href).toBe(c.path);
    }

    await page.close();
  });

  test('D: section is absent when candidates.json returns empty candidates array', async ({
    page,
  }) => {
    await page.route('**/candidates.json', (route) =>
      route.fulfill({
        contentType: 'application/json',
        body: JSON.stringify({ candidates: [], retentionDays: 14 }),
      }),
    );

    await page.goto(targetUri);
    await waitForPageReady(page);
    await openAboutTab(page);

    const section = page.locator('#about-candidate-builds');
    await expect(section).toHaveCount(0);

    await page.close();
  });
});
