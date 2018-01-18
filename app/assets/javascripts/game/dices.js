let dicesValues = ["&#9856", "&#9857", "&#9858", "&#9859", "&#9860", "&#9861"];
let dicesTimer;

function rollDices(dicesContainer) {
    $("tr td.dice", dicesContainer).each(function(index, el){
        $(el).html(dicesValues[Math.floor(Math.random()*6)]);
    });
}

function createDices(dicesContainer, landNames, dices) {
    $(dicesContainer).show();
    $(dicesContainer).html("");
    dices.forEach((player, index) => {
        let playerDicesHTML = document.createElement("tr");
        $(dicesContainer).append(playerDicesHTML);
        let landName = document.createElement("td");
        $(landName).text(landNames[parseInt(index)]);
        $(landName).css("font-size", "0.2em");
        $(landName).css("padding-right", "1em");
        landName.setAttribute("valign", "center");
        $(playerDicesHTML).append(landName);
        player.forEach((dice) => {
            let diceHTML = document.createElement("td");
            $(diceHTML).addClass("dice");
            $(diceHTML).html(dicesValues[Math.floor(Math.random()*6)]);
            diceHTML.setAttribute("valign", "center");
            $(playerDicesHTML).append(diceHTML);
        });
    });

    let inter = setInterval(function () {rollDices(dicesContainer);}, 100);
    setTimeout(function() {
        clearInterval(inter);
        $(dicesContainer).children("tr").each(function (index, playerDices) {
            $(playerDices).children("td.dice").each(function (diceIndex, diceValue){
                $(diceValue).html(String(dicesValues[dices[parseInt(index)][parseInt(diceIndex)] - 1]));
            });
        });
    }, 1000);
}