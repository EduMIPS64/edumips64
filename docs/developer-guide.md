### Table of Contents

[Requirements](#requirements)

[Main Gradle tasks](#main-gradle-tasks)

[Working on the Web UI](#working-on-the-web-ui)

[Source code structure](#source-code-structure)

[User manual structure](#user-manual-structure)

[Submitting code](#submitting-code)

[Unit tests](#unit-tests)

[Compiling under Windows](#windows)

[Compiling under Mac OSX](#mac-os-x)

[Versioning model](#versioning-model)

[Web production promotion](#web-production-promotion)

### Requirements

#### Dev Container
All requirements are available in the Development Container image described in the `.devcontainer/devcontainer.json` dev container. See https://containers.dev/ for documentation on dev containers.

Github codespaces will use the dev container by default and give you a fully set up dev environment, useable for desktop, web and documentation development work.

#### List of requirements

In order to compile EduMIPS64, you need the Java JDK version 17 or above.

To build the user documentation, you'll need Python 3.14+ with pip.

[Gradle](https://gradle.org/) will download the following dependencies:

- JUnit
- JavaHelp
- GWT (experimental)
- Python dependencies to build the documentation (they'll be installed in a virtual environment)
  - Sphinx (http://www.sphinx-doc.org/)
  - rst2pdf (for the PDF files)

See `requirements.txt` for the versions of Python packages.

To generate an installable Windows MSI package (using the Gradle `msi` task), you will need the WiX toolset.

This project uses GitHub Actions for continuous integration
(https://github.com/EduMIPS64/edumips64/actions).

There are four main CI/CD workflows:

- **CI Build** (`ci.yml`) — runs on every pull request and on a daily schedule.
  Builds and tests the desktop application, builds the web application, runs the
  web UI tests, and builds/tests the Snap package. It runs with a read-only
  token and no access to secrets, so it can safely build code from forks.
  All CI checkouts use `fetch-depth: 0` so `git describe` works correctly.
- **PR preview deploy** (`pr-reports.yml`) — triggered when a CI Build run
  completes. It runs from the base branch (never checking out pull request
  code) and deploys the pre-built web application to the staging environment.
- **Release** (`release.yml`) — runs on every push to `master` to build all
  release artifacts (JAR, MSI, Electron apps). The `deploy-prod` job is
  disabled (`if: false`); production web deploys are now gated (see below).
  Can also be triggered manually to create a tagged GitHub release with all
  artifacts attached.
- **Nightly web deploy** (`nightly-web.yml`) — runs on a daily schedule at
  01:00 UTC (and can be triggered manually via `workflow_dispatch`). Finds the
  latest green `master` CI build and deploys its web artifact to
  `web.edumips.org/nightly/` (an ungated preview channel; shows a NIGHTLY
  badge). Does not touch the production root.

### Main Gradle tasks

All the tasks of Gradle
[Java](https://docs.gradle.org/current/userguide/java_plugin.html#sec:java_tasks) and
[Application](https://docs.gradle.org/current/userguide/application_plugin.html#sec:application_tasks)
plugins are available to build,
compile documentation, run tests and run EduMIPS64 itself.  
In particular you may find useful these tasks:

- `./gradlew assemble` - (Java plugin) compile and assemble jar artifacts
- `./gradlew check` - (Java plugin) run tests and compile the documentation
- `./gradlew run` - (Application plugin) run the application
- `./gradlew war` - (GWT plugin) compile the GWT-based web worker running the EduMIPS64 core
- `./gradlew webapp` - (Custom task) compile the GWT-based web worker, the React frontend, and bundle the documentation

You may also find useful using the `--console=plain` flag to better see what tasks
are being executed.  
Individual tasks for building single documentation (PDF and HTML) and jar targets
are available too: please read `build.gradle` for the complete list.  
Gradle builds the following jar artifacts:

- `edumips64-<build-version>.jar`: GUI executable jar (includes JavaHelp and picocli)

The `<build-version>` is the full git-describe build identity. At a release tag it
collapses to the plain version (e.g. `edumips64-1.4.1.jar`); on development builds
it includes the commit distance and hash (e.g. `edumips64-1.4.1-5-gabc1234.jar`).

Gradle is supported by all the main Java IDEs (e.g. IDEA, Eclipse, NetBeans).

For developers that don't want to recompile the help files when creating a JAR, the
`noHelpJar` Gradle task will produce `edumips64-<build-version>-nohelp.jar`, which does
not include the compiled help files.

#### Build output directory

All Gradle-produced artifacts are written under the `out/` directory at the
root of the repository, rather than Gradle's default `build/`. The layout is:

| Artifact | Location |
| --- | --- |
| JARs (main, nohelp) | `out/edumips64-<build-version>[-nohelp].jar` |
| MSI installer | `out/EduMIPS64-<version>.msi` |
| Electron packages | `out/WebEduMips64-<platform>-<arch>/` |
| Web application (worker, ui, docs) | `out/web/` |
| Documentation (HTML, PDF) | `out/docs/` |
| Test reports, caches, intermediates | `out/reports/`, `out/tmp/`, ... |

The `out/` directory is ignored by Git.

### Visual Studio Code

If you want to work on EduMIPS64 with Visual Studio Code, you need to download the Java Extension Pack
(see [Java in Visual Studio Code](https://code.visualstudio.com/docs/languages/java)).

With the Java Extension Pack, you can directly import the Gradle project and use auto-complete, run unit tests, etc.

### Working on the Web UI

An experimental web frontend, based on GWT and React, is being developed
right now.

#### Web Worker

The core of EduMIPS64 is cross-compiled to Javascript using GWT. It is meant to
run inside a web worker. The code for the worker is in the `org.edumips64.client`
package; of course, building the worker also requires building most of the rest
of the EduMIPS64 core. The GWT configuration is in the `webclient.gwt.xml` file.

The GWT code runs as a Web Worker to enable concurrency between UI interaction
and the execution of the simulation steps.

To compile it, run the `war` task, which will produce the file `worker.js` inside
the directory `out/web/`.

**NOTE:** the `assembleWebApp` gradle task copies the GWT compiler output into
`out/web`. If you re-build the worker, you need to re-build the rest of the web
UI as well to have a working local test environment (see next section).

#### Web UI

The web UI itself is based on React, and it's compiled / assembled using the NPM and
webpack tools. The source code is in `src/webapp`.

The `webapp` Gradle task automates the build process for the web UI. It:
1. Compiles the GWT worker (`war` task)
2. Generates the Web-flavored HTML documentation
   (`htmlDocsWebEn`, `htmlDocsWebIt`, `htmlDocsWebZh` tasks). These are
   driven by the same Sphinx sources as the desktop manual but with the
   `-t web` tag, so the user interface chapter that is included is
   `user-interface-web.rst` only — see "User manual structure" below.
3. Installs NPM dependencies (`npmInstall` task)
4. Builds the React frontend (`npmBuild` task)
5. Copies the generated documentation into the web app bundle (`copyWebHelp` task)

Custom NPM scripts:

- `build-dbg`: runs `webpack -d` (compile with debugging symbols)
- `build`: runs `webpack -p` (compile without debugging symbols, minified, etc)
- `start`: starts the webpack-dev-server with live reloading

Both `build` and `build-dbg` produce a `ui.js` file in the `out/web` directory.

The code was tested with Node.JS 16. The CI environment uses this version.

There are Playwright tests for the web UI, which can be run with `npm test`.

#### Build environment indicator

The header of the web UI shows a label that identifies which deployment of
the application is currently running:

- "Web Version" — production deployment at `web.edumips.org`.
- "Web Version" + a clickable "PR #N" chip — a per-PR preview build deployed
  to `https://edumips64ci.z16.web.core.windows.net/<PR_NUMBER>/` by the
  `deploy-staging` job in `.github/workflows/pr-reports.yml`. The chip links
  back to the originating pull request on GitHub.
- "Web Version (dev)" + a "dev" chip — any other host (local development,
  forks, ad-hoc deployments, etc.).

The same information is also surfaced in the "About" tab of the help dialog.
The classification logic lives in `src/webapp/buildInfo.js` and is driven
purely by `window.location`, so it does not require any build-time
configuration.

#### Web UI code coverage

The web UI tests can generate Istanbul code coverage data. CI publishes this
report on the GitHub Actions run summary — no third-party coverage service is
involved.

To run tests with coverage locally:

1. Build the web application with coverage instrumentation:
   ```
   BABEL_ENV=coverage npm run build-dbg
   ```
2. Start a local server serving `out/web` on port 8080 (e.g. `python3 -m http.server 8080 --directory out/web`).
3. Run the tests with coverage collection enabled:
   ```
   COVERAGE=true npm test
   ```
4. Generate the HTML/lcov report:
   ```
   npm run report:coverage
   ```

The coverage report is written to `coverage/lcov.info` (and `coverage/index.html` for the
HTML report). Both `.nyc_output/` and `coverage/` are excluded from version control.

In CI, the `test-web-coverage` job in `ci.yml` performs these steps
automatically. It renders the lcov report as a Markdown table using
`utils/lcov-summary.py` and appends it to the Actions run summary. The Java
side is reported the same way: the `build-desktop` job uses
`utils/jacoco-summary.py` to turn the JaCoCo XML produced by `./gradlew check`
into a Markdown table on the run summary. Both summaries are published with a
read-only token and require no repository secrets.

### Source code structure

The source code structure follows the [Gradle project layout conventions](https://docs.gradle.org/current/userguide/java_plugin.html#N152C8).
The main package for the simulator is `org.edumips64`, therefore the Java code
resides in `src/main/java/org/edumips64`, and contains 5 sub-packages, plus
the entry points.

`Main.java` is the code for the main Swing frontend entry point, while `MainCLI.java`
contains an experimental CLI front-end.

- The `client` package contains Java code for the Web UI.
- The `core` package contains all the core classes for the simulator, including
  important bits such as the CPU, the Memory, instructions and the Parser.
- The `img` package contains a class to load images and the actual images used
  in the simulator.
- The `ui` package contains the code for the Swing UI.
- The `utils` package contains miscellaneous code, including abstractions needed
  to decouple the core code from packages that are not available in the GWT
  JRE emulation (such as `java.io`).

### User manual structure

The user manual lives under `docs/user/<lang>/src/` (one Sphinx project per
language: `en`, `it`, `zh`). The sources are split into a UI-independent
part — `source-files-format.rst`, `instructions.rst`, `fpu.rst`,
`examples.rst` — and two UI-specific chapters:

- `user-interface-swing.rst` — desktop (Swing) UI, including the
  command line options of the JAR.
- `user-interface-web.rst` — web frontend.

Which UI chapter is included in a given build is controlled by Sphinx
tags via `.. only::` directives in `index.rst`. Three flavors are
generated by Gradle for each language:

| Sphinx tag | Output dir | UI chapters | Used by |
| --- | --- | --- | --- |
| (none) — "full" | `out/docs/<lang>/html/` | both | PDF, Read the Docs |
| `swing` | `out/docs/<lang>/html-swing/` | Swing only | desktop in-app help (JAR) |
| `web` | `out/docs/<lang>/html-web/` | Web only | web frontend in-app help |

Per-flavor Gradle tasks are `htmlDocs<Lang>` (full),
`htmlDocsSwing<Lang>` and `htmlDocsWeb<Lang>`. The `copyHelp` task
bundles the Swing-flavored HTML into the JAR (under
`docs/user/<lang>/html/`); the `copyWebHelp` task bundles the
Web-flavored HTML into `out/web/docs/<lang>/html/`.

Any user-facing change must be reflected in all three languages
(English, Italian, Chinese). When a change is specific to one user
interface, it should go into the matching `user-interface-<flavor>.rst`
file only.

### CLI banner and in-shell help browser

The interactive CLI shell (`--headless`) shows a stylised startup
banner and offers an in-shell user-manual browser. The implementation
lives in `src/main/java/org/edumips64/utils/cli/`:

- `Banner.java` renders the EduMIPS64 ASCII-art banner (figlet "ANSI
  Shadow" font, heavy Unicode block characters) with three
  logo-matching colours — gold "Edu", bright/white "MIPS", red "64" —
  via picocli's `Help.Ansi.AUTO` markup. The "Edu" segment combines
  the font's uppercase E with a hand-crafted lowercase d (whose right
  wall doubles as a tall ascender) and a lowercase u in the same
  heavy block style, so the rendering matches the mixed-case product
  name. Each segment is painted with a top-to-bottom three-shade
  gradient (bold light at the top, mid tone in the middle, deep tone
  at the bottom), which gives the heavy blocks a soft drop-shadow /
  3-D effect — the same trick used by the Hermes Agent CLI banner.
  The banner auto-degrades to a one-line version string when stdout
  is not a TTY, when `COLUMNS` is below the banner width, when the
  encoding is not UTF-8, or when the user passes `--no-banner`. The
  same `Banner.print(...)` is invoked by `Main` for both the Swing
  and CLI entry points, so launching `java -jar edumips64.jar` from a
  terminal also shows the banner.
- `Pager.java` is a tiny dependency-free "more"-style pager used by
  the help browser. It bypasses paging when `System.console()` is
  null, so piped/redirected runs stay clean.
- `HelpCommand.java` is the picocli subcommand backing
  `help` / `help topics` / `help <topic>`. The list of available
  topics is read from a hand-maintained index file
  (`src/main/resources/org/edumips64/help/topics/topics.index`) with
  three tab-separated columns: `<id> <TAB> <rst-filename> <TAB> <human title>`.
  The chapter content itself is **not** hand-curated: the Gradle
  `processResources` task copies `docs/user/en/src/*.rst` verbatim
  into the JAR under `org/edumips64/help/topics/`, so the chapters
  served by the shell are always exactly the ones that Sphinx renders
  for the website and PDF (the build is hermetic; the docs cannot
  drift apart).

To add a new help topic:

1. Make sure the chapter exists at `docs/user/en/src/<filename>.rst`
   (it does already if it is part of the manual).
2. Add a row to `topics.index`:
   `<id><TAB><filename>.rst<TAB><human title>`.
3. (Optional) Mention the new topic in the user manual under
   `cli-interface.rst`.

`BannerTest` verifies that the banner mentions `EduMIPS64` and the
current `MetaInfo.VERSION`. `HelpTopicsTest` verifies that every entry
in `topics.index` resolves to a non-empty resource bundled in the JAR
— if a row references a missing file, this test fails and protects
the hermetic-build contract.



We use the [GitHub Flow](http://scottchacon.com/2011/08/31/github-flow.html)
development workflow, which means that `master` is always fully working
(the code can be built and all tests pass), and development is done in separate
named branches. The good state of `master` is enforced by its protected
status, meaning that no commits can be pushed directly to `master` and any
pull requests for `master` have to pass the status checks (Github Actions building
the code and executing unit tests).

### Unit tests

It is expected that all new features are implemented with good unit tests coverage.

There is a suite of end-to-end tests, but any significant change to core classes
should ideally come with their own separate unit tests.

Unit tests are stored in the `src/test` directory. The `resources`
subdirectory contains MIPS64 programs that are executed during unit test as a
form of end-to-end unit tests, whereas `java` contains the actual Java code
that runs unit tests.

The main tests are contained in `EndToEndTests.java`. This class contains unit
tests that run MIPS64 code (contained in `resources`). One of the common
patterns in those tests is that, if something goes unexpectedly during the
execution of unit tests, the MIPS64 code executes a `BREAK` instruction, which
will trigger a `BreakException` in the Java code and make the test fail. Tests
in `CpuTests.java` can also verify other behaviors, including forwarding and
correct working of the Dinero Tracefile generation logic.

Other types of test, e.g., `ParserTest.java` or `MemoryTest.java`, will test
other components in isolation.

To add a unit test, the first consideration is whether this test should be
written in assembly or in Java. Tests in assembly should typically be put in
`CpuTests.java`, since it contains already boilerplate for executing and
verifying assembly programs. Tests which should not be written in assembly,
and therefore most likely exercise only one component, should pertain to other
classes, possibly even an entirely new class if required.

When writing new unit test classes, pay attention to the initialization code
necessary to initialize the simulator. Look at other unit test classes to make
sure your new class behaves as required.

The Swing UI code is explicitly excluded from code coverage reports because
writing tests for it is quite difficult and might not be worth it since we
might be migrating to a new shiny web-based frontend.

#### Instruction coverage check

Every instruction implemented under `src/main/java/org/edumips64/core/is/`
must be exercised by at least one `.s` program under `src/test/resources/`.
The `utils/find-instructions.sh` script enforces this: it lists every
non-abstract instruction class, collects every mnemonic referenced in the
assembly test files, and exits non-zero if there is any instruction that
is not used in at least one test.

The script runs in CI as part of the desktop build, so a pull request that
adds a new instruction without also adding a test that uses it will fail
the build. When you add a new instruction, make sure to add (or extend) a
`.s` test under `src/test/resources/` that uses it, and register the test
in `EndToEndTests.java`.

A small allow-list inside the script (`EXCLUDE_REGEX`) covers internal
pseudo-instructions that are not user-addressable and therefore cannot
appear in assembly source (for example `BUBBLE`, which the pipeline
inserts to represent stalls, and `DDIV3`, which is the Java class name
for the 3-operand form of the `DDIV` mnemonic). Only extend this list
for entries that are genuinely not reachable from MIPS64 assembly.

### Windows

EduMIPS64 compiles under Windows, both natively (e.g., using PowerShell) and in WSL.

### Mac OS X

The build works under Mac OS X (tested with Catalina 10.15.2, AdoptOpenJDK 11.0.7).

The only thing that might not work out of the box is downloading the Gradle GWT
plugins, as the Maven repo uses Let's Encrypt as a certificate issuer, which
is not trusted by default by the JDK.

Follow instructions [here](https://dev.cloudburo.net/2018/06/03/install-letsencrypt-certificate-in-the-java-jdk-keystore-on-osx.html) to import the Let's Encrypt root certificates in the JDK keystore.

### Versioning model

EduMIPS64 uses **two distinct version concepts**:

- **Release label** — `version` in `gradle.properties` (e.g. `1.4.1`). This is
  the human-visible release name. It is constant between releases and is used for
  git tags (`v1.4.1`), JAR filenames, and the MSI installer version. Bumped
  manually before each release.

- **Build identity** — `git describe --tags --match v* --always --dirty` with
  the leading `v` stripped. This uniquely identifies every commit:
  - At a release tag: identical to the release label (e.g. `1.4.1`).
  - Between releases: includes the commit distance and short SHA
    (e.g. `1.4.0-74-geec1768`).
  - With local uncommitted changes: appended `-dirty`.

The build identity is what appears in the Swing window title/status bar, the
CLI `--version` output, the web UI header, and the user manual `|version|`
substitution. It is derived automatically from git at build time — no manual
bookkeeping is required.

The Gradle provider is defined in `build.gradle.kts` (the `gitDescribe` val).
All CI checkouts must use `fetch-depth: 0` so the full tag history is
available for `git describe` to produce meaningful output.

### Web production promotion

Production (`web.edumips.org`) is **not auto-deployed**. The `deploy-prod` job
in `release.yml` is disabled (`if: false`). Promotion is manual and gated.

#### Promoting a build to production

**`promote-web.yml` serves two modes:**

**Mode 1: Build and promote current master (one-click ship)**
1. Open **Actions → Promote Web to Production** (`promote-web.yml`).
2. Click **Run workflow**. Leave the **CI run ID** field **empty**.
3. The workflow builds and tests the current `master` (via reusable `build-web.yml`, 
   no secrets), then deploys the artifact to production.
4. The Pages repo (`EduMIPS64/web.edumips.org`) receives the update with the following layout:

   ```
   web.edumips.org/
   ├── index.html + [all files]   ← current promoted version (root)
   ├── prev/                      ← previous promoted version
   ├── v/
   │   ├── 1/  2/  3/ …          ← immutable snapshots (up to 50 kept)
   └── manifest.json              ← {current, prev, sha, build, promotedAt, …}
   ```

**Mode 2: Promote a specific past CI run (rollback / re-promote)**
1. Open **Actions → Promote Web to Production** (`promote-web.yml`).
2. Click **Run workflow** and enter the **CI run ID** whose `web` artifact to ship.
   (Find the run ID in the URL of any successful CI run on `master`.)
3. The workflow validates the run (must be a successful `ci.yml` run on `master`
   with a `web` artifact), then deploys it to production using the same layout as Mode 1.
   This mode is useful for: rolling back to a previous good build (find its run ID and
   re-promote), re-promoting the current production version by id, or auditing which
   runs have shipped.

**Details:**
- Only `lupino3` (Andrea) can trigger `promote-web.yml` (actor check enforced in
  the workflow, in addition to the collaborator gate on `workflow_dispatch`).
- Must be dispatched from the `master` branch in both modes.
- The Pages-layout logic lives in `.github/scripts/deploy-web-pages.py`.

#### Rolling back

If the current production version is broken, open **Actions → Rollback Web
Production** (`rollback-web.yml`) and click **Run workflow** (no inputs needed).
This promotes `prev/` back to root and updates `manifest.json`. Gated to
`lupino3` only.

Alternatively, use Mode 2 of `promote-web.yml` to re-promote a previous good build by its run ID.

#### Nightly channel

`nightly-web.yml` runs on a daily schedule at 01:00 UTC (and can be triggered
manually via `workflow_dispatch`). It finds the latest successful CI run on
`master` and deploys that build to `web.edumips.org/nightly/`. This is an
ungated preview; the build shows a **NIGHTLY** badge so users know it may be
unstable. Nightly never touches the production root, `prev/`, or any `v/N/`
snapshot.

### Manual release checklist

Most of the release process is automated via the `release.yml` GitHub Actions workflow.
To create a release, trigger the workflow manually from the Actions tab with `create_release: true`.
You can optionally provide a specific `commit_sha` to build and release from a particular commit
(defaults to the latest commit on the selected branch if left empty).
This will build all artifacts (JAR, PDF manuals, MSI, Electron apps), create a Git tag (`vX.Y.Z`),
and publish a GitHub release with all assets attached. The release body comes from `RELEASE_NOTES.md`.

Before triggering a release:

- Bump the version in `gradle.properties`
- Update `RELEASE_NOTES.md` with notes for the new version
- Update the version in `snapcraft.yaml`
- Merge all changes to `master`
- **Smoke-test the release artifacts produced by the automatic `release.yml`
  run for the merge commit to `master` _before_ triggering the manual run.**
  The same workflow runs on every push to `master` (without `create_release`),
  so the JAR, MSI, Electron apps and PDF manuals for the exact commit you are
  about to release are already available as workflow-run artifacts. Download
  them from the most recent automatic `Release` run on `master`, run through
  the verification checklist below, and only kick off the manual release once
  the artifacts pass. This avoids cutting a tag for a broken build.

After the automated release completes (or against the artifacts of the
automatic pre-release run on `master`, see above), manually verify:

- JAR and MSI:
  - verify that the splash screen works
  - verify that the version number, code name, build date and git ID are correct
  - open one .s file (e.g., `div.d.s`)
  - run it
  - open the help
  - close the application
- JAR-only: verify the JAR size (should be < 3 MB)
- Electron apps (Linux / macOS / Windows): launch each one, confirm the web UI
  loads (no `Cannot GET /` from the embedded express server) and run a sample program
- PDF:
  - open the English manual and check the version
  - open the Italian manual and check the version
  - open the Chinese manual and check the version

Trigger builds on snapcraft.

Check the 'edge' snap and promote it to stable if it works (https://snapcraft.io/edumips64/releases, needs login)
Test both on amd64 and armhf (Raspberry Pi)

Update winget manifest on https://github.com/microsoft/winget-pkgs/tree/master/manifests/e/EduMIPS64/EduMIPS64
