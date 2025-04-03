The details of the approach:
-----------------------------------

Launching app manually with command "npm run electron":

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
WORK IN PROGRESS: macosx only at the moment

Just run:
./gen_electron_app_macos.sh

an clickable app will be created at: dist/WebEduMips64-darwin-arm64

TODO:
- linux, windows versions of the script (should be trivial)
- appropriate icons for the app






