$(document).ready(function () {
    $("#play_name").keyup(function(e) {
        if ($(this).val().length > 0) {
            $('#new_game').removeAttr("disabled");
        } else {
            $('#new_game').attr("disabled", "true");
        }
    });
});