#!/bin/bash
#
# Quick and dirty launcher script for edumips64 with external JavaHelp JAR
# file. This needs to be personalized by packagers. It assumes Debian's
# locations for Javahelp now, and that edumips64-${VERSION}.jar is in the
# current directory.
#
# Author: Andrea Spadaccini <andrea.spadaccini@gmail.com>

JH_JAR=/usr/share/java/jhbasic.jar
VERSION=1.0
EDUMIPS_JAR=edumips64-${VERSION}-nodeps.jar

if [ ! -f ${EDUMIPS_JAR} ]; then
    echo "The EduMIPS64 JAR file (${EDUMIPS_JAR}) could not be found."
    exit 1
fi

if [ ! -f ${JH_JAR} ]; then
    echo "The JavaHelp JAR file (${JH_JAR}) could not be found."
    exit 1
fi

# TODO(andrea): use the splash image specified in the jar manifest.
java -cp ${EDUMIPS_JAR}:${JH_JAR} edumips64.Main
