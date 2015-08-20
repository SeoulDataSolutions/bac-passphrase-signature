#!/bin/sh
CP="lib/*:classes"
SP=src/

/bin/rm -f bac.jar
/bin/rm -rf classes
/bin/mkdir -p classes/

javac -encoding utf8 -sourcepath "${SP}" -classpath "${CP}" -d classes/ src/*.java || exit 1

echo "bac compiled successfully"

jar cf bac.jar -C classes . || exit 1
/bin/rm -rf classes

echo "bac.jar generated successfully"
