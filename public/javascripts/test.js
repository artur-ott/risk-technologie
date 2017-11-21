var map_;
$(document).ready(function() {
    //$('#ref')[0].onload = onLoadImg;
    map_ = new MapComponent($("#map")[0], 1650, 1080, 0.5);
});

function onLoadImg() {
    let white = $('#map_white')[0];
    let ctxWhite = white.getContext("2d");
    // ctxWhite.scale(0.5, 0.5);
    ctxWhite.drawImage($('#map')[0], 0, 0, 1650, 1080);

    let canvas = $('#reference')[0];
    let ctxRef = canvas.getContext("2d");
    // ctxRef.scale(0.5, 0.5);
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
        let data = ctxRef.getImageData(x, y, 1, 1).data;
        setPixelsinArea(ctxRef, ctxWhite, data, [123, 232, 12]);
    }
}

function setPixelsinArea(ctxRef, ctxDraw, targetColorArray, newColor) {
    let land = getLandFromPixel(targetColorArray);
    if (land.length === 0) return;
    /*
        TODO: needs to be scaled
    */
    let bounds = map_refrences.bounds[land];
    let width = bounds.max.x - bounds.min.x;
    let height = bounds.max.y - bounds.min.y;
    let dataRef = ctxRef.getImageData(bounds.min.x, bounds.min.y, width, height).data;
    let dataDraw = ctxDraw.getImageData(bounds.min.x, bounds.min.y, width, height).data;
    for (let i = dataRef.indexOf(targetColorArray[0]); i < dataRef.length && i !== -1; i += 4){
        if (targetColorArray[0] === dataRef[i] && targetColorArray[1] === dataRef[i + 1] &&
            targetColorArray[2] === dataRef[i + 2]) {
            // same color
            if (newColor[0] === dataDraw[i] && newColor[1] === dataDraw[i + 1] &&
                newColor[2] === dataDraw[i + 2]) return;
            // different color => recolor
            dataDraw[i] = newColor[0];
            dataDraw[i + 1] = newColor[1];
            dataDraw[i + 2] = newColor[2];
        }
    }
    let newImageData = ctxDraw.createImageData(width, height);
    newImageData.data.set(dataDraw);
    ctxDraw.putImageData(newImageData, bounds.min.x, bounds.min.y);
}

function getLandFromPixel(pixelData) {
    let land = [];
    if (pixelData.length >= 3) {
        land = Object.keys(map_refrences.colors).filter(function(land) {
            if(pixelData[0] === map_refrences.colors[land][0] && pixelData[1] === map_refrences.colors[land][1] && pixelData[2] === map_refrences.colors[land][2]) {
                return true;
            }
            return false;
        });
    }
    return land.join();
}

class MapComponent {
    /*
        Global variables:
            - node
            - ref
            - ctxMap
            - ctxRef
            - imgMap
            - imgRef

            - background
            - reference
            - width
            - height
            - scale
    */
    constructor(node, width=1650, height=1080, scale=1.0) {
        this.node = node;
        this.background = node.getAttribute("data-map");
        this.reference = node.getAttribute("data-ref");
        this.width = width;
        this.height = height;
        this.scale = scale;
        this.init();
    }

    init() {
        this.node.setAttribute("width", this.width * this.scale);
        this.node.setAttribute("height", this.height * this.scale);
        this.ctxMap = this.node.getContext("2d");
        this.ctxMap.scale(this.scale, this.scale);

        this.ref = document.createElement("canvas");
        this.ref.setAttribute("width", this.width * this.scale);
        this.ref.setAttribute("height", this.height * this.scale);
        this.ctxRef = this.ref.getContext("2d");
        this.ctxRef.scale(this.scale, this.scale);

        this.imgMap = document.createElement("img");
        this.imgRef = document.createElement("img");
        this.imageLoadedCount = 0;
        $(this.imgMap).load(imageLoaded);
        this.imgRef.onload = imageLoaded();
        this.imgMap.src = this.background;
        document.body.appendChild(this.imgMap);
    }

    imageLoaded() {
        if (this.imageLoadedCount === 1) {

            delete this.imageLoadedCount;
        } else {
            this.imageLoadedCount++;
        }
        console.log(this.imageLoadedCount);
    }

    toString() {
        return JSON.stringify(this);
    }
}