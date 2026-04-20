# GitHub Copilot Instructions for EduMIPS64

## Project Overview

EduMIPS64 is a free, cross-platform visual MIPS64 CPU simulator written in Java with a web-based UI using JavaScript/React. The project aims to be an educational tool for learning MIPS64 assembly language and computer architecture.

## Development Environment

### Dev Container
The repository includes a dev container configuration (`.devcontainer/devcontainer.json`) that provides a fully configured development environment. GitHub Codespaces will use this by default.

### Requirements
- Java JDK 17 or above
- Node.js 16+ (for web UI development)
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
  - `components/` - React components
  - `css/` - Stylesheets
  - `static/` - Static assets

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
- `./gradlew war` - Compile GWT-based web worker
- `./gradlew noHelpJar` - Build JAR without help files (faster for development)

### NPM Scripts (Web UI)
- `npm start` - Start webpack dev server with live reloading
- `npm run build-dbg` - Build with debugging symbols
- `npm run build` - Production build (minified)
- `npm test` - Run Playwright tests

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

## Code Style and Conventions

### Java
- Follow standard Java conventions
- Pay attention to initialization code when creating new test classes
- Look at existing code for patterns and style consistency

### JavaScript/React
- ESLint configuration in `.eslintrc.json`
- Extends: `eslint:recommended`, `plugin:react/recommended`, `plugin:react-hooks/recommended`, `prettier`
- Prettier for code formatting (`.prettierrc`)
- React prop-types validation is disabled

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
- Source: `org.edumips64.client` package
- Configuration: `webclient.gwt.xml`
- Build: `./gradlew war` produces `worker.js` in `out/web/`
- **Note**: The `war` task wipes the output directory, so rebuild the web UI after rebuilding the worker

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
- CI runs on pushes to `master` and all pull requests
