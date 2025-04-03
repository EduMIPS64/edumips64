#npx electron-packager . MyApp --platform=linux --arch=x64 --icon=path/to/myicon.png --out dist
rm -rf ./dist/WebEduMips64-darwin-arm64
cp index.js ..
npx electron-packager ../ WebEdumips64 --platform=linux --arch=x64 --out dist
rm ../index.js
