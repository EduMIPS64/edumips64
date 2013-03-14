#!/bin/bash
#
# Quick and dirty launcher script for edumips64 with external JavaHelp JAR
# file. This needs to be personalized by packagers. It assumes Debian's
# locations for Javahelp now, and that it is being run from the source code
# top-level directory.
#
# Author: Andrea Spadaccini <andrea.spadaccini@gmail.com>

JH_JAR=/usr/share/java/jhbasic.jar
VERSION=1.1
EDUMIPS_JAR=edumips64-${VERSION}-nodeps.jar
SPLASH_IMAGE=src/org/edumips64/img/splash.png

if [ ! -f ${EDUMIPS_JAR} ]; then
    echo "The EduMIPS64 JAR file (${EDUMIPS_JAR}) could not be found."
    exit 1
fi

if [ ! -f ${JH_JAR} ]; then
    echo "The JavaHelp JAR file (${JH_JAR}) could not be found."
    exit 1
fi

java -splash:${SPLASH_IMAGE} -cp ${EDUMIPS_JAR}:${JH_JAR} org.edumips64.Main
