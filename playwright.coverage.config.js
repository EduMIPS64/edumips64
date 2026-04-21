/**
 * Playwright configuration for local coverage runs.
 *
 * Differences from the default playwright.config.js:
 * - Starts a static file server that serves the Istanbul-instrumented build
 *   from out/web/ so that tests run locally without a staging deployment.
 * - Tests run fully in parallel (one worker per test) matching the default
 *   configuration; each worker writes its own coverage file to .nyc_output/.
 */
module.exports = {
  testDir: 'src/test/webapp',
  screenshots: 'only-on-failure',
  fullyParallel: true,
  workers: '100%',
  webServer: {
    command: 'npx serve out/web -l 8080 --no-clipboard',
    url: 'http://localhost:8080',
    reuseExistingServer: false,
    timeout: 30000,
  },
  use: {
    baseURL: 'http://localhost:8080',
  },
};
