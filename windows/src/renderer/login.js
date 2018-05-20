/*
    @Author: Dylan Tonks
    @Created Date: 19/05/2018

    This code fragment belongs to the login
    view. It handles authorization requests.
*/

//===> Requires <===
const {ipcRenderer, remote} = require('electron')
let $ = require('jquery')
let url = require('url')
let path = require('path')

//Create an event listener for the login button
const loginBtn = document.getElementById('login_button')

loginBtn.addEventListener('click', function(event) {

    //Create the JSON corresponding to an authorization request
    obj = {
        "type":"authorization",
        "message": {
            "username": $('#userName').val(),
            "password": $('#passWord').val()
        }
    }
    json = JSON.stringify(obj)

    //Send the JSON to the main process for sending
    ipcRenderer.send('send', json)
})

/*
    This is invoked when the main process has recieved
    some data from the Java server, after being requested
    to send the data from this file.
*/
ipcRenderer.on('reply', (event, message) => {

    //Convert JSON into JS object.
    obj = JSON.parse(message)

    //Check if the message is an authorization type message.
    if(obj.body.type == "authorization") {

        //Is the user authorized?
        if(obj.body.body == "Authorized") {
            console.log("AUTHORIZED!")

            //Load the index page
            remote.getCurrentWindow().loadURL(url.format ({
                pathname: path.join(__dirname, './index.html'),
                protocol: 'file:',
                slashes: true
             }))
        }
    }
    console.log(message)
})
