let AllowedMessages = ["MessageTypeList"];
let websocket;
let websocketInterval;
function connectToWebsocket() {
    websocket = new WebSocket("ws://192.168.0.24:9000/game/socket");
    // websocket = new WebSocket("ws://localhost:9000/game/socket");
    websocket.setTimeout
}

connectToWebsocket();

websocket.onopen = function(event) {
    console.log("Connected to Websocket");
}

websocket.onclose = function () {
    console.log('Connection with Websocket Closed!');
    clearInterval(websocketInterval);
};

websocket.onerror = function (error) {
    console.log('Error in Websocket Occured: ' + JSON.stringify(error));
};

websocket.onmessage = function (e) {
    let message = JSON.parse(e.data);
    console.log(e.data);
    if (AllowedMessages.indexOf(message.type) === -1) return;
    switch (message.type) {
        case "MessageTypeList":
            AllowedMessages = message.value;
            websocket.send('{"type": "StartGame"}');
            clearInterval(websocketInterval);
            websocketInterval = setInterval(function(){
                websocket.send('{"type": "Ping"}');
            }, 5000);
            break;
        case "Ping":
            break;
        case "UpdateMap":
            map_data = message.value;
            map_draw();
            break;
        case "SpreadTroops":
            let player = message.value.player;
            let troops = message.value.troops;
            console.log("player: " + player + " troops: " + troops);
            break;
        default:
            console.log(message.type);
    }
};