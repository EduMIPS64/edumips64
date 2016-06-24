### Table of Contents
[Requirements](#requirements)

[Main Ant targets](#main-ant-targets)

[Working on the GWT frontend](#working-on-the-gwt-frontend)

[Source code structure](#source-code-structure)

[Submitting code](#submitting-code)

### Requirements

In order to compile EduMIPS64, you need the following tools:
- Java JDK version 8 or above.
- Apache Ant version 1.8 or above (needed for JUnit 4 tests)
- Gradle

To build the user documentation, you'll need:
- GNU Make
- Sphinx (http://sphinx.pocoo.org/) version 1.0.7 or above
- latex / pdflatex

Gradle will help you download the following dependencies:
- JUnit
- JavaHelp
- GWT (experimental)

To download the dependencies (Javahelp, JUnit), use the `gradle getLibs`
command. This is a necessary step before doing any development work.

If you want to use the automatic style checks (pre-commit hook), then you
should have Automatic Style (astyle) installed.

This project uses Travis CI for continuous integration
(https://travis-ci.org/lupino3/edumips64).

### Main Ant targets

The default action is `slim-jar`.

* `slim-jar` builds the jar package named edumips64-`version`-nodeps.jar, that
  does not embed the JavaHelp libraries and is oriented towards distribution
  package creators, that should add a dependency on JavaHelp and appropriately
  set the classpath in their scripts.

* `test` runs unit tests;

* `standalone-jar` builds the jar package named edumips64-`version`.jar, that
  embeds the JavaHelp libraries, and is oriented towards users downloading the
  JAR archive from the website (not through package managers).

* `clean` removes the jar files, the build directory and the compiled
  documentation

* `docs` builds the user documentation (both in-app HTML and PDF)

* `cli-jar` build a jar package containing an experimental CLI front-end

* `src-release` builds a tar.bz2 file containing the source distribution

* `javadoc` builds the javadoc documentation; will store it in the `javadoc`
   directory.

* `devmode` and `gwtc` are related to the GWT frontend, see below.

### Working on the GWT frontend

An experimental web frontend, based on GWT, is being developed right now.
Currently, only a prototype is available. The GWT code for it is in the
`org.edumips64.client` package. The HTML file is in `contrib/edumips64.html`.

To work on it, run the `ant devmode` ANT target, which will fire up the GWT
developer console for you. Once the console is available, you'll be given a
local URL where the frontend will be available.

Every time you change the GWT frontend, reloading that web page will cause the
GWT console to recompile the code, thus allowing quick iteration on the web
frontend code.

To create a releasable version of the JS code, use the `gwtc` target. The
compiled code (HTML + JS) will be stored in the `war` directory.

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

Code should be submitted as pull requests. The `master` branch is protected,
meaning that pull requests can be merged only if they pass the status checks.
Currently, the only status check is the Travis CI continuous integration.

If this proves to be too inconvenient, it might be better to split out a
protected `stable` branch to use for releases and have `master` unprotected.
We'll see how this fares as more people start contributing to the project.