/*
    @Author: Dylan Tonks
    @Created Date: 19/05/2018

    This file invokes inter-process communication, to
    allow communication from the renderer process, to
    the Java server.

    It should be noted that this is ran in the main process.
*/

//Requires
var electron = require('electron')
var {app, BrowserWindow, ipcMain} = electron
var net = require('net')

//This stores in order, the Windows which have made requests.
var requests = []

//Connect to the Java server
//TODO: need to implement client.on('end', ...)
var client = net.connect({host: "127.0.0.1", port:5000}, function() {
    console.log('Connection established!');
})


/*
    When there is incoming data, forward it
    to the next in line waiting Window.
*/
client.on('data', function(data) {

    //Get the 'next in line'
    var sender = requests.shift()
    console.log(data.toString())
    //Send it through the 'reply' channel, to the renderer process
    sender.send('reply', data.toString())
})


/*
    When the renderer process requests
    data to be sent to the Java server...
*/
ipcMain.on('send', (event, arg) => {
    client.write(arg)
    console.log("SEND: " + arg)
    //Add the sender to the 'next in line' priority queue.
    requests.push(event.sender)
})

