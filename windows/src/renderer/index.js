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

    showMessage("You", obj.message.body, true);
    ipcRenderer.send('send', json)
})

ipcRenderer.on('reply', (event, message) => {
    console.log(message)
    //Convert JSON into JS object.
    obj = JSON.parse(message)
    body = obj.body;

    //Check if the message is an authorization type message.
    if(body.type == "message") {

        //TODO: make server replies with 'sent', and doesn't return client's msg
        showMessage(body.sender.toString(), body.body.toString(), false);
   
        //$('#messageBox').text($("#messageBox").val() + obj.body.body.toString() + "\n")
    }
    console.log(message)
})

function showMessage(sender, body, client) {
    var template = $('#messageTemplate').html().trim()
    var clone = $(template)

    if(client == false) {
        clone.attr("class", "message serverMessage")
    }

    clone.find('.sender').text(sender)
    clone.find('.body').text(body)
    clone.appendTo('#messageBox')
}