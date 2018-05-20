/*
    @Author: Dylan Tonks
    @Created Date: 20/05/2018

    The main logic behind the index view.
    This may depend on ./messaging/*
*/

//===> Requires <===
const {ipcRenderer, remote} = require('electron')
let $ = require('jquery')
let url = require('url')
let path = require('path')


//Create an event listener for the send button
const sendBtn = document.getElementById('button_send')

sendBtn.addEventListener('click', function(event) {
    console.log('test')
    //Create the JSON corresponding to a message request
    obj = {
        "type":"message",
        "message": {
            "address": "all",
            "body": $('#messageInput').val()
        }
    }
    json = JSON.stringify(obj)
    ipcRenderer.send('send', json)
})

ipcRenderer.on('reply', (event, message) => {

    //Convert JSON into JS object.
    obj = JSON.parse(message)

    //Check if the message is an authorization type message.
    if(obj.body.type == "message") {

        console.log("MESSAGE!")
        $('#messageBox').text($("#messageBox").val() + obj.body.body.toString())
    }
    console.log(message)
})