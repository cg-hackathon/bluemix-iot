package com.capgemini.mqtt;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.ibm.iotf.client.AbstractClient;
import com.ibm.iotf.client.app.Command;
import com.ibm.iotf.client.app.Event;
import com.ibm.iotf.client.app.EventCallback;
import com.ibm.json.java.JSONObject;
/**
 * 
 * @author cmammado
 *
 */
public class AgentEventCallback implements EventCallback {

	private static final String CLASS_NAME = AbstractClient.class.getName();
	private static final Logger LOG = Logger.getLogger(CLASS_NAME);

	/*
	 * This hashmap contains the current location of each ambulance. The key is
	 * the vehicle identification number (vin) and the value is am ambulance
	 * object containing the vin, the latitude, the longitude and the state of
	 * ambulance
	 */
	public static Map<String, Ambulance> ambulances = new HashMap<String, Ambulance>();

	public AgentEventCallback(Agent agent) {
		super();
	}

	@Override
	public void processCommand(Command command) {
		LOG.info("Command received: " + command.getPayload());

	}

	/**
	 * This method will be called each time a device publishes events to IBM
	 * Internet of Things Foundation (IoT)
	 * 
	 * @param event
	 *            event which is sent from the Raspberry Pi to IoT (non-Javadoc)
	 * 
	 * @see com.ibm.iotf.client.app.EventCallback#processEvent(com.ibm.iotf.client.
	 *      app.Event)
	 */
	@Override
	public void processEvent(Event event) {
		// Handle only events ambulances publish
		if (event.getPayload().contains("ambulance")) {
			addAmbulance(event);
		}

	}

	/**
	 * This method finds the vin, current latitude, current longitude and state
	 * of ambulance and adds it to the hashmap of ambulances
	 * 
	 * @param event
	 *            event which is sent from Raspberry Pi to IoT
	 */
	public void addAmbulance(Event event) {
		JSONObject obj;

		try {
			// Get the payload of event
			obj = JSONObject.parse(event.getPayload());
			Map<String, String> data = (Map<String, String>) obj.get("d");
			// Get the vehicle identification number (vin) of ambulance
			String vin = data.get("vin");
			// Get the latitude of the current position of ambulance
			Double currentLatitude = Double.parseDouble(data.get("latitude"));
			// Get the longitude of the current position of ambulance
			Double currentLongitude = Double.parseDouble(data.get("longitude"));
			// Get the state of ambulance, isFree is false if the ambulance
			// is on the way to the emergency location, otherwise it is true
			boolean isFree = Boolean.parseBoolean(data.get("isFree"));

			// Add the current position of ambulance with the given id to the
			// hashmap of ambulances. Set the state of ambulance (free or not)
			if (!ambulances.containsKey(vin)) {
				Ambulance a = new Ambulance();
				a.setCurrentLocation(new CurrentLocation(currentLatitude, currentLongitude));
				a.setVin(vin);
				a.setIsFree(isFree);
				ambulances.put(vin, a);
			} else {
				Ambulance a = ambulances.get(vin);
				a.setCurrentLocation(new CurrentLocation(currentLatitude, currentLongitude));
				a.setIsFree(isFree);
				ambulances.put(vin, a);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
