rmdir /s /q dist\WebEduMips64-darwin-arm64
copy index.js ..
npx electron-packager .. WebEdumips64 --platform=win32 --arch=x64 --out dist
del ..\index.js
