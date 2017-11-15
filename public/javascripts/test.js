$(document).ready(function() {
    $('#ref')[0].onload = onLoadImg;
});

function onLoadImg() {
    let white = $('#map_white')[0];
    let ctxWhite = white.getContext("2d");
    ctxWhite.scale(0.5, 0.5);
    ctxWhite.drawImage($('#map')[0], 0, 0, 1650, 1080);

    let canvas = $('#reference')[0];
    let ctxRef = canvas.getContext("2d");
    ctxRef.scale(0.5, 0.5);
    ctxRef.drawImage($('#ref')[0], 0, 0, 1650, 1080);
    canvas.onload = canvasLoaded();

    function canvasLoaded() {
        $(white).click(onClickImg);
        $(white).mousemove(function(e) {hover(e)});
    }

    function onClickImg(e) {
        var rect = white.getBoundingClientRect();
        let x = e.clientX - rect.left;
        let y = e.clientY - rect.top;
        console.log("X: " + x + " Y: " + y + " Color: " + JSON.stringify(ctxRef.getImageData(x, y, 1, 1)));
    }

    function hover(e) {
        var rect = white.getBoundingClientRect();
        let x = e.clientX - rect.left;
        let y = e.clientY - rect.top;
        let color = ctxRef.getImageData(x, y, 1, 1);
        let data = color.data;
        let imData = ctxRef.getImageData(0, 0, 1650, 1080).data;
        let imDataWhite = ctxWhite.getImageData(0, 0, 1650, 1080).data;
        let land = getLandFromPixel(data);
        console.log(land);
        for (let i = 0; i < imData.length; i += 4) {
            if (!(data[0] === 0 && data[1] === 0 && data[2] === 0) && data[0] === imData[i] && data[1] === imData[i+1] && data[2] === imData[i+2]) {
                imDataWhite[i] = 123;
                imDataWhite[i+1] = 232;
                imDataWhite[i+2] = 12;
            }
        }
        let imDataWhiteCtx = ctxWhite.createImageData(1650, 1080);
        imDataWhiteCtx.data.set(imDataWhite);
        ctxWhite.putImageData(imDataWhiteCtx, 0, 0);
    }
}

function getLandFromPixel(pixelData) {
    if (pixelData.length >= 3) {
        console.log("foo");
        Object.keys(map_refrences.colors).forEach(function(land) {
            if(pixelData[0] === map_refrences.colors[land][0] && pixelData[1] === map_refrences.colors[land][1] && pixelData[2] === map_refrences.colors[land][2]) {
                return land;
            }
        });
    }
    return "";
}