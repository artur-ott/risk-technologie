let AllowedMessages = [];
let websocket;
function connectToWebsocket() {
    websocket = new WebSocket("ws://192.168.0.24:9000/game/socket");
    // websocket = new WebSocket("ws://localhost:9000/game/socket");
}

connectToWebsocket();

websocket.onopen = function(event) {
    websocket.send(JSON.stringify({
        "type": "MessageTypeList"
    }));
}

websocket.onclose = function () {
    console.log('Connection with Websocket Closed!');
    setTimeout(connectToWebsocket, 100);
};

websocket.onerror = function (error) {
    console.log('Error in Websocket Occured: ' + JSON.stringify(error));
};

websocket.onmessage = function (e) {
    let message = JSON.parse(e.data);
    if (AllowedMessages.indexOf(message.type) === -1) return;
    switch (message.type) {
        case "MessageTypeList":
            AllowedMessages = message.value;
            break;
        case "UpdateMap":
            map_data = message.value;
            map_draw();
        default:
            console.log(message.type);
    }
};