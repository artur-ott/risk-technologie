let logging = "";
function log(text) {
    logging += text;
    logging +="\n";
}

let AllowedMessages = ["MessageTypeList"];
let websocket;
let websocketInterval;

function startGame() {
    websocket.send("{\"type\": \"StartGame\"}");
    clearInterval(websocketInterval);
    websocketInterval = setInterval(function(){
        if (websocket.readyState == websocket.OPEN) {  
            websocket.send("{\"type\": \"Ping\"}");
        }
    }, 5000);
}

function connectToWebsocket() {
    // websocket = new WebSocket("ws://192.168.0.24:9000/game/socket");
    websocket = new WebSocket("ws://localhost:9000/game/socket");
    websocket.setTimeout;
}

connectToWebsocket();

websocket.onopen = function(event) {
    log("Connected to Websocket");
};

websocket.onclose = function () {
    log("Connection with Websocket Closed!");
    clearInterval(websocketInterval);
};

websocket.onerror = function (error) {
    log("Error in Websocket Occured: " + JSON.stringify(error));
};

websocket.onmessage = function (e) {
    let message = JSON.parse(e.data);
    log(e.data);
    if (AllowedMessages.indexOf(message.type) === -1) {
        return;
    }
    switch (message.type) {
        case "MessageTypeList":
            AllowedMessages = message.value;
            startGame();
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
            log("player: " + player + " troops: " + troops);
            break;
        default:
            log(message.type);
    }
};