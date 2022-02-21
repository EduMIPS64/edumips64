#!/bin/sh

/usr/lib/jvm/java-17-openjdk-amd64/bin/java -jar -Djava.util.prefs.userRoot="$SNAP_USER_DATA" $SNAP/jar/edumips64-1.2.9.jar
