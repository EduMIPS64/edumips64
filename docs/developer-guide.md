### Table of Contents

[Requirements](#requirements)

[Main Gradle tasks](#main-gradle-tasks)

[Working on the Web UI](#working-on-the-web-ui)

[Source code structure](#source-code-structure)

[Submitting code](#submitting-code)

[Unit tests](#unit-tests)

[Compiling under Windows](#windows)

[Compiling under Mac OSX](#mac-os-x)

### Requirements

In order to compile EduMIPS64, you need the Java JDK version 17 or above.

To build the user documentation, you'll need Python 3 with pip.

[Gradle](https://gradle.org/) will download the following dependencies:

- JUnit
- JavaHelp
- GWT (experimental)
- Python dependencies to build the documentation (they'll be installed in a virtual environment)
  - Sphinx (http://www.sphinx-doc.org/) version 3.4.3 or above
  - rst2pdf (for the PDF files) version 0.98 or above

To generate an installable Windows MSI package (using the Gradle `msi` task), you will need the WiX toolset.

This project uses GitHub Actions for continuous integration
(https://github.com/EduMIPS64/edumips64/actions).

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

You may also find useful using the `--console=plain` flag to better see what tasks
are being executed.  
Individual tasks for building single documentation (PDF and HTML) and jar targets
are available too: please read `build.gradle` for the complete list.  
Gradle builds the following jar artifacts:

- `edumips64-<version>.jar`: GUI executable jar (includes JavaHelp and picocli)

Gradle is supported by all the main Java IDEs (e.g. IDEA, Eclipse, NetBeans).

For developers that don't want to recompile the help files when creating a JAR, the
`noHelpJar` Gradle task will produce `edumips64-<version>-nohelp.jar`, which does
not include the compiled help files.

### Visual Studio Code

If you want to work on EduMIPS64 with Visual Studio Code, you need to download the Java Extension Pack
(see [Java in Visual Studio Code](https://code.visualstudio.com/docs/languages/java)).

To make it recognize the EduMIPS64 folder as a project, run `./gradlew eclipse` to generate
Eclipse-style project files, which are readable by the VSCode plugins.

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
the directory `build/gwt/war/edumips64/`.

**NOTE:** the `war` gradle task wipes the `build/gwt/war/edumips64` directory.
So if you re-build the worker, you need to re-build the rest of the web UI as well
to have a working local test environment (see next section).

#### Web UI

The web UI itself is based on React, and it's compiled / assembled using the NPM and
webpack tools. The source code is in `src/webapp`.

Custom NPM scripts:

- `build-dbg`: runs `webpack -d` (compile with debugging symbols)
- `build`: runs `webpack -p` (compile without debugging symbols, minified, etc)
- `start`: starts the webpack-dev-server with live reloading

Both `build` and `build-dbg` produce a `ui.js` file in the `build/gwt/war/edumips64` directory.

The code was tested with Node.JS 16. The CI environment uses this version.

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

### Submitting code

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
writte in assembly or in Java. Tests in assembly should typically be put in
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

### Windows

EduMIPS64 compiles under Windows, both natively (e.g., using PowerShell) and in WSL.

### Mac OS X

The build works under Mac OS X (tested with Catalina 10.15.2, AdoptOpenJDK 11.0.7).

The only thing that might not work out of the box is downloading the Gradle GWT
plugins, as the Maven repo uses Let's Encrypt as a certificate issuer, which
is not trusted by default by the JDK.

Follow instructions [here](https://dev.cloudburo.net/2018/06/03/install-letsencrypt-certificate-in-the-java-jdk-keystore-on-osx.html) to import the Let's Encrypt root certificates in the JDK keystore.

### Manual release checklist

Before doing a release, please do the following tasks. Over time, those should
be automated, but before that is done those checks should be done manually.

- For each released JAR / MSI file:
  - verify that the splash screen works
  - verify that the version number, code name, build date and git ID are correct
  - open one .s file (e.g., `div.d.s`)
  - run it
  - open the help
  - close the application
  - verify the JAR size (should be < 3 MB)
- open the English manual and check the version
- open the Italian manual and check the version
- check the 'edge' snap and promote it to stable if it works (https://snapcraft.io/edumips64/releases, needs login)