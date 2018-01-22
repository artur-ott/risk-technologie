let logging = [];

function connectToWebsocket(receiveMethod) {
    // let websocket = new WebSocket("ws://192.168.0.24:9000/game/socket");
    let websocket = new WebSocket("ws://localhost:9000/game/socket");
    websocket.setTimeout;
    websocketInitEvents(websocket, receiveMethod);
    return websocket;
}

function websocketInitEvents(websocket, receiveMethod) {
    websocket.onopen = function(event) {
        logging.push("Connected to Websocket");
    };
    
    websocket.onclose = function () {
        logging.push("Connection with Websocket Closed!");
        clearInterval(websocketInterval);
    };
    
    websocket.onerror = function (error) {
        logging.push("Error in Websocket Occured: " + JSON.stringify(error));
    };
    
    websocket.onmessage = function (e) {
        logging.push(e.data);
        receiveMethod(e.data);
    };
}