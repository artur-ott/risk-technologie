var websocket = new WebSocket("ws://192.168.0.24:9000/game/socket");

websocket.onopen = function(event) {
    console.log("Connected to Websocket");
}

websocket.onclose = function () {
    console.log('Connection with Websocket Closed!');
};

websocket.onerror = function (error) {
    console.log('Error in Websocket Occured: ' + JSON.stringify(error));
};

websocket.onmessage = function (e) {
    console.log("Message: " + e.data);
};