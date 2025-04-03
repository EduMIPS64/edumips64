Remove-Item -Recurse -Force ./dist/WebEduMips64-darwin-arm64
Copy-Item index.js ..
npx electron-packager ../ WebEdumips64 --platform=win32 --arch=x64 --out dist
Remove-Item ../index.js
