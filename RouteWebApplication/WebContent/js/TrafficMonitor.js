var TrafficMonitor = (function(conf) {

	if (window.$ !== undefined) {
		conf = $.extend({
			map : "map",
			latitude : 51.530784,  
			longitude :-0.102517, 
			zoom : 15,
			zoomMinus : 0,
			zoomPlus : 0,
			controls : false
		}, conf);
	}

	var map;
	var cars = {};
	var ambulances = {};
	
	var orders = {};
	var circles = {};
	

	var icon = {
		car : L.MakiMarkers.icon({
			icon : "car",
			color : "#0098cc",
			size : "s"
		}),
		alarmed : L.MakiMarkers.icon({
			icon : "fire-station",
			color : "#691e7c",
			size : "m"
		}),
		ambulance : L.MakiMarkers.icon({
			icon : "hospital",
			color : "#b70132",
			size : "l"
		}),
		emergency : L.MakiMarkers.icon({
			icon : "danger",
			color : "#b70132",
			size : "m"
		})
	};

	function init() {

		var mapConf = (conf.controls) ? {
			zoomControl : false,
			zoomAnimation : true
		} : {
			dragging : false,
			zoomControl : false,
			zoomAnimation : false,
			fadeAnimation : false
		};
		map = new L.Map(conf.map, mapConf);

		var osm = new L.TileLayer(
				'http://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}.png',
				{
					minZoom : conf.zoom - conf.zoomMinus,
					maxZoom : conf.zoom + conf.zoomPlus,
					attribution : '<a href="http://openstreetmap.org">OpenStreetMap</a>',
					detectRetina : true
				});

		map.setView(new L.LatLng(conf.latitude, conf.longitude), conf.zoom);
		map.addLayer(osm);
		
		L.control.zoom({
		     position:'bottomright'
		}).addTo(map);
		
		
		map.on('click', function(e) {
			clickAction(e.latlng.lat, e.latlng.lng);
		});
		map.on('zoomend', function(e) {
			map.panTo([ conf.latitude, conf.longitude ], {
				animate : false
			})
		});
	}

	function refresh() {
		map._onResize();
	}

	function setClickAction(fn) {
		clickAction = fn;
	}

	function setEmergency(emergency) {

		var order = orders[emergency.vin];

		if (order === undefined) {
			order = L.marker([ emergency.emergencyLatitude,
					emergency.emergencyLongitude ], {
				icon : icon.emergency
			});

			order.addTo(map);
			orders[emergency.vin] = order;

		}

	}

	function drawCircle(lat, lng, radius, emergencyID){	
		var circle = L.circle([lat, lng], radius).addTo(map);
		circles[emergencyID] = circle;		
	}
	
	
	function updateAmbulance(car) {		
		
		var c = ambulances[car.vin];	
		if (c === undefined) {
			c = L.Marker.movingMarker([ [ car.latitude, car.longitude ] ], [],
					{
						icon : icon.ambulance
					});
		
			c.ts = new Date();
			c.addTo(map);
			ambulances[car.vin] = c;
		}
			
		c.moveTo([ car.latitude, car.longitude ], (new Date() - c.ts));
		c.ts = new Date();
		
		if (car.isFree == "true") {
			c.setIcon(icon.ambulance);
			
			if (orders[car.vin] !== undefined) {
				map.removeLayer(orders[car.vin]);
				delete orders[car.vin];
			}
			

		} else {
			var newAmbulanceIcon = L.MakiMarkers.icon({
				icon : "hospital",
				color : "#FFFF00",
				size : "l"
			});
			
			c.setIcon(newAmbulanceIcon);
			
		}
	}

	function updateCar(car) {
		var c = cars[car.vin];
		if (c === undefined) {
			c = L.Marker.movingMarker([ [ car.latitude, car.longitude ] ], [],
					{
						icon : icon.car
					});
		
			c.ts = new Date();
			c.addTo(map);
			cars[car.vin] = c;
		}
		c.moveTo([ car.latitude, car.longitude ], (new Date() - c.ts));
		c.ts = new Date();

	}

	

	function update(car) {
		if (car.vin.indexOf("ambulance") > -1) {
			updateAmbulance(car);
		} else {
			updateCar(car);
		}
	}

	return {
		init : init,
		setClickAction : setClickAction,
		update : update,
		setEmergency: setEmergency,
		refresh : refresh
	};

});
