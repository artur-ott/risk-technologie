$(document).ready(function() {
    document.getElementById("testpicture").addEventListener("mouseover", mouseoverPicture);
    document.getElementById("dropdownLoad").addEventListener("click", loadGame);
    document.getElementById("dropdownSave").addEventListener("click", saveGame);
    document.getElementById("dropdownShow").addEventListener("click", showContinents);
    document.getElementById("dropdownContinue").addEventListener("click", continueGame);
    document.getElementById("dropdownClose").addEventListener("click", closeGame);
    document.getElementById("withdrawButton").addEventListener("click", withdraw);
    document.getElementById("finishMoveButton").addEventListener("click", finishMove);

});

function mouseoverPicture() {
    console.log("Pictured viewed");
}

function loadGame() {
    console.log("Loaded game");
}

function saveGame() {
    console.log("Saved game");
}

function showContinents() {
    console.log("Showed continents");
}

function continueGame() {
    console.log("Continued game");
}

function closeGame() {
    console.log("Closed game");
}

function withdraw() {
    console.log("Rückgängig geklickt");
}

function finishMove() {
    console.log("Zug beendet geklickt");
}

