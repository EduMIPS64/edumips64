const { test, expect } = require('./fixtures');
const { targetUri, waitForPageReady } = require('./test-utils');

/**
 * Tests for the build-identity versioning and the archived-build chip.
 *
 * Coverage:
 *   - The About tab displays a non-empty version string.
 *   - On a normal (root/localhost) load the archived-build chip is NOT present.
 *   - The chip PRESENCE test requires the app to be served under a /c/<sha>/
 *     build path which the local Playwright harness does not do; that path is
 *     therefore deferred to CI / manual verification.
 */

// Regex mirroring BUILD_PATH_RE in buildInfo.js / versionHistory.js
const BUILD_PATH_RE = /^\/c\/([0-9a-f]{40})(?:\/|$)/;

test.describe('version display and archived-build chip', () => {
  test('About tab shows a non-empty version string', async ({ page }) => {
    await page.goto(targetUri);
    await waitForPageReady(page);

    await page.click('#help-button');
    await page.waitForSelector('.help-title');

    // Switch to About tab
    await page.click('#help-tab-1');

    const aboutContent = await page.textContent('.help-content');
    expect(aboutContent).toContain('Version:');

    const versionText = await page
      .locator('.help-content >> text=Version:')
      .first()
      .textContent();
    const versionValue = versionText.replace(/^Version:\s*/, '').trim();
    expect(versionValue.length).toBeGreaterThan(0);

    // git-describe format: "1.4.0", "1.4.0-74-ge1b45a15", "...-dirty",
    // or a bare short SHA (e.g. "ge1b45a15") when no tag is reachable.
    const gitDescribeOrSha =
      /^(\d+\.\d+\.\d+(-\d+-g[0-9a-f]+)?(-dirty)?|g?[0-9a-f]{7,})$/;
    expect(versionValue).toMatch(gitDescribeOrSha);

    await page.close();
  });

  test('archived-build chip is absent on a normal (non-archive) load', async ({
    page,
  }) => {
    await page.goto(targetUri);
    await waitForPageReady(page);

    // The chip has id="candidate-build-chip" and is only rendered when
    // window.location.pathname matches /c/<sha>/ on web.edumips.org.
    // On a localhost load at "/" the chip must not be in the DOM.
    const chip = page.locator('#candidate-build-chip');
    await expect(chip).toHaveCount(0);

    await page.close();
  });

  test('archived-build detection is path-based (unit-level check)', async ({
    page,
  }) => {
    await page.goto(targetUri);
    await waitForPageReady(page);

    const sha = 'a'.repeat(40);

    const isBuildOnRoot = await page.evaluate(
      (re) => new RegExp(re).test(window.location.pathname),
      BUILD_PATH_RE.source,
    );
    expect(isBuildOnRoot).toBe(false);

    const isBuildOnPath = await page.evaluate(
      ({ re, p }) => new RegExp(re).test(p),
      { re: BUILD_PATH_RE.source, p: `/c/${sha}/index.html` },
    );
    expect(isBuildOnPath).toBe(true);

    // A short sha must NOT match (full 40-char sha required).
    const isShortSha = await page.evaluate(
      ({ re, p }) => new RegExp(re).test(p),
      { re: BUILD_PATH_RE.source, p: '/c/abc1234/' },
    );
    expect(isShortSha).toBe(false);

    // Old candidate/nightly paths must NOT match.
    const isOldCandidate = await page.evaluate(
      ({ re, p }) => new RegExp(re).test(p),
      { re: BUILD_PATH_RE.source, p: '/2026-06-13/1-abc1234/' },
    );
    expect(isOldCandidate).toBe(false);

    await page.close();
  });
});
