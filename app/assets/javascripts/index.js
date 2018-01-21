$(document).ready(function () {
    $("#game_name").keyup(activateGame);
    $("#game_name").on("propertychange input", activateGame);
    $("#games").change(activateGame);
    getGameList();
    setInterval(function() {getGameList();}, 1000);
});

function activateGame(e) {
    if ($("#game_name").val().length > 0 || $("#games")[0].selectedIndex > 0) {
        $("#play_game").removeAttr("disabled");
    } else {
        $("#play_game").attr("disabled", "true");
    }
}

function getGameList() {
    $.get($("#games").attr("url"), reloadGameList);
}

function reloadGameList(data) {
    let games = Array.from(JSON.parse(data));
    let selected = $("#games").val().toString();
    $("#games").html("<option></option>");
    $("#games").attr("disabled", "disabled");
    for(let i = 0; i < games.length; i++) {
        let option = document.createElement("option");
        option.value = games[i];
        $(option).text(games[i]);
        if (games[i] === selected) {
            option.setAttribute("selected", "selected");
        }
        $("#games")[0].appendChild(option);
        $("#games").removeAttr("disabled");
    }
}