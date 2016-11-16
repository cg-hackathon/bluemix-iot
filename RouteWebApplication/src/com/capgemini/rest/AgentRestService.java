package com.capgemini.rest;

import java.util.UUID;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import com.capgemini.mqtt.AgentListener;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.iotf.client.AbstractClient;

/**
 * 
 * @author cmammado
 *
 */
// Specify the path to the REST-service. Required to switch between different
// methods in different classes.
@Path("/getEmergencyLocation")
public class AgentRestService {

	private static final String CLASS_NAME = AbstractClient.class.getName();
	private static final Logger LOG = Logger.getLogger(CLASS_NAME);

	/**
	 * This method will be called each time an emergency get solved
	 * 
	 * @param msg
	 *            message which is sent to the rest service when emergency get
	 *            solved
	 */

	public AgentRestService() {

	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public void getEmergencyLocation(String msg) {

		JsonObject data = new JsonParser().parse(msg).getAsJsonObject();
		// Generate the ID of the emergency (here) to match the emergencys with
		// the regions
		String newID = UUID.randomUUID().toString();
		// Check if it is the first emergency
		Boolean emergencyFirst = data.get("isfirstEmergency").getAsBoolean();
		// Adding groupId and the emergencyId
		data.addProperty("groupId", "simulator");
		data.addProperty("newEmergencyID", newID);

		// Instances the Geospatial object
		Geospatial geo = new Geospatial();

		// Starts if it is the first emergency happened
		if (!emergencyFirst) {
			try {
				geo.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// Publish a command via IoT to the hospital device with the emergency
		AgentListener.getAgent().getClient().publishCommand("hospital", "hospital1", "emergency", data);

		// Add new region
		// FIXME: Radius anpassen / adjust radius
		try {
			geo.addRegion("region_" + newID, Double.toString(data.get("latitude").getAsDouble()),
					Double.toString(data.get("longitude").getAsDouble()), "7000");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}