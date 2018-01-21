let websocket;
let AllowedMessages = ["MessageTypeList"];
let websocketInterval;
let user_list;
let dices;
let conquered_country;
let move_troops;
let start_game;

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

function hideStatusElements() {
    start_game.hide();
    dices.hide();
    conquered_country.hide();
    move_troops.hide();
}

function startGame(e) {
    hideStatusElements();
    websocket.send("{\"type\": \"StartGame\"}");
}

function finishMove() {
    websocket.send("{\"type\": \"EndTurn\"}");
}

function landClick(clickedLand) {
    let message = {
        "type": "Click",
        "message": clickedLand
    };
    websocket.send(JSON.stringify(message));
}

function playerConqueredCountrySocket(troops) {
    let message = {
        "type": "MoveTroops",
        "message": troops
    };
    websocket.send(JSON.stringify(message));
}

function moveTroopsSocket(landFrom, landTo, troops) {
    let message = {
        "type": "TransfereTroops",
        "message": {
            landFrom,
            landTo,
            "troops": parseInt(troops)
        }
    };
    websocket.send(JSON.stringify(message));
}

function resetMoveTroops() {
    websocket.send("{\"type\": \"ResetTransfereTroops\"}");
}

function keepAlive() {
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

function playerConqueredCountryMove() {
    barElement1.message = "";
    barElement2.message = "Verschieben";
    barElement3.message = "";
}

function websocketMessages(data) {
    let message = JSON.parse(data);
    if (AllowedMessages.indexOf(message.type) === -1) {
        return;
    }
    switch (message.type) {
        case "MessageTypeList":
            AllowedMessages = message.value;
            keepAlive();
            break;
        case "Ping":
            break;
        case "UpdateMap":
            map_data = message.value;
            map_draw();
            break;
        case "PlayerList":
            $(user_list).html("");
            let userTable = document.createElement("table");
            userTable.className = "halfFontSize";
            $(user_list)[0].appendChild(userTable);
            let userTbody = document.createElement("tbody");
            userTable.appendChild(userTbody);
            let users = Array.from(message.value);
            for (let i = 0; i < users.length; i++) {
                let user = users[i];
                let userTr = document.createElement("tr");
                userTbody.appendChild(userTr);
                let userColor = document.createElement("td");
                userColor.className = "playerColor mr- " + user[1];
                userTr.appendChild(userColor);
                let userName = document.createElement("td");
                $(userName).text(user[0]);
                userTr.appendChild(userName);
            }
            break;
        case "SpreadTroops":
            hideStatusElements();
			// Disable end turn
            spreadTroops(message);
            break;
        case "PlayerAttacking":
            playerAttacking(message);
            break;
        case "DicesRolled":
            hideStatusElements();
            createDices(dices, message.value.lands, message.value.dices);
            break;
        case "PlayerConqueredCountry":
            hideStatusElements();
            playerConqueredCountry(conquered_country, message.value, playerConqueredCountrySocket, hideStatusElements);
            break;
        case "ConqueredCountry":
            hideStatusElements();
            conqueredCountry(conquered_country, message.value);
            break;
        case "TransfereTroops":
            hideStatusElements();
            playerConqueredCountryMove();
            moveTroops(move_troops, moveTroopsSocket, resetMoveTroops);
            break;
        case "TransfereTroopsSetFromLand":
            setFromLand(message.value.land, message.value.troops);
            break;
        case "TransfereTroopsSetToLand":
            setToLand(message.value);
            break;
        default:
            logging.push(message.type);
    }
}

$(document).ready(function() {
    $("#dropdownLoad").click(loadGame);
    $("#dropdownSave").click(saveGame);
    $("#dropdownShow").click(showContinents);
    $("#dropdownContinue").click(continueGame);
    $("#dropdownClose").click(closeGame);
    $("#withdrawButton").click(withdraw);
    $("#finishMoveButton").click(finishMove);
    _map_init($("#map")[0], landClick, 1650, 1080, 1);
    user_list = $(".user_list");
    dices = $(".dices");
    conquered_country = $(".conquered_country");
    move_troops = $(".move_troops");
    start_game = $(".start_game");
    websocket = connectToWebsocket(websocketMessages);
    $(".start_game button").click(startGame);
});