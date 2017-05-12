var TrafficMonitor = (function(conf) {

	if (window.$ !== undefined) {
		conf = $.extend({
			map : "map",
			latitude : 51.530784,
			longitude : -0.102517,
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
	var firstClicks = {};
	
	// Routes
	var ambulanceRoutes = {};
	var carRoutes = {};

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
		emergencyRed : L.MakiMarkers.icon({
			icon : "danger",
			color : "#b70132",
			size : "m"
		}),

		emergencyYellow : L.MakiMarkers.icon({
			icon : "danger",
			color : "#FFFF00",
			size : "m"
		}),

		emergencyGreen : L.MakiMarkers.icon({
			icon : "danger",
			color : "#00FF00",
			size : "m"
		}),
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
			position : 'bottomright'
		}).addTo(map);

		map.on('click', function(e) {
			// clickAction(e.latlng.lat, e.latlng.lng);
			updateTarget(e.latlng.lat, e.latlng.lng);
		});
		map.on('zoomend', function(e) {
			map.panTo([ conf.latitude, conf.longitude ], {
				animate : false
			})
		});
	}

	function updateTarget(lat, lng) {
		var order = L.marker([ lat, lng ], {
			icon : icon.emergencyRed
		});

		firstClicks[lat + lng] = order;
		order.addTo(map);

		clickAction(lat, lng);

	}

	function refresh() {
		map._onResize();
	}

	function setClickAction(fn) {
		clickAction = fn;
	}

	function showEmergency(emergency) {

		var ambulance = ambulances[emergency.vin];
		
		if (firstClicks[emergency.latitude + emergency.longitude] !== undefined) {
			map.removeLayer(firstClicks[emergency.latitude
					+ emergency.longitude]);
			delete firstClicks[emergency.latitude + emergency.longitude];

		}

		var status = emergency.status;
		if (status == "OPEN") {
			var order = orders[emergency.emergencyId];

			if (order === undefined) {
				order = L.marker([ emergency.latitude, emergency.longitude ], {
					icon : icon.emergencyYellow
				});

				order.addTo(map);
				orders[emergency.emergencyId] = order;

			}

		} else if (status == "ONGING") {
			var order = orders[emergency.emergencyId];

			if (order === undefined) {
				order = L.marker([ emergency.latitude, emergency.longitude ], {
					icon : icon.emergencyGreen
				});
				order.addTo(map);
			} else {

				order.setIcon(icon.emergencyGreen);
			}
			orders[emergency.emergencyId] = order;
			
			var newAmbulanceIcon = L.MakiMarkers.icon({
				icon : "hospital",
				color : "#FFFF00",
				size : "l"
			});

			ambulance.setIcon(newAmbulanceIcon);
			ambulances[emergency.vin] = ambulance;

		} else if (status == "SOLVED") {
			if (orders[emergency.emergencyId] !== undefined) {
				map.removeLayer(orders[emergency.emergencyId]);
				delete orders[emergency.emergencyId];

			}
			ambulance.setIcon(icon.ambulance);
			ambulances[emergency.vin] = ambulance;
		}
	}

	function drawCircle(lat, lng, radius, emergencyID) {
		var circle = L.circle([ lat, lng ], radius).addTo(map);
		circles[emergencyID] = circle;
	}
	
	// Ambulance or Car checker
	
	function isAmbulance(car){
		if (car.vin.indexOf("ambulance") > -1) {
			return true;
		} else {
			return false;
		} 
	}
	
	
	// Routes
	
	function updateAmbulanceRoute(car) {
		
		var l = ambulanceRoutes[car.vin];
		var lineColor = 'red';
		var latlongs = car.nodes;	
		
		if (l === undefined) {
			l = L.polyline(latlongs, {color: lineColor});
			l.ts = new Date();
			l.addTo(map);
			ambulanceRoutes[car.vin] = l;
		}
		
		l.setLatLngs(latlongs);
		l.ts = new Date();
	}
	
	function updateCarRoute(car){
		var l = carRoutes[car.vin];
		var lineColor = 'blue';
		var latlongs = car.nodes;
		
		if (l === undefined) {
			l = L.polyline(latlongs, {color: lineColor});
			l.ts = new Date();
			l.addTo(map);
			carRoutes[car.vin] = l;
		}
		
		l.setLatLngs(latlongs);
		l.ts = new Date();
	}
	
	function updateRoute(car) {
		var l = {};
		var lineColor = '';
		
		if(isAmbulance(car)){
			l = ambulanceRoutes[car.vin];
			lineColor = 'red';
		} else { 
			l = carRoutes[car.vin];
			lineColor = 'blue';
		}
		
		var latlongs = car.nodes;
		
		if (l === undefined) {
			l = L.polyline(latlongs, {color: lineColor});
			l.ts = new Date();
			l.addTo(map);
			
			if(isAmbulance(car)) {
				ambulanceRoutes[car.vin] = l;
			} else {
				carRoutes[car.vin] = l;
			}
		}
		
		l.setLatLngs(latlongs);
		l.ts = new Date();
	}
	
	// line is [point, point] 
	function getDistance(line,point){
		var x0 = point[0];
		var y0 = point[1];
		
		var x1 = line[0].lat;
		var y1 = line[0].lng;
		
		var x2 = line[1].lat;
		var y2 = line[1].lng;
		
		return Math.abs((x2-x1)*(y1-y0)-(x1-x0)*(y2-y1))/Math.sqrt(Math.pow(x2-x1,2)+Math.pow(y2-y1,2));
	}
	
	function removeRouteFromPast(car) {
		var l = {};
		if(isAmbulance(car)){
			l = ambulanceRoutes[car.vin];
		} else {
			l = carRoutes[car.vin];
		}
		
		// if route is not set, skip.
		if(l === undefined) {
			return;
		}
		
		var routes = l._latlngs;
		var position = [car.latitude,car.longitude];
		
		var threshold = 0.0001;
		for(var routeIndex = 0; routeIndex < routes.length - 1; routeIndex++) {
			var d = getDistance([routes[routeIndex],routes[routeIndex+1]],position);
			if (d < threshold) {
				routes[routeIndex].lat = position[0];
				routes[routeIndex].lng = position[1];
				routes = routes.reverse();
				for(var removeIndex = 0; removeIndex < routeIndex; removeIndex++){
					routes.pop();
				}
				routes.reverse();
			}
		}
		l.setLatLngs(routes);
		
		
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
	
		// Route
		removeRouteFromPast(car);
	}

	return {
		init : init,
		setClickAction : setClickAction,
		update : update,
		showEmergency : showEmergency,
		refresh : refresh,
		updateRoute: updateRoute
	};

});
