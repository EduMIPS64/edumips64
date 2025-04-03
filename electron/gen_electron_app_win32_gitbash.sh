rm -rf ./dist/WebEduMips64-darwin-arm64
cp index.js ..
npx electron-packager ../ WebEdumips64 --platform=win32 --arch=x64 --out dist
rm ../index.js
