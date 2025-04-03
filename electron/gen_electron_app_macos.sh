rm -rf ./dist/WebEduMips64-darwin-arm64
cp index.js ..
npx electron-packager ../ WebEduMips64 --platform=darwin --arch=arm64 --out dist
rm ../index.js
