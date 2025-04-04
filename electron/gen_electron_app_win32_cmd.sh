$arch="x64"
#$arch="arm64"
rmdir /s /q dist\WebEduMips64-win32-$arch
copy index.js ..
npx electron-packager .. WebEdumips64 --platform=win32 --arch=$arch --out dist
del ..\index.js
