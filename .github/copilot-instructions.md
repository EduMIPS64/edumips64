# GitHub Copilot Instructions for EduMIPS64

## Project Overview

EduMIPS64 is a free, cross-platform visual MIPS64 CPU simulator written in Java with a web-based UI using JavaScript/React. The project aims to be an educational tool for learning MIPS64 assembly language and computer architecture.

## Development Environment

### Dev Container
The repository includes a dev container configuration (`.devcontainer/devcontainer.json`) that provides a fully configured development environment. GitHub Codespaces will use this by default.

### Requirements
- Java JDK 17 or above
- Node.js 24+ (for web UI development; version pinned in `.nvmrc`)
- Python 3.12+ with pip (for documentation)
- Gradle (wrapper included in the repository)

## Project Structure

### Source Code Layout
The project follows Gradle project layout conventions:

- **`src/main/java/org/edumips64/`** - Main Java source code
  - `Main.java` - Entry point for Swing UI
  - `MainCLI.java` - Experimental CLI frontend
  - `client/` - Web UI Java code (GWT compilation)
  - `core/` - Core simulator classes (CPU, Memory, Instructions, Parser)
  - `ui/` - Swing UI code
  - `utils/` - Miscellaneous utilities and abstractions

- **`src/webapp/`** - Web UI React components
  - `components/` - React components (`Simulator.js` is the root, plus `Header.js`, `Registers.js`, `Pipeline.js`, `Memory.js`, `Statistics.js`, `Code.js`, `Settings.js`, `CacheConfig.js`, `ErrorList.js`, `StdOut.js`, `InputDialog.js`, `HelpDialog.js`, `CpuStatusDisplay.js`, `ErrorDisplay.js`)
  - `css/` - Stylesheets
  - `data/` - Static data (e.g., `SampleProgram.js` contains the default MIPS64 program)
  - `settings/` - Settings management (`useSetting.js` hook, `SettingKey.js` keys, `schema.js` validation)
  - `static/` - Static assets (HTML, CSS, images)
  - `index.js` - App entry point (creates web worker from `worker.js`, initializes React app)

- **`src/test/`** - Test code
  - `java/` - Java unit tests
  - `resources/` - MIPS64 test programs for end-to-end tests
  - `webapp/` - Web UI tests

- **`docs/`** - User documentation (Sphinx-based)

## Build System

### Gradle Tasks
- `./gradlew assemble` - Compile and assemble JAR artifacts
- `./gradlew check` - Run tests and compile documentation
- `./gradlew run` - Run the application
- `./gradlew war` - Compile GWT-based web worker (produces `worker.js` in `out/tmp/gwt-war/web/`)
- `./gradlew assembleWebApp` - Copies the GWT output into `out/web/` (depends on `gwtCompile`)
- `./gradlew webapp` - Full web build: GWT worker + HTML docs + npm build + copy docs into bundle
- `./gradlew noHelpJar` - Build JAR without help files (faster for development)

**Note:** Gradle's `layout.buildDirectory` is configured to `out/` (not the default `build/`), so all Gradle outputs go under `out/`.

### NPM Scripts (Web UI)
- `npm start` - Start webpack dev server with live reloading (serves from `out/web/`)
- `npm run build-dbg` - Build with debugging symbols (outputs `ui.js` to `out/web/`)
- `npm run build` - Production build (minified, outputs `ui.js` to `out/web/`)
- `npm test` - Run Playwright tests (uses `playwright.config.js` in project root)
- `npm run test:coverage` - Run Playwright tests with Istanbul coverage collection
- `npm run report:coverage` - Generate lcov/text coverage report from `.nyc_output/`

## Testing Guidelines

### General Testing Principles
- All new features **must** include unit tests with good coverage
- The `master` branch must always be in a working state (all tests passing)
- Unit tests are stored in `src/test/`

### Test Types

#### Java Unit Tests
- **End-to-end tests**: `EndToEndTests.java` - Runs MIPS64 assembly programs
  - Test failures trigger `BREAK` instruction → `BreakException`
- **CPU tests**: `CpuTests.java` - Tests forwarding, Dinero Tracefile generation
- **Component tests**: `ParserTest.java`, `MemoryTest.java`, etc. - Test individual components in isolation

#### Test Patterns
- Assembly tests go in `CpuTests.java` (has boilerplate for executing assembly)
- Component-specific tests should be in dedicated test classes
- Look at existing test classes for proper initialization patterns
- Swing UI code is excluded from coverage reports

#### Web UI Tests
- Playwright tests in `src/test/webapp/`
- Run with `npm test`
- **Test fixtures:** `src/test/webapp/fixtures.js` provides custom `test` and `expect` exports that transparently collect Istanbul coverage when `COVERAGE=true` is set. All spec files should import from `./fixtures` instead of `@playwright/test`.
- **Test utilities:** `src/test/webapp/test-utils.js` provides shared helpers:
  - `targetUri` - The base URL (defaults to `http://localhost:8080`, overridable via `PLAYWRIGHT_TARGET_URL`)
  - `removeOverlay(page)` - Removes the webpack dev server error overlay
  - `waitForPageReady(page)` - Waits for `#load-button`, Monaco editor, and `window.monaco`
  - `waitForRunningState(page)` - Waits for `#step-button` to become enabled
  - `waitForSimulationComplete(page)` - Waits for cycles > 0 and `#clear-code-button` enabled
  - `loadProgram(page, program)` - Types a program into the Monaco editor and clicks Load
  - `runToCompletion(page)` - Clicks Run All and waits for simulation to complete
- **Playwright config:** `playwright.config.js` in the project root; `testDir` is `src/test/webapp/`

##### Running Web UI Tests Locally

The Playwright tests require a running web server with the GWT web worker (`worker.js`) available. Follow these steps:

1. **Build the GWT web worker** (required once, or when Java code changes):
   ```bash
   ./gradlew assembleWebApp
   ```
   This compiles the Java core via GWT and copies `worker.js` into `out/web/`.

2. **Start the webpack dev server:**
   ```bash
   npm start
   ```
   This serves the React UI from `out/web/` on `http://localhost:8080`.
   The dev server serves `worker.js` from `out/web/` as static content.

3. **Run the tests** (in another terminal):
   ```bash
   npm test                                    # all tests, default browser
   npx playwright test basic-tests.spec.js     # single test file
   npx playwright test --reporter=list         # verbose output
   ```

**Common pitfall:** If the page loads as a blank white screen and tests time out waiting for `#load-button`, it almost always means `out/web/worker.js` is missing. Run `./gradlew assembleWebApp` to fix.

##### Web UI Test Conventions
- Each spec file is `src/test/webapp/<name>.spec.js`
- Import `{ test, expect }` from `./fixtures` (not from `@playwright/test` directly)
- Use helpers from `./test-utils.js` for common operations
- UI elements are identified by `id` attributes (e.g., `#load-button`, `#step-button`, `#run-button`, `#stop-button`, `#clear-code-button`, `#stat-cycles`, `#stat-instructions`, `#registers`, `#pipeline`, `#memory`, `#statistics`)
- Close the page with `await page.close()` at the end of each test

## Code Style and Conventions

### Java
- Follow standard Java conventions
- Pay attention to initialization code when creating new test classes
- Look at existing code for patterns and style consistency

### JavaScript/React
- ESLint configuration in `eslint.config.mjs` (flat config format for ESLint 10+)
- Uses `@eslint/js` recommended, `@eslint-react/eslint-plugin` recommended, `eslint-config-prettier`, and `eslint-plugin-prettier`
- Prettier for code formatting (`.prettierrc`)
- ESLint only covers `src/webapp/**/*.{js,jsx}` files; test files under `src/test/webapp/` use CommonJS (`require`) and are not linted by ESLint

## Development Workflow

### GitHub Flow
We use the [GitHub Flow](http://scottchacon.com/2011/08/31/github-flow.html) workflow:
- `master` branch is always fully working
- Development happens in feature branches
- Pull requests must pass CI checks before merging
- No direct commits to `master` (protected branch)

### Pull Request Requirements
- All tests must pass (GitHub Actions CI)
- Code must build successfully
- New features must include unit tests
- Follow existing code patterns and conventions

## GWT Web Worker

The EduMIPS64 core is cross-compiled to JavaScript using GWT and runs as a Web Worker:
- Source: `org.edumips64.client` package (entry point: `Worker.java`)
- Configuration: `webclient.gwt.xml`
- Build pipeline:
  1. `./gradlew gwtCompile` → produces `worker.js` in `out/tmp/gwt-war/web/`
  2. `./gradlew assembleWebApp` → copies GWT output from `out/tmp/gwt-war/web/` into `out/web/`
  3. `./gradlew war` → runs `gwtCompile` (but does **not** copy to `out/web/`; use `assembleWebApp` for that)
- The webpack dev server (`npm start`) serves static files from `out/web/`, so `worker.js` must be present there before starting the dev server or running Playwright tests.
- **Note**: The `war` task wipes the output directory, so rebuild the web UI after rebuilding the worker.
- The `webapp` Gradle task orchestrates the full build: `war` → `htmlDocs` → `assembleWebApp` → `npmBuild` → `copyWebHelp`.

## Documentation

### User Documentation
- Built with Sphinx (Python)
- Source: `docs/` directory
- Available online at http://edumips64.rtfd.io
- Build with `./gradlew check` (includes documentation compilation)
- Requirements in `docs/requirements.txt`
- User documentation is localized in three languages, each under its own subdirectory:
  - English: `docs/user/en/`
  - Italian: `docs/user/it/`
  - Chinese: `docs/user/zh/`
- **Any user-facing change MUST be documented in all three languages.** When
  updating a user-facing page (e.g. `source-files-format.rst`, `instructions.rst`,
  `examples.rst`, `fpu.rst`, `user-interface.rst`, etc.), apply the equivalent
  change to the matching file under `docs/user/en/`, `docs/user/it/` and
  `docs/user/zh/`. If you cannot translate the change accurately, mirror the
  structure and leave a clear English note in the non-English files so a native
  speaker can refine the wording later, but do not skip any language.

### Developer Documentation
- Developer guide: `docs/developer-guide.md`
- README: `readme.md`
- Code of Conduct: `CODE_OF_CONDUCT.md`
- **Any developer-facing change MUST be documented in the developer docs.**
  This includes changes to how developers interact with the EduMIPS64 source
  code, such as: new or changed Gradle tasks, build/test/lint commands, project
  structure, CI workflows, dev-container setup, coding conventions, release
  process, or new developer tooling. Update `docs/developer-guide.md` (and
  `readme.md` if it is affected) as part of the same change.

## Important Guidelines for Code Changes

### Minimal Changes
- Make the smallest possible changes to achieve the goal
- Don't remove or modify working code unless absolutely necessary
- Don't fix unrelated bugs or broken tests
- Focus on the specific issue at hand

### Security
- No secrets in source code
- Validate changes don't introduce security vulnerabilities
- Fix any vulnerabilities related to your changes

### Dependencies
- Use existing libraries whenever possible
- Only add/update dependencies if absolutely necessary
- For npm dependencies: Check package security

## Community and Support

- **Code of Conduct**: Contributor Covenant - report issues to andrea.spadaccini@gmail.com
- **IRC**: #edumips64 on Libera.chat (https://web.libera.chat/)
- **Website**: http://www.edumips.org
- **Blog**: http://edumips64.blogspot.com

## Platform-Specific Notes

### Windows
- Builds natively (PowerShell) and in WSL
- WiX toolset required for MSI packages (`./gradlew msi`)

### Mac OS X
- Tested on Catalina 10.15.2 with AdoptOpenJDK 11.0.7
- May need to trust Let's Encrypt certificates for Gradle GWT plugins

## Continuous Integration

- GitHub Actions: https://github.com/EduMIPS64/edumips64/actions
- All PRs must pass CI checks
- CI runs on `pull_request_target` to `master` and on a daily schedule
- **CI workflow structure** (`ci.yml`):
  - `build-desktop` - Builds and tests the Java desktop application (JAR + tests + Codecov)
  - `build-web` - Builds the full web app (`./gradlew webapp`), uploads `out/web/` as artifact
  - `test-web-coverage` - Downloads the web artifact, rebuilds webpack with Istanbul coverage instrumentation, runs Playwright tests with `COVERAGE=true`, uploads lcov to Codecov
  - `deploy-staging` - Deploys the web app to Azure blob storage for PR preview
  - `test-web` - Runs Playwright tests against the deployed staging URL (only on PRs)
- **Web UI tests in CI** install only Chromium by default (`npx playwright install --with-deps chromium`); the `test-web` job installs all browsers (`npx playwright install`)
- The `PLAYWRIGHT_TARGET_URL` env var controls the base URL for tests (defaults to `http://localhost:8080`)

## Maintaining These Instructions

- **Always update this file** (`copilot-instructions.md`) when you discover new project setup details, build quirks, environment requirements, or common pitfalls during development.
- If a build step, test command, or project convention is not documented here but you had to figure it out, add it so future sessions don't repeat the discovery.
- Keep the information accurate: if you find an existing section is outdated or incorrect, fix it.
