const { test, expect } = require('./fixtures');
const { targetUri, waitForPageReady } = require('./test-utils');

/**
 * Tests for the build-identity versioning and CANDIDATE chip (PR-A / web promotion).
 *
 * Coverage:
 *   - The About tab displays a non-empty version string.
 *   - On a normal (root/localhost) load the CANDIDATE chip is NOT present.
 *   - The CANDIDATE chip PRESENCE test requires the app to be served under a
 *     candidate path (e.g. /2026-06-13/1-abc1234/) which the local Playwright
 *     harness does not do; that path is therefore deferred to CI / manual verification.
 */

// Regex mirroring CANDIDATE_PATH_RE in versionHistory.js
const CANDIDATE_PATH_RE = /^\/(\d{4}-\d{2}-\d{2})\/(\d+)-([a-f0-9]{7,8})\//;

test.describe('version display and CANDIDATE chip', () => {
  test('About tab shows a non-empty version string', async ({ page }) => {
    await page.goto(targetUri);
    await waitForPageReady(page);

    await page.click('#help-button');
    await page.waitForSelector('.help-title');

    // Switch to About tab
    await page.click('#help-tab-1');

    const aboutContent = await page.textContent('.help-content');
    // Must contain the "Version:" label
    expect(aboutContent).toContain('Version:');

    // The version value must be non-empty (git-describe or fallback, never blank).
    // Locate the exact Typography node that holds "Version: <value>".
    const versionText = await page.locator('.help-content >> text=Version:').first().textContent();
    // Strip the "Version:" prefix and any surrounding whitespace.
    const versionValue = versionText.replace(/^Version:\s*/, '').trim();
    expect(versionValue.length).toBeGreaterThan(0);

    // git-describe format: "1.4.0", "1.4.0-74-ge1b45a15", "1.4.0-74-ge1b45a15-dirty",
    // or a bare short SHA (e.g. "ge1b45a15") when no tag is reachable.
    // We accept the full range via a permissive regex and at minimum assert non-empty.
    const gitDescribeOrSha = /^(\d+\.\d+\.\d+(-\d+-g[0-9a-f]+)?(-dirty)?|g?[0-9a-f]{7,})$/;
    expect(versionValue).toMatch(gitDescribeOrSha);

    await page.close();
  });

  test('CANDIDATE chip is absent on a normal (non-candidate) load', async ({ page }) => {
    await page.goto(targetUri);
    await waitForPageReady(page);

    // The chip has id="candidate-build-chip" and is only rendered when
    // window.location.pathname matches /<YYYY-MM-DD>/<n>-<shortsha>/ on
    // web.edumips.org.  On a localhost load at "/" the chip must not be in the DOM.
    const candidateChip = page.locator('#candidate-build-chip');
    await expect(candidateChip).toHaveCount(0);

    await page.close();
  });

  test('CANDIDATE chip detection is path-based (unit-level check)', async ({ page }) => {
    // Verify the detection regex in isolation without serving under a candidate path.
    await page.goto(targetUri);
    await waitForPageReady(page);

    const isCandidateOnRoot = await page.evaluate((re) =>
      new RegExp(re).test(window.location.pathname),
      CANDIDATE_PATH_RE.source
    );
    expect(isCandidateOnRoot).toBe(false);

    // Simulate what the component would compute for a valid candidate pathname.
    const isCandidateOnPath = await page.evaluate((re) =>
      new RegExp(re).test('/2026-06-13/1-abc1234/index.html'),
      CANDIDATE_PATH_RE.source
    );
    expect(isCandidateOnPath).toBe(true);

    // Another valid candidate path (different day, n=2, 8-char sha)
    const isCandidateN2 = await page.evaluate((re) =>
      new RegExp(re).test('/2026-06-14/2-deadbeef/'),
      CANDIDATE_PATH_RE.source
    );
    expect(isCandidateN2).toBe(true);

    // Old nightly path must NOT match
    const isNightlyPath = await page.evaluate((re) =>
      new RegExp(re).test('/nightly/'),
      CANDIDATE_PATH_RE.source
    );
    expect(isNightlyPath).toBe(false);

    await page.close();
  });
});
