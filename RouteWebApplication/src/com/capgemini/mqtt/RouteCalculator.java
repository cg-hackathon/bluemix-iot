package com.capgemini.mqtt;

import java.util.Locale;
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.PointList;

/**
 * 
 * @author belmahjo
 *
 */
public class RouteCalculator {

	private PointList pointList;
	private Double distance;

	/**
	 * This method retrieves all points between 2 locations
	 * @return list of all points
	 */
	public PointList getPointList() {
		return pointList;
	}

	/**
	 * This method sets the points between 2 locations
	 * @param pointList list of points to be set
	 */
	public void setPointList(PointList pointList) {
		this.pointList = pointList;
	}

	/**
	 * This method sets the distance between 2 locations
	 * @param distance distance to be set
	 */
	public void setDistance(Double distance) {
		this.distance = distance;
	}

	
	/**
	 * This method retrieves the distance between 2 locations
	 * @return distance between 2 locations
	 */
	public Double getDistance() {
		return distance;
	}

	
	/**
	 * This method calculates the route between 2 locations
	 * @param latFrom latitude of start position
	 * @param lonFrom longitude of start position
	 * @param latTo latitude of end position
	 * @param lonTo longitude of end position
	 */
	public void calcuateRoute(double latFrom, double lonFrom, double latTo, double lonTo) {
		// create singleton
		GraphHopper hopper = new GraphHopper().forServer();

		// store graphhopper files
	    hopper.setGraphHopperLocation(AgentListener.graphhopperPath);
		
		hopper.setEncodingManager(new EncodingManager("car"));

		// now this can take minutes if it imports or a few seconds for loading
		// of course this is dependent on the area you import
		hopper.importOrLoad();

		// simple configuration of the request object, see the
		// GraphHopperServlet class for more possibilities.

		GHRequest req = new GHRequest(latFrom, lonFrom, latTo, lonTo).setWeighting("fastest").setVehicle("car")
				.setLocale(Locale.UK);
		GHResponse rsp = hopper.route(req);

		// first check for errors
		if (rsp.hasErrors()) {
			// return null;
		}
		else
		{
			this.setPointList(rsp.getPoints());
			this.setDistance(rsp.getDistance());
		}

		

	}

}
