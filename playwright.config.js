const { devices } = require('@playwright/test');

module.exports = {
    testDir: "src/test/webapp",
    screenshots: 'only-on-failure',
    // Run tests in parallel with unlimited workers (one per test)
    fullyParallel: true,
    workers: '100%',
    // Test across multiple browsers (Chromium, Firefox, WebKit) and a mobile viewport.
    projects: [
        {
            name: 'chromium',
            use: { ...devices['Desktop Chrome'] },
        },
        {
            name: 'firefox',
            use: { ...devices['Desktop Firefox'] },
        },
        {
            name: 'webkit',
            use: { ...devices['Desktop Safari'] },
        },
        {
            name: 'mobile-chrome',
            use: { ...devices['Pixel 5'] },
        },
    ],
};