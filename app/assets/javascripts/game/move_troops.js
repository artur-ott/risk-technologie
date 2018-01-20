let landFrom = null;
let landTo = null;
let select = null;
let button = null;

function moveTroops(move_troops, move_troops_socket, resetMoveTroops) {
    $(move_troops).show();
    $(move_troops).html("");

    let fromDiv = document.createElement("div");
    let labelFrom = document.createElement("label");
    fromDiv.appendChild(labelFrom);
    labelFrom.setAttribute("for", "landFrom");
    $(labelFrom).text("Von: ");
    landFrom = document.createElement("div");
    landFrom.className = "landFrom inline";
    fromDiv.appendChild(landFrom);
    $(move_troops)[0].appendChild(fromDiv);

    let toDiv = document.createElement("div");
    let labelTo = document.createElement("label");
    toDiv.appendChild(labelTo);
    labelTo.setAttribute("for", "landTo");
    $(labelTo).text("Nach: ");
    landTo = document.createElement("div");
    landTo.setAttribute("id", "landTo");
    landTo.className = "landTo inline";
    toDiv.appendChild(landTo);
    $(move_troops)[0].appendChild(toDiv);

    select = document.createElement("select");
    select.setAttribute("id", "troopsSelect");
    select.className = "form-control inline w10em";
    select.setAttribute("disabled", "disabled");
    $(move_troops)[0].append(select);

    let reset = document.createElement("button");
    reset.className = "btn btn-primary inline ml- mr-";
    $(reset).text("zurücksetzen");
    $(reset).click(function (e) {
        $(landFrom).text("");
        $(landTo).text("");
        $(select).html("");
        select.setAttribute("disabled", "disabled");
        resetMoveTroops();
    });
    $(move_troops)[0].appendChild(reset);

    button = document.createElement("button");
    button.className = "btn btn-primary inline";
    $(button).text("verschieben");
    button.setAttribute("disabled", "disabled");
    $(button).click(function (e) {
        let landFromName = $(landFrom).text();
        let landToName = $(landTo).text();
        if (landFrom === null || landTo === null ||
            landFromName.length === 0 || landToName.length === 0) {
                return;
            }

        move_troops_socket(landFromName, landToName, $(select).val());
    });
    $(move_troops)[0].appendChild(button);

    $(reset).click(function (e) {
        $(landFrom).text("");
        $(landTo).text("");
        $(select).html("");
        select.setAttribute("disabled", "disabled");
        button.setAttribute("disabled", "disabled");
        resetMoveTroops();
    });
}

function setFromLand(land, troops) {
    if (landFrom === null || landTo === null || select === null) {
        return;
    }
    for (let i = 1; i < troops; i++) {
        let option = document.createElement("option");
        option.value = i;
        $(option).text(i);
        select.append(option);
    }
    $(landFrom).text(land);
}

function setToLand(land) {
    if (landFrom === null || landTo === null || select === null) return;
    $(landTo).text(land);
    select.removeAttribute("disabled");
    button.removeAttribute("disabled");
}