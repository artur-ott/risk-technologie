var websocket = new WebSocket("ws://192.168.0.24:9000/game/socket");
let AllowedMessages = [];

websocket.onopen = function(event) {
    websocket.send(JSON.stringify({
        "type": "MessageTypeList"
    }));
}

websocket.onclose = function () {
    console.log('Connection with Websocket Closed!');
};

websocket.onerror = function (error) {
    console.log('Error in Websocket Occured: ' + JSON.stringify(error));
};

websocket.onmessage = function (e) {
    let message = JSON.parse(e.data);
    switch (message) {
        case "MessageTypeList":
            AllowedMessages = message.value;
            console.log(message.value);
        break;
        default:
            console.log(message.type);
    }
};