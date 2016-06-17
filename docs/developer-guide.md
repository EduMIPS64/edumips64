
Requirements
------------

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

Main Ant targets
----------------

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
