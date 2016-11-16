package com.capgemini.rest;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.iotf.client.AbstractClient;

/**
 * 
 * @author cmammado
 *
 */
// Specify the path to the REST-service
@Path("/removeRegion")
public class GeoRegionService {

	private static final String CLASS_NAME = AbstractClient.class.getName();
	private static final Logger LOG = Logger.getLogger(CLASS_NAME);

	/**
	 * This method will be called each time an emergency happens
	 * 
	 * @param msg
	 *            message which is sent to the rest service by clicking on the
	 *            map
	 * @return
	 */

	public GeoRegionService() {

	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public void removeRegion(String msg) {

		JsonObject data = new JsonParser().parse(msg).getAsJsonObject();
		// Get the emergency ID to remove the correct region
		String emergencyID = data.get("eID").getAsString();
		// Instances the Geospatial object
		Geospatial geo = new Geospatial();

		// Remove the region from Geospatial
		try {
			geo.removeRegion("region_" + emergencyID);
			// Print the status of regions to the server console
			geo.status();

		} catch (Exception e) {

			e.printStackTrace();
		}

	}

}
