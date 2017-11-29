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
    document.getElementById("dropdownLoad").addEventListener("click", loadGame);
    document.getElementById("dropdownSave").addEventListener("click", saveGame);
    document.getElementById("dropdownShow").addEventListener("click", showContinents);
    document.getElementById("dropdownContinue").addEventListener("click", continueGame);
    document.getElementById("dropdownClose").addEventListener("click", closeGame);
    document.getElementById("withdrawButton").addEventListener("click", withdraw);
    document.getElementById("finishMoveButton").addEventListener("click", finishMove);
    _map_init($("#map")[0], 1650, 1080, 1);
});
