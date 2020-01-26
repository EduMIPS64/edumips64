### Table of Contents
[Requirements](#requirements)

[Main Gradle tasks](#main-gradle-tasks)

[Working on the GWT frontend](#working-on-the-gwt-frontend)

[Source code structure](#source-code-structure)

[Submitting code](#submitting-code)

[Unit tests](#unit-tests)

[Compiling under Windoes](#windows)

### Requirements

In order to compile EduMIPS64, you need the Java JDK version 8 or above.

To build the user documentation, you'll need:
- GNU Make
- Python 3
- Sphinx (http://www.sphinx-doc.org/) version 2.3.1 or above
- rst2pdf (for the PDF files) version 0.9.6 or above

You can install the Python dependencies using PIP:

```
$ pip3 install -r docs/requirements.txt
```

[Gradle](https://gradle.org/) will download the following dependencies:
- JUnit
- JavaHelp
- GWT (experimental)

If you want to use the automatic style checks (pre-commit hook), then you
should have Automatic Style (astyle) installed.

This project uses Travis CI for continuous integration
(https://travis-ci.org/lupino3/edumips64).

### Main Gradle tasks

All the tasks of Gradle 
[Java](https://docs.gradle.org/current/userguide/java_plugin.html#sec:java_tasks) and 
[Application](https://docs.gradle.org/current/userguide/application_plugin.html#sec:application_tasks) 
plugins are available to build,
compile documentation, run tests and run EduMIPS64 itself.  
In particular you may find useful these tasks:

 * `./gradlew assemble` - (Java plugin) compile and assemble jar artifacts 
 * `./gradlew check` - (Java plugin) run tests and compile the documentation
 * `./gradlew run` - (Application plugin) run the application

You may also find useful using the `--console=plain` flag to better see what tasks 
are being executed.  
Individual tasks for building single documentation (PDF and HTML) and jar targets 
are available too: please read `build.gradle` for the complete list.  
Gradle builds the following jar artifacts:

 - `edumips64-<version>-standalone.jar`: GUI executable jar including the JavaHelp dependency
 - `edumips64-<version>.jar`: GUI executable jar
 - `edumips64-<version>-cli.jar`: CLI executable jar 

Gradle is supported by all the main Java IDEs (e.g. IDEA, Eclipse, NetBeans).


### Visual Studio Code

If you want to work on EduMIPS64 with Visual Studio Code, you need to download the Java Extension Pack
(see [Java in Visual Studio Code](https://code.visualstudio.com/docs/languages/java)).

To make it recognize the EduMIPS64 folder as a project, run `./gradlew eclipse` to generate
Eclipse-style project files, which are readable by the VSCode plugins.

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
pull requests for `master` have to pass the status checks (Azure Pipelines building
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

### Windows

Compilation under Windows is possible by using the Windows Subsystem for Linux
(WSL), exactly in the same way as you would do under Linux.
