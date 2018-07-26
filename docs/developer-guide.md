### Table of Contents
[Requirements](#requirements)

[Main Bazel targets](#main-bazel-targets)

[Working on the GWT frontend](#working-on-the-gwt-frontend)

[Source code structure](#source-code-structure)

[Submitting code](#submitting-code)

[Unit tests](#unit-tests)

[Bazel](#bazel)

### Requirements

In order to compile EduMIPS64, you need the following tools:
- Java JDK version 8 or above.
- Apache Ant version 1.8 or above (needed for JUnit 4 tests)
- Bazel (http://bazel.io)

To build the user documentation, you'll need:
- GNU Make
- Sphinx (http://sphinx.pocoo.org/) version 1.0.7 or above
- latex / pdflatex

Bazel will download the following dependencies:
- JUnit
- JavaHelp
- GWT (experimental)

If you want to use the automatic style checks (pre-commit hook), then you
should have Automatic Style (astyle) installed.

This project uses Travis CI for continuous integration
(https://travis-ci.org/lupino3/edumips64).

### Main Bazel targets

Note that you will need to specify the command to use to generate build-time
variables. This is done by appending the following parameters to all the
`bazel build` commands: `--workspace_status_command=utils/bazel-stamp.sh --stamp`.

* `//src/main/java/org/edumips64:slim-jar` builds the jar package named
  edumips64-`version`-nodeps.jar, that does not embed the JavaHelp libraries
  and is oriented towards distribution package creators, that should add a
  dependency on JavaHelp and appropriately set the classpath in their scripts.

* `//src/test/java/org/edumips64/:all` runs all unit tests (to be ran with
  `bazel test`);

* `//src/main/java/org/edumips64:standalone-jar` builds the jar package named
  edumips64-`version`.jar, that embeds the JavaHelp libraries, and is oriented
  towards users downloading the JAR archive from the website (not through
  package managers).
  
* `//src/main/java/org/edumips64:cli-jar` builds a jar package containing an
  experimental CLI front-end.

* `//docs/user/en:pdf` builds the English PDF docs.

* `//docs/user/en:html` builds the English HTML docs.

* `//docs/user/it:pdf` builds the English PDF docs.

* `//docs/user/it:html` builds the English HTML docs.

* `//src/main/java/org/edumips64:{devmode,gwtc}` are related to the GWT frontend,
  see below.

### Working on the GWT frontend

An experimental web frontend, based on GWT, is being developed right now.
Currently, only a prototype is available. The GWT code for it is in the
`org.edumips64.client` package. The HTML file is at
`src/main/java/org/edumips64/client/edumips64.html`.

To work on it, run the `devmode` Build target, which will fire up the GWT
developer console for you. Once the console is available, you'll be given a
local URL where the frontend will be available.

Every time you change the GWT frontend, reloading that web page will cause the
GWT console to recompile the code, thus allowing quick iteration on the web
frontend code.

To create a releasable version of the JS code, use the `gwtc` target.

### Source code structure

The source code structure follows the [Gradle project layout conventions](https://docs.gradle.org/current/userguide/java_plugin.html#N152C8).
The main package for the simulator is `org.edumips64`, therefore the Java code
resides in `src/main/java/org/edumips64`, and contains 5 sub-packages, plus
the entry points.

`Main.java` is the code for the main Swing frontend entry point, while `MainCLI.java`
contains an experimental CLI front-end.

* The `client` package contains Java code for the Web UI. 
* The `core` package contains all the core classes for the simulator, including
  important bits such as the CPU, the Memory, instructions and the Parser.
* The `img` package contains a class to load images and the actual images used
  in the simulator.
* The `ui` package contains the code for the Swing UI.
* The `utils` package contains miscellaneous code, including abstractions needed
  to decouple the core code from packages that are not available in the GWT
  JRE emulation (such as `java.io`).

### Submitting code

We use the [GitHub Flow](http://scottchacon.com/2011/08/31/github-flow.html)
development workflow, which means that `master` is always fully working
(the code can be built and all tests pass), and development is done in separate
named branches. The good state of `master` is enforced by its protected
status, meaning that no commits can be pushed directly to `master` and any
pull requests for `master` have to pass the status checks (Travis CI building
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
tests that run MIPS64 code (contained in `resources`).  One of the common
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
