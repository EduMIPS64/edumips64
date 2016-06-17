# Requirements

In order to compile EduMIPS64, you need the following tools:
- Java JDK version 7 or above.
- Apache Ant version 1.8 or above (needed for JUnit 4 tests)
- Gradle

To build the user documentation, you'll need:
- GNU Make
- Sphinx (http://sphinx.pocoo.org/) versione 1.0.7 or above
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

# Main Ant targets

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

# Working on the GWT frontend

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

# A note on the Cloud9 web IDE

[Cloud9](c9.io) is a nice Web IDE that provides GitHub integration and a Linux
container that can be used for development.

The Cloud9 IDE can be used for development of EduMIPS64, but its lack of X
Server means that it is not possible to:

 * run the Swing UI JAR;
 * use GWT's devmode;

To test GWT changes, use the `ant gwtc` target, and then right-click on
`war/edumips64.html` in the Workspace panel and then choose "Preview".

It should be possible to test the Swing UI by doing the same with
`utils/test-applet.html`.
