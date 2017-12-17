/**
 * document ready needed !!!!!! aaaaarrrrr
 */

let barElement1;
let barElement2;
let barElement3;
let message1;

$(document).ready(function () {

    barElement1 = new Vue({
        el: "#bar-1",
        data: {
            message: "11, 3"
        }
    });

    barElement2 = new Vue({
        el: "#bar-2",
        data: {
            message: "Aufrüsten"
        }
    });

    barElement3 = new Vue({
        el: "#bar-3",
        data: {
            message: "3"
        }
    });

    message1 = new Vue({
        el: "#message-1",
        data: {
            message: "The best game ! "
        }
    });

});