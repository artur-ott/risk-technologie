let dicesValues = ["&#9856", "&#9857", "&#9858", "&#9859", "&#9860", "&#9861"];
let dicesTimer;

function rollDices(dicesContainer) {
    $("tr td.dice", dicesContainer).each(function(index, el){
        $(el).html(dicesValues[Math.floor(Math.random()*6)]);
    });
}

function createDices(dicesContainer, modal, playerNames, dices) {
    dices.forEach((player, index) => {
        let playerDicesHTML = document.createElement("tr");
        $(dicesContainer).append(playerDicesHTML);
        let playerName = document.createElement("td");
        $(playerName).text(String(playerNames[index]));
        $(playerName).css("font-size", "0.2em");
        $(playerName).css("padding-right", "1em");
        playerName.setAttribute("valign", "center");
        $(playerDicesHTML).append(playerName);
        player.forEach((dice) => {
            let diceHTML = document.createElement("td");
            $(diceHTML).addClass("dice");
            $(diceHTML).html(dicesValues[Math.floor(Math.random()*6)]);
            diceHTML.setAttribute("valign", "center");
            $(playerDicesHTML).append(diceHTML);
        });
    });
    
    $(modal).on("show.bs.modal", function (e) {
        let inter = setInterval(function () {rollDices(dicesContainer);}, 100);
        setTimeout(function() {
            clearInterval(inter);
            $(dicesContainer).children("tr").each(function (index, playerDices) {
                $(playerDices).children("td.dice").each(function (diceIndex, diceValue){
                    $(diceValue).html(String(dicesValues[dices[index][diceIndex] - 1]));
                });
            });
        }, 1000);
        $(modal).on("hide.bs.modal", function (ev) {
            $(dicesContainer).html("");
        });
    });
    $(modal).modal("show");
}