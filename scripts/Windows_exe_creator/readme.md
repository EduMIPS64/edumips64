Script that runs on windows and outputs the installer on the "Output" folder

For the moment the standalone jar need to be placed on this folder;
The current working name is edumips.jar.
Jar files with other names work,however the conf.xml file "jar" tag needs to be updated with the correct name.
(future versions may create the standalone, copy it to the folder and update the conf.xml)

Current version bundles the jre with the program. One can be made that relies on jre installed on the user machine.

This script uses [launch4j](http://launch4j.sourceforge.net/) to create an executable with a bundled jre and [Ino Setup](https://jrsoftware.org/isinfo.php) to create a installer setup.
