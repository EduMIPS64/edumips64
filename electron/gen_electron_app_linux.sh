rm -rf ./dist/WebEduMips64-linux-x64
cp index.js ..
npx electron-packager ../ WebEdumips64 --platform=linux --arch=x64 --out dist
rm ../index.js
