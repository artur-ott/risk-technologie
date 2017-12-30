$(document).ready(function () {
    $("#game_name").keyup(function(e) {
        if ($(this).val().length > 0) {
            $("#play_game").removeAttr("disabled");
        } else {
            $("#play_game").attr("disabled", "true");
        }
    });
});