const {app, BrowserWindow} = require('electron')
const url = require('url')
const path = require('path')

//require('electron-reload')(__dirname)

global.win = {window : null}

function createWindow() {
    win.window = new BrowserWindow({
        name:"RawM",
        width:650,
        height:550,
        toolbar: false,
        frame: true,
        
    });

   win.window.loadURL(url.format ({
      pathname: path.join(__dirname, './src/renderer/login.html'),
      protocol: 'file:',
      slashes: true
   }))
   win.window.webContents.openDevTools();
}

const connector = require('./src/main/connector')
app.on('ready', createWindow)