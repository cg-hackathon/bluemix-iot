package com.capgemini.mqtt;

import java.io.File;
import java.util.Properties;
import java.util.logging.Logger;
import com.ibm.iotf.client.AbstractClient;
import com.ibm.iotf.client.app.ApplicationClient;

/**
 * 
 * @author cmammado
 *
 */
public class Agent {

	private ApplicationClient client = null;

	private static final String CLASS_NAME = AbstractClient.class.getName();
	private static final Logger LOG = Logger.getLogger(CLASS_NAME);

	public Agent(String appPropPath) {

		Properties options = ApplicationClient.parsePropertiesFile(new File(appPropPath));

		try {
			// Create a new application client
			client = new ApplicationClient(options);
			// Connect to IBM Internet of Things Foundation
			client.connect();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method retrieves the application client
	 * 
	 * @return application client
	 */
	public ApplicationClient getClient() {
		return client;
	}

	/**
	 * This method disconnects the application client from IoT
	 */
	public void disconnect() {
		try {
			if (getClient() != null) {
				getClient().disconnect();
			} else {
				LOG.info("No ApplicationClient available, no disconnect required");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
