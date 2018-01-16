$(document).ready(function () {
    $("#game_name").keyup(activateGame);
    $("#game_name").select(activateGame);
    $("#games").change(activateGame)
});

function activateGame(e) {
    if ($("#game_name").val().length > 0 || $("#games")[0].selectedIndex > 0) {
        $("#play_game").removeAttr("disabled");
    } else {
        $("#play_game").attr("disabled", "true");
    }
}