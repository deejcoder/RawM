const {app, BrowserWindow} = require('electron')
const url = require('url')
const path = require('path')

require('electron-reload')(__dirname)

let win

function createWindow() {
   win = new BrowserWindow({width: 800, height: 600})
   win.loadURL(url.format ({
      pathname: path.join(__dirname, './src/renderer/login.html'),
      protocol: 'file:',
      slashes: true
   }))
}

const connector = require('./src/main/connector')
app.on('ready', createWindow)