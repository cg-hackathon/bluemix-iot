package com.capgemini.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.capgemini.mqtt.AgentEventCallback;
import com.capgemini.mqtt.AgentListener;
import com.capgemini.mqtt.Ambulance;
import com.capgemini.mqtt.CurrentLocation;
import com.capgemini.mqtt.RouteCalculator;
import com.google.gson.JsonObject;
import com.ibm.iotf.client.AbstractClient;
import com.ibm.json.java.JSONObject;

/**
 * 
 * @author cmammado
 *
 */
//Specify the path to the REST-service
@Path("/")
public class AgentRestService {

	private static final String CLASS_NAME = AbstractClient.class.getName();
	private static final Logger LOG = Logger.getLogger(CLASS_NAME);

	private JSONObject emergencyLocation;
	// Latitude of emergency location
	private Double emergencyLatitude;
	// Longitude of emergency location
	private Double emergencyLongitude;
	// Vehicle identification number (vin) of the closest ambulance to the
	// emergency location
	private String closestAmbulance;

	private int emergencyID = 0;
	// private boolean freeAmbulance;

	private ArrayList<HashMap<Integer, CurrentLocation>> emergencies = new ArrayList<HashMap<Integer, CurrentLocation>>();

	/**
	 * This method will be called each time an emergency happens
	 * 
	 * @param msg
	 *            message which is sent to the rest service by clicking on the
	 *            map
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public void getEmergencyLocation(String msg) {
		// Get the location of emergency from msg
		try {
			emergencyLocation = JSONObject.parse(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Get the latitude of emergency location
		emergencyLatitude = (Double) emergencyLocation.get("latitude");
		// Get the longitude of the emergency location
		emergencyLongitude = (Double) emergencyLocation.get("longitude");
		
		// if there is a free ambulance, find the closest one to the emergency location
		if (getAmbulanceState())
			findTheClosestAmbulance(emergencyLatitude, emergencyLongitude);
		else {
			// Otherwise if there is not a free ambulance, add the emergency in a queue
			HashMap<Integer, CurrentLocation> emergency = new HashMap<Integer, CurrentLocation>();
			emergencyID++;
			emergency.put(emergencyID, new CurrentLocation(emergencyLatitude, emergencyLongitude));
			emergencies.add(emergency);

			// Find the closest ambulance for all emergencies in the queue
			while (emergencies.size() > 0) {
				// Check the ambulance state each 5 seconds, if there are free ambulances, find the closest one to the first emergency in the queue
				while (!getAmbulanceState()) {
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				}
				//Find the closest ambulance to the first emergency in the queue
				for (Entry<Integer, CurrentLocation> entry : emergencies.get(0).entrySet()) {
					findTheClosestAmbulance(entry.getValue().getCurrentLatitude(),
							entry.getValue().getCurrentLongitude());
					// Set the state of the closest ambulance to false
					AgentEventCallback.ambulances.get(closestAmbulance).setIsFree(false);

				}
				// Remove the emergency from the queue
				emergencies.remove(0);

			}

		}

	}

	/**
	 * This method returns the state of ambulances
	 * @return true, if there is a free ambulance, otherwise false
	 */
	public boolean getAmbulanceState() {

		boolean freeAmbulance = false;
		for (Entry<String, Ambulance> ambulance : AgentEventCallback.ambulances.entrySet()) {
			if (ambulance.getValue().getIsFree().equals(true)) {
				freeAmbulance = true;
			}

		}
		return freeAmbulance;
	}

	/**
	 * This method finds which ambulance is the closest ambulance to the
	 * emergency location
	 * 
	 * @param emergencyLatitude
	 *            the latitude of the emergency location
	 * 
	 * @param emergencyLongitude
	 *            the longitude of the emergency location
	 */

	public void findTheClosestAmbulance(Double emergencyLatitude, Double emergencyLongitude) {
		List<Double> distancesBetweenAmbulanceAndEmergency = new ArrayList<Double>();
		// This hashmap contains the distances from each ambulance to the
		// emergency location
		Map<String, Double> distances = new HashMap<String, Double>();
		// This hashmap contains ambulances
		Map<String, Ambulance> ambulances = AgentEventCallback.ambulances;

		if (ambulances.isEmpty()) {
			LOG.info("No ambulance available:");
		} else {
			// Find the distance between the current location of each ambulance and
			// the emergency location
			for (Entry<String, Ambulance> ambulance : ambulances.entrySet()) {
	
				if (ambulance.getValue().getIsFree()) {
					String vin = ambulance.getKey();
					Double currentLatitude = ambulance.getValue().getCurrentLocation().getCurrentLatitude();
					Double currentLongitude = ambulance.getValue().getCurrentLocation().getCurrentLongitude();
	
					RouteCalculator routeCalculator = new RouteCalculator();
					routeCalculator.calcuateRoute(currentLatitude, currentLongitude, emergencyLatitude, emergencyLongitude);
					Double distance = routeCalculator.getDistance();
					distances.put(vin, distance);
				}
	
			}
	
			// Add the distances between each ambulance and the emergency location
			// to the list
			for (Entry<String, Double> entry : distances.entrySet()) {
				distancesBetweenAmbulanceAndEmergency.add(entry.getValue());
			}
	
			// Find the closest ambulance to the emergency location
			Object minDistance = Collections.min(distancesBetweenAmbulanceAndEmergency);
	
			// Get the vin (vehicle identification number) of the closest ambulance
			for (Entry<String, Double> entry : distances.entrySet()) {
				if (!entry.equals(null))
					if (entry.getValue() == minDistance) {
						closestAmbulance = entry.getKey();
					}
			}
	
			sendAmbulanceToEmergency();
		}
	}

	/**
	 * This method publishes a command to the device with type "RaspberryPi"
	 * The published command contains the vin of the closest
	 * ambulance, the latitude and the longitude of the emergency location and the id of the emergency location
	 */
	public void sendAmbulanceToEmergency() {
		String emergencyCommand = "emergencyCommand";
		JsonObject data = new JsonObject();

		//TODO use case 2 Create command
		//data.addProperty("", );

		//TODO use case 2 Enter your Device ID
		// Publish a command to your device containing the vin of the ambulance which must drive to the emergency location
		AgentListener.getAgent().getClient().publishCommand("RaspberryPi", "Your-Device-ID", emergencyCommand, data, 0);

	}
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getMessage(){
		return "REST Service is working!";
		
	}
	

}
