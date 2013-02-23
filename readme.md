EduMIPS64 README
================

EduMIPS64 is a cross-platform visual MIPS64 CPU simulator written in
Java/swing. 

The EduMIPS64 web site is http://www.edumips.org. There is also a 
developers' blog at http://edumips64.blogspot.com.

To learn how it works and how to use it, please refer to the
EduMIPS64 User Manual (in the docs/{en,it}/output/latex directory). This file
is a short guide for developers who are interested in compiling the
simulator from scratch and/or modifying its source code.

Requirements
------------

In order to compile EduMIPS64, you need the following tools:
- Java JDK version 6 or above. 
- Apache Ant version 1.8 or above (needed for JUnit 4 tests)
- Sphinx (http://sphinx.pocoo.org/) versione 1.0.7 or above
- GNU Make

The two latter dependencies are needed for the in-application help.

If you need to compile the PDF manual, you will also need:
- latex / pdflatex

A distribution of JUnit 4 is included in the libs/ directory, since it is
needed for unit test and this project uses Travis CI for continuous
integration (https://travis-ci.org/lupino3/edumips64).

Main Ant targets
----------------

* `standalone-jar` builds the jar package named edumips64-`version`.jar, that
  embeds the JavaHelp libraries, and is oriented towards users downloading the
  JAR archive from the website (not through package managers).

* `slim-jar` builds the jar package named edumips64-`version`-nodeps.jar, that
  does not embed the JavaHelp libraries and is oriented towards distribution
  package creators, that should add a dependency on JavaHelp and appropriately
  set the classpath in their scripts.

* `docs` builds the user documentation (both in-app HTML and PDF)

* `clean` removes the jar files, the build directory and the compiled
  documentation

* `test` runs unit tests; it depends on junit4.jar being added to the
  classpath

* `cli-jar` build a jar package containing an experimental CLI front-end

* `src-release` builds a tar.bz2 file containing the source distribution

* `javadoc` builds the javadoc documentation (depends on junit4.jar being
  in CLASSPATH); will store it in the `javadoc` directory.

Compilation options
-------------------

Set those variables to modify some compile-time options:

* src_java_version (default: 6): Java version of source code;

* dst_java_version (default: 6): bytecode target version;

* debug (default: off): whether to compile with debugging info;

To set any variable, use the -D option of ant.
