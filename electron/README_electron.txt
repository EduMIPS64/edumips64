The details of the approach:
-----------------------------------

Testing app manually with command "npm run electron":

Sequence of events:

- Electron is started from a script defined in package.json
- Electron runs electron_main.js (the entry point as defined in package.json).
- electron_main.js requires and launches express_server.js.
- express_server.js sets up an Express server on localhost:8081 (configurable port).
- electron_main.js creates the Electron window and calls BrowserWindow.loadURL("http://localhost:8081").
- Electron’s internal browser requests index.html from localhost:8081
- The Express server responds with index.html.
- index.html references your compiled front-end bundle (for example, <script src="ui.js">).
- Electron requests ui.js from localhost:8081/ui.js. and Express serves the file
- ui.js executes in the renderer process, initializing the UI or React/Vue/etc.

The user interacts with the resulting UI within the Electron window, all running locally with no external internet requirement.


Creating Desktop App Executable
------------------------------------

From edumips64 root dir: 

npm run build

Then, just run one of these from the electron dir:

./gen_electron_app_macos.sh
./gen_electron_app_linux.sh
./gen_electron_app_win32_SHELLVERSION (3 versions)

an clickable app will be created at: electron/dist/WebEduMips64-PLATFORM-ARCH


Running the Built Desktop App
------------------------------------

After building the desktop app using the scripts above, you can run the app by navigating to the created directory and launching the executable.

Linux: 
./dist/WebEduMips64-linux-x64/WebEdumips64

macOS:
./dist/WebEduMips64-darwin-arm64/WebEduMips64.app/Contents/MacOS/WebEduMips64

Note for macOS users: Due to macOS security restrictions, you may need to remove the quarantine attribute from downloaded applications before they can be run. If you encounter a security warning when trying to run the app, execute the following command in Terminal:

xattr -dr com.apple.quarantine /path/to/WebEduMips64.app

Replace "/path/to/WebEduMips64.app" with the actual path to your WebEduMips64.app file.

Windows:
.\dist\WebEdumips64-win32-x64\WebEdumips64.exe

TODO:
- appropriate icons for the app

