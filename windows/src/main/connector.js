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
var remote = electron.remote
var {app, BrowserWindow, ipcMain} = electron
var net = require('net')

//This stores in order, the Windows which have made requests.
var requests = [] //this doesn't work, since responses can come from server without requests!
var webcontents

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
    /*if(requests.length() == 0) {
        //TODO: need to route request types to the ideal window (i.e message = index)
    }*/
    //var sender = requests.shift()
    var sender = webcontents
    data = data.toString().split("\r\n")
    console.log(data)
    for(line in data) {
        if(data[line].length > 0 && data != undefined) {
            console.log("DATA" + data[line])
            sender.send('reply', data[line])
        }
    }
    
    //Send it through the 'reply' channel, to the renderer process
    
})


/*
    When the renderer process requests
    data to be sent to the Java server...
*/
ipcMain.on('send', (event, arg) => {
    client.write(arg)
    console.log("SEND: " + arg)
    //Add the sender to the 'next in line' priority queue.
    webcontents = event.sender
})

