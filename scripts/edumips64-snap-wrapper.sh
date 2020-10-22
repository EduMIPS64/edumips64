#!/bin/sh

java -jar -Djava.util.prefs.userRoot="$SNAP_USER_DATA" $SNAP/jar/edumips64-1.2.8.jar
