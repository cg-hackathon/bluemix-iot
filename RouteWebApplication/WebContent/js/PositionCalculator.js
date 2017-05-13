
	
	// line is [point, point] 
	var getDistance = function(line,point){
		var x0 = point[0];
		var y0 = point[1];
		
		var x1 = line[0].lat;
		var y1 = line[0].lng;
		
		var x2 = line[1].lat;
		var y2 = line[1].lng;
		
		return Math.abs((x2-x1)*(y1-y0)-(x1-x0)*(y2-y1))/Math.sqrt(Math.pow(x2-x1,2)+Math.pow(y2-y1,2));
	};
	
	var nodeDistance = function([x1,y1],[x2,y2]){
		return Math.sqrt(Math.pow(x1-x2,2)+Math.pow(y1-y2,2));
	};
	
	var isInRange = function(line, position){
		var x1 = line[0].lat;
		var y1 = line[0].lng;
		
		var x2 = line[1].lat;
		var y2 = line[1].lng;
		
		var xb = position[0];
		var yb = position[1];
		
		// Richtungsvektor
		var xu = x2-x1;
		var yu = y2-y1;
		
		var quot = (xb*xu + yb*yu)/(Math.pow(xu,2)+Math.pow(yu,2));
		
		var xl = quot * xu;
		var yl = quot * yu;
		
		if((lineLength(x1,y1,xl,yl) > lineLength(x1,y1,x2,y2)) || (lineLength(x2,y2,xl,yl) > lineLength(x1,y1,x2,y2))){
			return false;
		}
		return true;
	};
	
	