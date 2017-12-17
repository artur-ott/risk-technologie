/**
 * document ready needed !!!!!! aaaaarrrrr
 */

$(document).ready(function () {

    let barElement1 = new Vue({
        el: "#bar-1",
        data: {
            message: "11, 3"
        }
    })

    let barElement2 = new Vue({
        el: "#bar-2",
        data: {
            message: "Aufrüsten"
        }
    })

    let barElement3 = new Vue({
        el: "#bar-3",
        data: {
            message: "3"
        }
    })

    let message1 = new Vue({
        el: "#message-1",
        data: {
            message: "The best game ! "
        }
    })

})