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



$(document).ready(function() {
    $("#dropdownLoad").click(loadGame);
    $("#dropdownSave").click(saveGame);
    $("#dropdownShow").click(showContinents);
    $("#dropdownContinue").click(continueGame);
    $("#dropdownClose").click(closeGame);
    $("#withdrawButton").click(withdraw);
    $("#finishMoveButton").click(finishMove);
    _map_init($("#map")[0], 1650, 1080, 1);
});
