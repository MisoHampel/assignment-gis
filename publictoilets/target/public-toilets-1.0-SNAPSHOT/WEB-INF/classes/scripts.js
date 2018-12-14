
var url ="http://"+location.host+"/pdt";
var filterstatus = false;
// L.mapbox.accessToken = 'pk.eyJ1IjoibWlzb2hhbXBlbCIsImEiOiJjam5venJ3NWUwMzh0M3BvaTlncGR1YzU2In0.1AyaZVvI4qGr3lREewI1fA';
// var map = L.mapbox.map('map', 'mapbox.streets', {}).setView(startPosition, 5);
// L.mapbox.styleLayer("mapbox://styles/misohampel/cjopj15765c6m2so3nem8qhxk").addTo(map);

mapboxgl.accessToken = 'pk.eyJ1IjoibWlzb2hhbXBlbCIsImEiOiJjam5venJ3NWUwMzh0M3BvaTlncGR1YzU2In0.1AyaZVvI4qGr3lREewI1fA';
var map = new mapboxgl.Map({
    container: 'map',
    center: [135, -25],
    // style: 'mapbox://styles/misohampel/cjopj15765c6m2so3nem8qhxk', without icons
    style: 'mapbox://styles/misohampel/cjoqv0gys01a92ss2zsdxj3eg',
    zoom: 4,
});
var draw = new MapboxDraw({
    displayControlsDefault: false,
    controls: {
        polygon: true,
        trash: true
    }
});

//kreslenie
map.addControl(draw, 'top-left');
map.on('draw.create', updateArea);
map.on('draw.delete', updateArea);
map.on('draw.update', updateArea);

function updateArea(e) {
    var data = draw.getAll();
    console.log(JSON.stringify(data));
    var requestUrl = encodeURI(url+"/polygon?geojson="+JSON.stringify(data)+getFilters());
    jQuery.get(requestUrl, function(response) {
        // var geojson = {
        //     "type": "geojson",
        //     "data": response
        //
        // };
        map.getSource("toilets").setData(response);
    });
}

/*MARKER - ACTUAL POSITION*/
var actualPosition = new mapboxgl.Marker({
    draggable: true
})
    .setLngLat(map.getCenter())
    .addTo(map);
actualPosition.on('dragend', function () {
    lngLat = actualPosition.getLngLat();
    var lat = String(lngLat.lat).substr(0,10);
    var lng = String(lngLat.lng).substr(0,10);
    document.getElementById("latitude").innerText = lat;
    document.getElementById("longitude").innerText = lng;
});

lngLat = actualPosition.getLngLat();
var lat = String(lngLat.lat).substr(0,10);
var lng = String(lngLat.lng).substr(0,10);
document.getElementById("latitude").innerText = lat;
document.getElementById("longitude").innerText = lng;



map.on('click', function (e) {

});

map.on('dblclick', function(e) {
});

var hoveredStateId =  null;
map.on('load', function() {

    map.addSource("toilets", {
        type: "geojson",
        // Point to GeoJSON data. This example visualizes all M1.0+ earthquakes
        // from 12/22/15 to 1/21/16 as logged by USGS' Earthquake hazards program.
        data: {
            "type": "FeatureCollection",
            "features": []
        },
        cluster: true,
        clusterMaxZoom: 14, // Max zoom to cluster points on
        clusterRadius: 50 // Radius of each cluster when clustering points (defaults to 50)
    });
    map.addLayer({
        id: "clusters",
        type: "circle",
        source: "toilets",
        filter: ["has", "point_count"],
        paint: {
            // Use step expressions (https://www.mapbox.com/mapbox-gl-js/style-spec/#expressions-step)
            // with three steps to implement three types of circles:
            //   * Blue, 20px circles when point count is less than 100
            //   * Yellow, 30px circles when point count is between 100 and 750
            //   * Pink, 40px circles when point count is greater than or equal to 750
            "circle-color": [
                "step",
                ["get", "point_count"],
                "#51bbd6",
                50,
                "#f1f075",
                200,
                "#f28cb1"
            ],
            "circle-radius": [
                "step",
                ["get", "point_count"],
                20,
                50,
                30,
                200,
                40
            ]
        }
    });

    map.addLayer({
        id: "cluster-count",
        type: "symbol",
        source: "toilets",
        filter: ["has", "point_count"],
        layout: {
            "text-field": "{point_count_abbreviated}",
            "text-font": ["DIN Offc Pro Medium", "Arial Unicode MS Bold"],
            "text-size": 12
        }
    });

    map.addLayer({
        id: "unclustered-point",
        type: "symbol",
        source: "toilets",
        filter: ["!", ["has", "point_count"]],
        layout: {
            "icon-image": "{icon}",
            "icon-size" : 0.5,
            "text-field": "{name}",
            "text-font": ["Open Sans Semibold", "Arial Unicode MS Bold"],
            "text-offset": [0, 0.6],
            "text-anchor": "top"
        }
    });
    // inspect a cluster on click
    map.on('click', 'clusters', function (e) {
        var features = map.queryRenderedFeatures(e.point, { layers: ['clusters'] });
        var clusterId = features[0].properties.cluster_id;
        map.getSource('toilets').getClusterExpansionZoom(clusterId, function (err, zoom) {
            if (err)
                return;

            map.easeTo({
                center: features[0].geometry.coordinates,
                zoom: zoom
            });
        });
    });

    map.on('mouseenter', 'clusters', function () {
        map.getCanvas().style.cursor = 'pointer';
    });

    map.on('mouseleave', 'clusters', function () {
        map.getCanvas().style.cursor = '';
    });

    /*DESCRIPTION*/
    // When a click event occurs on a feature in the places layer, open a popup at the
    // location of the feature, with description HTML from its properties.
    map.on('click', 'unclustered-point', function (e) {
        var coordinates = e.features[0].geometry.coordinates.slice();
        var description = "<b>Názov: " + e.features[0].properties.name + "</b><br/>" +
            "Adresa: " + e.features[0].properties.address + "<br/>" +
            "Mesto: " + e.features[0].properties.town + "<br/>" +
            "Štát: " + e.features[0].properties.state + "<br/>" +
            "Smerové číslo: " + e.features[0].properties.postcode + "<br/>" +
            "Otvorené: " + e.features[0].properties.isopen + "<br/>";

        // Ensure that if the map is zoomed out such that multiple
        // copies of the feature are visible, the popup appears
        // over the copy being pointed to.
        while (Math.abs(e.lngLat.lng - coordinates[0]) > 180) {
            coordinates[0] += e.lngLat.lng > coordinates[0] ? 360 : -360;
        }

        new mapboxgl.Popup()
            .setLngLat(coordinates)
            .setHTML(description)
            .addTo(map);
    });
    map.on('dblclick', 'unclustered-point', function (e) {
        console.log("#nearest_poi ");
        console.log(url+"/nearestpoi?longitude="+actualPosition.getLngLat().lng+"&latitude="+actualPosition.getLngLat().lat+"&distance="+distance*1000);
        // clearLayouts();
        var distance = document.getElementById("txt_nearest").value;
        jQuery.get(url+"/nearest?longitude="+actualPosition.getLngLat().lng+"&latitude="+actualPosition.getLngLat().lat+"&distance="+distance*1000, function(response) {

            map.getSource("toilets").setData(response);
        });
    });

    // Change the cursor to a pointer when the mouse is over the places layer.
    map.on('mouseenter', 'unclustered-point', function () {
        map.getCanvas().style.cursor = 'pointer';
    });

    // Change it back to a pointer when it leaves.
    map.on('mouseleave', 'unclustered-point', function () {
        map.getCanvas().style.cursor = '';
    });


    /*Fill boundareis combo */
    jQuery.get(url+"/allboundaries", function(response) {
        var boundaries = response;
        console.log(boundaries);
        var comboBox = document.getElementById('boundaries')
        var myStringArray = response;
        var arrayLength = myStringArray.length;
        for (var i = 0; i < arrayLength; i++) {
            var opt = document.createElement("option"); // Create the new element
            opt.value = myStringArray[i].id; // set the value
            opt.text = myStringArray[i].name;
            comboBox.appendChild(opt);
            console.log(myStringArray[i].id +": "+ myStringArray[i].name);
            //Do something
        }
    });

    jQuery.get(url+"/heatmapcountries", function(response) {
        var heatMapCountries = response;
        console.log(heatMapCountries);
        var arrayLength = heatMapCountries.length;
        var max = heatMapCountries[0].count;
        for (var i = 0; i < arrayLength; i++) {
            var name = heatMapCountries[i].name;
            var count = heatMapCountries[i].count;
            console.log(heatMapCountries[i].name);
            console.log(heatMapCountries[i].count);

            map.addSource(name + "-states", {
                "type": "geojson",
                "data": {
                    "type": "FeatureCollection",
                    "features": []
                },
            });
            map.addLayer({
                'id': name+"-states-layer",
                'type': 'fill',
                'source': name+"-states",
                'paint': {
                    "fill-color": "rgba(200, 100, 240, 1)",
                    "fill-opacity": ["case",
                        ["boolean", ["feature-state", "hover"], false],
                        1,
                        count/max
                    ],
                    'fill-outline-color': 'rgba(200, 100, 240, 1)'
                }
            });

        };
    });


});

map.on('locationfound', function(e) {
    alert("jupi");
});

document.querySelector('#filter').onclick = function() {
    filterstatus= !filterstatus;
    if(filterstatus){
        document.getElementById("filterstatus").innerText="Filtre: <b>ZAPNUTÉ</b>";
    }else{
        document.getElementById("filterstatus").innerText="Filtre: <b>VYPNUTÉ</b>";
    }
};

document.querySelector('#clear').onclick = function() {
    if(map.getSource("toilets") != null) {
        map.getSource("toilets").setData({
            "type": "FeatureCollection",
            "features": []
        })
    }
    if(map.getSource("boundary") != null) {
        map.getSource("boundary").setData({
            "type": "FeatureCollection",
            "features": []
        })
    }
    jQuery.get(url+"/heatmapcountries", function(response) {
        var heatMapCountries = response;
        console.log(heatMapCountries);
        var arrayLength = heatMapCountries.length;
        var max = heatMapCountries[0].count;
        for (var i = 0; i < arrayLength; i++) {
            var name = heatMapCountries[i].name;
            var count = heatMapCountries[i].count;
            if(map.getSource(name + "-states") != null) {
                map.getSource(name + "-states").setData({
                    "type": "FeatureCollection",
                    "features": []
                })
            }
        };
    });
    jQuery.get(url+"/heatmapdistricts", function(response) {
        var heatMapCountries = response;
        console.log(heatMapCountries);
        var arrayLength = heatMapCountries.length;
        var max = heatMapCountries[0].count;
        for (var i = 0; i < arrayLength; i++) {
            var name = heatMapCountries[i].name;
            var count = heatMapCountries[i].count;
            if(map.getLayer(name + "-districts-layer") != null) {
                map.removeLayer(name + "-districts-layer");
            }
            if(map.getSource(name + "-districts") != null) {
                map.removeSource(name + "-districts");
            }
        };
    });
};

document.querySelector('#nearestcount').onclick = function() {
    console.log("#nearestcount ");
    var lat =document.getElementById("latitude").innerText
    document.getElementById("longitude").innerText
    console.log(url+"/nearestcount?longitude"+actualPosition.getLngLat().lng+"&latitude"+actualPosition.getLngLat().lat+"&limit"+limit+getFilters());
    // myLayer.clearLayers();
    var limit = document.getElementById("txt_limit").value;
    jQuery.get(url+"/nearestcount?longitude="+actualPosition.getLngLat().lng+"&latitude="+actualPosition.getLngLat().lat+"&limit="+limit+getFilters(), function(response) {
        map.getSource("toilets").setData(response);
    });
};

document.querySelector('#nearest').onclick = function() {
    console.log("#nearest ");
    console.log(url+"/nearest?longitude="+actualPosition.getLngLat().lng+"&latitude="+actualPosition.getLngLat().lat+"&distance="+distance*1000);
    // clearLayouts();
    var distance = document.getElementById("txt_nearest").value;
    jQuery.get(url+"/nearest?longitude="+actualPosition.getLngLat().lng+"&latitude="+actualPosition.getLngLat().lat+"&distance="+distance*1000+getFilters(), function(response) {

        map.getSource("toilets").setData(response);
    });
};


//show state boundaries
document.querySelector('#heatmapcountriesbtn').onclick = function() {
    console.log("#heatmapcountriesbtn ");
    jQuery.get(url+"/heatmapcountries", function(response) {
        var heatMapCountries = response;
        console.log(heatMapCountries);
        var arrayLength = heatMapCountries.length;
        var max = heatMapCountries[0].count;
        for (var i = 0; i < arrayLength; i++) {
            var name = heatMapCountries[i].name;
            var count = heatMapCountries[i].count;
            console.log(heatMapCountries[i].name);
            console.log(heatMapCountries[i].count);
            jQuery.get(url+"/boundarycountriesname?name="+name+"&count="+count, function(response) {

                console.log("map.getSource("+response.name+"-states)");
                console.log(response);
                map.getSource(response.name+"-states").setData(response);
                map.on('click', response.name+"-states-layer", function (e) {
                    new mapboxgl.Popup()
                        .setLngLat(e.lngLat)
                        .setHTML(response.name+" - "+response.count)
                        .addTo(map);
                });
            });
        };
    });
};

document.querySelector('#heatmapdistrictsbtn').onclick = function() {
    console.log("#heatmapdistrictsbtn ");
    jQuery.get(url+"/heatmapdistricts", function(response) {
        var heatMapCountries = response;
        console.log(heatMapCountries);
        var arrayLength = heatMapCountries.length;
        var max = heatMapCountries[0].count;
        for (var i = 0; i < arrayLength; i++) {
            map.addSource(name + "-districts", {
                "type": "geojson",
                "data": {
                    "type": "FeatureCollection",
                    "features": []
                },
            });
            map.addLayer({
                'id': name+"-districts-layer",
                'type': 'fill',
                'source': name+"-districts",
                'paint': {
                    "fill-color": "rgba(200, 100, 240, 1)",
                    "fill-opacity": ["case",
                        ["boolean", ["feature-state", "hover"], false],
                        1,
                        count/max
                    ],
                    'fill-outline-color': 'rgba(200, 100, 240, 1)'
                }
            });
            var name = heatMapCountries[i].name;
            var count = heatMapCountries[i].count;
            console.log(heatMapCountries[i].name);
            console.log(heatMapCountries[i].count);
            jQuery.get(url+"/boundarydistrictsname?name="+name+"&count="+count, function(response) {

                console.log("map.getSource("+response.name+"-districts)");
                console.log(response);
                map.getSource(response.name+"-districts").setData(response);
                map.on('click', response.name+"-districts-layer", function (e) {
                    new mapboxgl.Popup()
                        .setLngLat(e.lngLat)
                        .setHTML(response.name+" - "+response.count)
                        .addTo(map);
                });
            });
        };
    });
};





document.querySelector('#boundariesbtn').onclick = function() {
    console.log("#boundariesbtn ");
    var comboBox = document.getElementById('boundaries');
    var id = comboBox.options[comboBox.selectedIndex].value;
    console.log("id: "+ id);
    jQuery.get(url+"/boundary?id="+id, function(response) {
        var geojson = {
            "type": "geojson",
            "data": response

        };
        if(map.getLayer("boundary") != null){
            map.removeLayer("boundary");
        }
        if(map.getSource("boundary") != null){
            map.removeSource('boundary');
        }
        map.addLayer({
            'id': 'boundary',
            'type': 'fill',
            'source': geojson,
            'layout': {},
            'paint': {
                'fill-color': '#088',
                'fill-opacity': 0.8
            }
        });
    });
};
document.querySelector('#boundariesbtn2').onclick = function() {
    console.log("#boundariesbtn2 ");
    var comboBox = document.getElementById('boundaries');
    var id = comboBox.options[comboBox.selectedIndex].value;
    console.log("id: "+ id+getFilters());
    jQuery.get(url+"/boundarypoints?id="+id+getFilters(), function(response) {
        var geojson = response;
        map.getSource("toilets").setData(geojson);
        console.log("nastavene");
    });
};

function getFilters() {
    var inputs = document.querySelectorAll("input[type='checkbox']");
    var filters = "";
    if (filterstatus == true) {
        for (var i = 0; i < inputs.length; i++) {
            filters += "&filter=" + inputs[i].name + ":" + inputs[i].checked;
        }
    }
    return filters;
}

function clearLayouts(){
    if(map.getLayer("clusters") != null){
        map.removeLayer("clusters");
    }
    if(map.getSource("clusters") != null){
        map.removeSource('clusters');
    }
    if(map.getLayer("cluster-count") != null){
        map.removeLayer("cluster-count");
    }
    if(map.getSource("cluster-count") != null){
        map.removeSource('cluster-count');
    }
    if(map.getLayer("unclustered-point") != null){
        map.removeLayer("unclustered-point");
    }
    if(map.getSource("unclustered-point") != null){
        map.removeSource('unclustered-point');
    }
    if(map.getLayer("toilets") != null){
        map.removeLayer("toilets");
    }
    if(map.getSource("toilets") != null){
        map.removeSource('toilets');
    }
}
