let map_node;
let map_background;
let map_reference;
let map_width;
let map_height;
let map_scale;
let map_ctx_map;
let map_ref;
let map_ctx_ref;
let map_img_map;
let map_img_ref;
let map_img_loaded = 0;
let map_data = {};

$(document).ready(function() {
    _map_init($("#map")[0], 1650, 1080, 0.5);
});

function _map_init(node, width=1650, height=1080, scale=1.0) {
    if (typeof map_refrences === "undefined") return;
    map_node = node;
    map_node.onclick = map_click;
    if (!node.hasAttribute("data-map") || !node.hasAttribute("data-ref")) return;
    map_background = node.getAttribute("data-map");
    map_reference = node.getAttribute("data-ref");
    map_width = width;
    map_height = height;
    map_scale = scale;
    Object.keys(map_refrences.colors).forEach(function(name) {
        map_data[name] = [255, 255, 255];//map_refrences.colors[name];
    });
    map_load_();
}

function map_load_() {
    map_node.setAttribute("width", map_width * map_scale);
    map_node.setAttribute("height", map_height * map_scale);
    map_ctx_map = map_node.getContext("2d");
    map_ctx_map.scale(map_scale, map_scale);

    map_ref = document.createElement("canvas");
    map_ref.setAttribute("width", map_width * map_scale);
    map_ref.setAttribute("height", map_height * map_scale);
    map_ctx_ref = map_ref.getContext("2d");
    map_ctx_ref.scale(map_scale, map_scale);

    map_img_map = document.createElement("img");
    map_img_ref = document.createElement("img");
    map_img_map.onload = map_load_img;
    map_img_ref.onload = map_load_img;
    map_img_map.src = map_background;
    map_img_ref.src = map_reference;
}

function map_load_img() {
    if (map_img_loaded == 1) {
        map_draw();
        delete map_img_loaded;
    } else {
        map_img_loaded++;
    }
}

function map_scale_map(scale_factor) {
    map_scale = scale_factor;
    map_node.setAttribute("width", map_width * map_scale);
    map_node.setAttribute("height", map_height * map_scale);
    map_ctx_map = map_node.getContext("2d");
    map_ctx_map.scale(map_scale, map_scale);

    map_ref.setAttribute("width", map_width * map_scale);
    map_ref.setAttribute("height", map_height * map_scale);
    map_ctx_ref = map_ref.getContext("2d");
    map_ctx_ref.scale(map_scale, map_scale);
    map_draw();
}

function map_draw() {
    map_ctx_map.drawImage(map_img_map, 0, 0, map_width, map_height);
    map_ctx_ref.drawImage(map_img_ref, 0, 0, map_width, map_height);
    Object.keys(map_data).forEach(function(name) {
        map_draw_country(name, map_data[name]);
    });
}

function map_draw_country(name, color) {
    if (typeof map_refrences === "undefined") return;
    if (typeof map_refrences.bounds[name] === "undefined") return;
    let bounds = map_refrences.bounds[name];
    let width = (bounds.max.x - bounds.min.x) * map_scale;
    let height = (bounds.max.y - bounds.min.y) * map_scale;

    let ref_data = map_ctx_ref.getImageData(bounds.min.x * map_scale, bounds.min.y * map_scale, width, height).data;
    let draw_data = map_ctx_map.getImageData(bounds.min.x * map_scale, bounds.min.y * map_scale, width, height).data;
    for (let i = ref_data.indexOf(map_refrences.colors[name][0]); i < ref_data.length && i !== -1; i += 4){
        if (map_refrences.colors[name][0] === ref_data[i] && map_refrences.colors[name][1] === ref_data[i + 1] &&
            map_refrences.colors[name][2] === ref_data[i + 2]) {
            // same color
            if (color[0] === draw_data[i] && color[1] === draw_data[i + 1] &&
                color[2] === draw_data[i + 2]) return;
            // different color => recolor
            draw_data[i] = color[0];
            draw_data[i + 1] = color[1];
            draw_data[i + 2] = color[2];
        }
    }
    let newImageData = map_ctx_map.createImageData(width, height);
    newImageData.data.set(draw_data);
    map_ctx_map.putImageData(newImageData, bounds.min.x * map_scale, bounds.min.y * map_scale);
}

function map_click(event) {
    var rect = map_node.getBoundingClientRect();
    let x = event.clientX - rect.left;
    let y = event.clientY - rect.top;
}

function map_position_to_land(x, y) {
    let ref_color = map_ctx_ref.getImageData(x, y, 1, 1).data;
    let land = [];
    if (ref_color.length >= 3) {
        land = Object.keys(map_refrences.colors).filter(function(land) {
            if(ref_color[0] === map_refrences.colors[land][0] &&
                ref_color[1] === map_refrences.colors[land][1] &&
                ref_color[2] === map_refrences.colors[land][2]) {
                return true;
            }
            return false;
        });
    }
    return land.join();
}

function map_display_continent() {
    let continents = {
        "ARGENTINIEN": [
            11,
            53,
            89
        ],
        "GROSSBRITANNIEN": [
            88,
            116,
            140
        ],
        "SUEDOSTASIEN": [
            235,
            221,
            208
        ],
        "CHINA": [
            235,
            221,
            208
        ],
        "NORDWEST-TERRITORIEN": [
            9,
            40,
            62
        ],
        "SIBIRIEN": [
            235,
            221,
            208
        ],
        "INDONESIEN": [
            161,
            170,
            177
        ],
        "ZENTRALAFRIKA": [
            36,
            84,
            115
        ],
        "SKANDINAVIEN": [
            88,
            116,
            140
        ],
        "NEUGUINEA": [
            161,
            170,
            177
        ],
        "JAPAN": [
            235,
            221,
            208
        ],
        "AEGYPTEN": [
            36,
            84,
            115
        ],
        "BRASILIEN": [
            11,
            53,
            89
        ],
        "PERU": [
            11,
            53,
            89
        ],
        "WESTSTAATEN": [
            9,
            40,
            62
        ],
        "JAKUTIEN": [
            235,
            221,
            208
        ],
        "VENEZUELA": [
            11,
            53,
            89
        ],
        "ALBERTA": [
            9,
            40,
            62
        ],
        "RUSSLAND": [
            88,
            116,
            140
        ],
        "AFGHANISTAN": [
            235,
            221,
            208
        ],
        "NORDAFRIKA": [
            36,
            84,
            115
        ],
        "OSTSTAATEN": [
            9,
            40,
            62
        ],
        "OSTAFRIKA": [
            36,
            84,
            115
        ],
        "INDIEN": [
            235,
            221,
            208
        ],
        "SUEDEUROPA": [
            88,
            116,
            140
        ],
        "SUEDAFRIKA": [
            36,
            84,
            115
        ],
        "ISLAND": [
            88,
            116,
            140
        ],
        "KAMTSCHATKA": [
            235,
            221,
            208
        ],
        "OSTAUSTRALIEN": [
            161,
            170,
            177
        ],
        "MITTELAMERIKA": [
            9,
            40,
            62
        ],
        "WESTAUSTRALIEN": [
            161,
            170,
            177
        ],
        "MONGOLEI": [
            235,
            221,
            208
        ],
        "WESTEUROPA": [
            88,
            116,
            140
        ],
        "MADAGASKAR": [
            36,
            84,
            115
        ],
        "MITTLERER-OSTEN": [
            235,
            221,
            208
        ],
        "OSTKANADA": [
            9,
            40,
            62
        ],
        "ALASKA": [
            9,
            40,
            62
        ],
        "GROENLAND": [
            9,
            40,
            62
        ],
        "ONTARIO": [
            9,
            40,
            62
        ],
        "IRKUTSK": [
            235,
            221,
            208
        ],
        "URAL": [
            235,
            221,
            208
        ],
        "NORDEUROPA": [
            88,
            116,
            140
        ]
    };
    let temp_map_data = map_data;
    map_data = continents;
    map_draw();
    map_data = temp_map_data;
}