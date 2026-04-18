#!/bin/sh

$SNAP/usr/lib/jvm/java-17-openjdk-$SNAP_ARCH/bin/java -Dfile.encoding=UTF8 -Djava.util.prefs.userRoot="$SNAP_USER_DATA" -jar $SNAP/jar/edumips64.jar $*
