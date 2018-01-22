function conqueredCountry(conquered_country, land) {
    $(conquered_country).show();
    $(conquered_country).html("");
    $(conquered_country).text(land + " wurde erobert!");
}

function playerConqueredCountry(conquered_country, troops, playerConqueredCountrySocket, hideStatusElements) {
    $(conquered_country).show();
    $(conquered_country).html("");

    let label = document.createElement("label");
    label.setAttribute("for", "troopsSelect");
    $(label).text("Verfügbare Truppen:");
    $(conquered_country)[0].append(label);
    let select = document.createElement("select");
    select.setAttribute("id", "troopsSelect");
    select.className = "form-control";
    $(conquered_country)[0].append(select);
    for (let i = 1; i < troops; i++) {
        let option = document.createElement("option");
        option.value = i;
        $(option).text(i);
        $(select)[0].append(option);
    }
    let button = document.createElement("button");
    button.className = "btn btn-primary";
    $(button).text("verschieben");
    $(button).click(function (e) {
        playerConqueredCountrySocket(parseInt($(select).val()));
        hideStatusElements();
    });
    $(conquered_country)[0].append(button);
}