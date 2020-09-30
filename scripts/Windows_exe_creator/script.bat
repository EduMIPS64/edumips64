curl.exe -LO https://kumisystems.dl.sourceforge.net/project/launch4j/launch4j-3/3.12/launch4j-3.12-win32.zip
tar -xf launch4j-3.12-win32.zip 
copy *.jar launch4j
copy *.xml launch4j
cd launch4j
launch4jc.exe conf.xml
move edumips.exe ..\
cd ..
tar -xf jre.zip
mkdir a
move jre a
tar -xf iscc.zip
iscc "conf.iss"