// Allow overriding the Chromium executable path for environments where the
// Playwright-bundled binary is unavailable (e.g. Ubuntu 26.04 before official
// Playwright support).  In CI the bundled binary is used; locally the system
// Chromium snap can be pointed to via this env var.
const executablePath = process.env.PLAYWRIGHT_CHROMIUM_EXECUTABLE_PATH || undefined;

const chromeUse = executablePath
    ? { launchOptions: { executablePath, args: ['--no-sandbox', '--disable-dev-shm-usage'] } }
    : {};

module.exports = {
    testDir: "src/test/webapp",
    screenshots: 'only-on-failure',
    // Run tests in parallel with unlimited workers (one per test)
    fullyParallel: true,
    workers: '100%',
    projects: [
        {
            name: 'chromium',
            use: chromeUse,
        },
    ],
};