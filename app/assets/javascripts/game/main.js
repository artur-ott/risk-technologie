let websocket;
let AllowedMessages = ["MessageTypeList"];
let websocketInterval;

function mouseoverPicture() {
    // Add nice hover hover function here
}

function loadGame() {
    // Dummy function. Probably never needed
}

function saveGame() {
    // Dummy function. Probably never needed
}

function showContinents() {
    // Nice dummy function here
}

function continueGame() {
    // nothing to do here. Move on
}

function closeGame() {
    // Dummy function. Implement it later
}

function withdraw() {
    // Insert function here
}

function finishMove() {
    // Dummy function to rule the world
}

function landClick(clickedLand) {
    let message = {
        "type": "Click",
        "message": clickedLand
    };
    websocket.send(JSON.stringify(message));
}

function startGame() {
    websocket.send("{\"type\": \"StartGame\"}");
    clearInterval(websocketInterval);
    websocketInterval = setInterval(function(){
        if (websocket.readyState === websocket.OPEN) {  
            websocket.send("{\"type\": \"Ping\"}");
        }
    }, 5000);
}

function spreadTroops(message) {
    barElement1.message = message.value.player;
    barElement2.message = "Ausrüsten";
    barElement3.message = message.value.troops;
    logging.push("player: " + message.value.player + " troops: " + message.value.troops);
}

function playerAttacking(message) {
    barElement1.message = message.value;
    barElement2.message = "Angreifen";
    barElement3.message = "";
    logging.push("attacking player: " + message.value);
}

function websocketMessages(data) {
    let message = JSON.parse(data);
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
            spreadTroops(message);
            break;
        case "PlayerAttacking":
            playerAttacking(message);
            break;
        case "DicesRolled":
            createDices($(".dices"), $("#dices-modal"), message.value.players, message.value.dices);
            break;
        default:
            logging.push(message.type);
    }
}

$(document).ready(function() {
    document.getElementById("dropdownLoad").addEventListener("click", loadGame);
    document.getElementById("dropdownSave").addEventListener("click", saveGame);
    document.getElementById("dropdownShow").addEventListener("click", showContinents);
    document.getElementById("dropdownContinue").addEventListener("click", continueGame);
    document.getElementById("dropdownClose").addEventListener("click", closeGame);
    document.getElementById("withdrawButton").addEventListener("click", withdraw);
    document.getElementById("finishMoveButton").addEventListener("click", finishMove);
    _map_init($("#map")[0], landClick, 1650, 1080, 1);
    websocket = connectToWebsocket(websocketMessages);
});