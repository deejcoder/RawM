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
    sendMessage();

})


$('#messageInput').keypress(function(e) {
    if(e.which == 13) {
        sendMessage();
    }
})

//I know this is very messy, I just love to experiment first !
$(document).ready(function() {
    var userElements = document.querySelectorAll(".user");
    var elem;

    console.log(userElements);
    for(var i = 0; i < userElements.length; i++) {
        userElements[i].addEventListener("click", function(event) {
            if(elem === this) {
                $("#profileview").hide();
                elem = null;
                this.style.backgroundColor = null;
                this.style.color = null;
                return;
            }
            this.style.backgroundColor = "#3C3F41";
            this.style.color = "white";

            elem = this;
            console.log('test');
            obj = {
                "type":"userinfo",
                "message": {
                    "username": "Dylan",
                }
            }
            json = JSON.stringify(obj);
            ipcRenderer.send('send', json)
        });
    }

});


getUserList();
function getUserList() {
    obj = {
        "type":"FetchActiveUsers",
        "message": {
            "blah":"blah"
        }
    }

    json = JSON.stringify(obj);
    ipcRenderer.send('send', json)


}

//To be moved ----
function sendMessage() {
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
    $('#messageInput').val("");
}

ipcRenderer.on('reply', (event, message) => {
    console.log(message)
    //Convert JSON into JS object.
    json = JSON.parse(message)

    //Check if the message is an authorization type message.
    if(json.type == "message") {

        //TODO: make server replies with 'sent', and doesn't return client's msg
        showMessage(json.body.sender.toString(), json.body.body.toString(), false);
   
        //$('#messageBox').text($("#messageBox").val() + obj.body.body.toString() + "\n")
    }

    if(json.type == "activeusers") {

        $("#userlist ul li").remove();
        for(user in json.body) {
            $("#userlist ul").append("<li class='user'>" + json.body[user] + "</li>");
        }
    }

    if(json.type == "userinfo") {
        $("#profileview .text").text(json.body);
        $("#profileview").show();
    }
})

function showMessage(sender, data, client) {
    var template = $('#messageTemplate').html().trim()
    var clone = $(template)

    if(client == false) {
        clone.attr("class", "message serverMessage")
    }

    clone.find('.sender').text(sender)
    clone.find('.body').text(data)
    clone.appendTo('#messageBox')

    //Set scroll to bottom
    $('#messageBox').scrollTop($("#messageBox")[0].scrollHeight)
}

//---