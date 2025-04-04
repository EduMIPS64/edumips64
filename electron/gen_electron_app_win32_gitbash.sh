$arch="x64"
rm -rf ./dist/WebEduMips64-win32-$arch
cp index.js ..
npx electron-packager ../ WebEdumips64 --platform=win32 --arch=$arch --out dist
rm ../index.js
