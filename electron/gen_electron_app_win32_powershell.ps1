#$arch = "arm64"
$arch = "x64"
Remove-Item -Recurse -Force ./dist/WebEduMips64-darwin-$arch
Copy-Item index.js ..
npx electron-packager ../ WebEdumips64 --platform=win32 --arch=$arch --out dist
Remove-Item ../index.js
