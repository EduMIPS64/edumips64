const { app, BrowserWindow } = require('electron');
const path = require('path');
let mainWindow;

function createWindow() {
  mainWindow = new BrowserWindow({
    width: 800,
    height: 600
  });

  // Se hai una webapp che gira su un server locale (npm start),
  // qui potresti puntare all'URL http://localhost:8080
  // Oppure, se vuoi tutto offline, punta direttamente ai file HTML
   mainWindow.loadURL('http://localhost:8080');
  // mainWindow.loadURL(`file://${path.join(__dirname, 'src','webapp', 'static', 'index.html')}`);
  
  mainWindow.on('closed', function () {
    mainWindow = null;
  });
}

app.on('ready', createWindow);
app.on('window-all-closed', function () {
  if (process.platform !== 'darwin') app.quit();
});
app.on('activate', function () {
  if (mainWindow === null) createWindow();
});
