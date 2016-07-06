package com.capgemini.rest;

import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;

/**
 * This class is used as an adapter to the geospatial service. To use the class 
 * create an instance by using the default construcutor and then call the appropriate methods.
 * When creating an instance the VCAP_Services environment variable will be evaluated and stored within the object.
 */
public class Geospatial {

	private static final String MQTT_SERVER ="0yngjl";
	private static final String MQTT_CLIENT_ID_INPUT = "a:"+MQTT_SERVER+":geoInput";
	private static final String MQTT_CLIENT_ID_NOTIFY = "a:"+MQTT_SERVER+":geoNotify";
	private static final String MQTT_UID = "a-0yngjl-fn8uegycq1";
	private static final String MQTT_PW = "nSbn2DYpS6qTh7Is+B";
	private static final String MQTT_NOTIFY_TOPIC = "iot-2/type/api/id/geospatial/cmd/geoAlert/fmt/jsons";
	private static final String MQTT_URI = MQTT_SERVER+".messaging.internetofthings.ibmcloud.com:1883";
	
	private static final Logger LOG = Logger.getLogger(Geospatial.class.getName());
	private static final ResponseHandler<Void> RESPONSE_HANDLER = new PrintResponeToLogHandler();

	private GeospatialAnalytics environment;

	public Geospatial() {
		// TODO: get access to environment variables
		String vcap = "";
		Gson gson = new Gson();
		JsonReader reader = new JsonReader(new StringReader(vcap));
		reader.setLenient(true);
		VCAP_Services env = gson.fromJson(reader, VCAP_Services.class);
		environment = env.geospatialAnalytics[0];
	}

	public void start() throws Exception {
		HttpClient client = createHttpClient();
		HttpPut request = new HttpPut();

		prepareRequest(request, environment.credentials.start_path);
		
		GeoMqtt geomqtt = new GeoMqtt();
		geomqtt.mqtt_client_id_input = MQTT_CLIENT_ID_INPUT + (int) Math.floor(Math.random() * 1000);
		geomqtt.mqtt_client_id_notify = MQTT_CLIENT_ID_NOTIFY + (int) Math.floor(Math.random() * 1000);
		geomqtt.mqtt_uid = MQTT_UID;
		geomqtt.mqtt_pw = MQTT_PW;
		geomqtt.mqtt_uri = MQTT_URI;
		geomqtt.mqtt_notify_topic = MQTT_NOTIFY_TOPIC;
		
		// TODO: insert messagetopic to listen to
		// geomqtt.mqtt_input_topics =
		
		// TODO: define message attributes which has to be evaluated by geospatial service
		// geomqtt.device_id_attr_name =
		// geomqtt.latitude_attr_name = 
		// geomqtt.longitude_attr_name = 

		request.setEntity(new StringEntity(new Gson().toJson(geomqtt)));

		client.execute(request, RESPONSE_HANDLER);

	}

	public void status(GeospatialAnalytics evironment) throws Exception {
		HttpClient client = createHttpClient();
		HttpGet request = new HttpGet();
		
		prepareRequest(request, environment.credentials.status_path);

		client.execute(request, RESPONSE_HANDLER);

	}

	public void stop(GeospatialAnalytics environment) throws Exception {
		HttpClient client = createHttpClient();
		HttpPut request = new HttpPut();		
		prepareRequest(request, environment.credentials.stop_path);

		client.execute(request, RESPONSE_HANDLER);
	}

	public void addRegion(String regionName, String latitude, String longtitude, String radius) throws Exception {
		JsonObject parameter = new JsonObject();
		JsonObject region = new JsonObject();

		
		region.add("region_type", new JsonPrimitive("regular"));
		// TODO: set all neccessary properties to call geospatial  

		parameter.add("regions", new JsonArray());
		parameter.get("regions").getAsJsonArray().add(region);

		HttpClient client = createHttpClient();
		HttpPut request = new HttpPut();
		prepareRequest(request, environment.credentials.add_region_path);

		request.setEntity(new StringEntity(new Gson().toJson(parameter)));

		client.execute(request, RESPONSE_HANDLER);

	}

	private void prepareRequest(HttpRequestBase request, String path) throws Exception {
		URIBuilder uriBuilder = new URIBuilder();
		uriBuilder.setHost(environment.credentials.geo_host);
		uriBuilder.setPort(Integer.parseInt(environment.credentials.geo_port));
		uriBuilder.setPath(path);
		uriBuilder.setScheme("https");
		
		request.setURI(uriBuilder.build());
		request.addHeader("Content-Type", "application/json");
	}


	private HttpClient createHttpClient() {
		CredentialsProvider provider = new BasicCredentialsProvider();
		UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(environment.credentials.userid,
				environment.credentials.password);
		provider.setCredentials(AuthScope.ANY, credentials);
		HttpClient client = HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build();
		return client;
	}

	
	
	public static class PrintResponeToLogHandler implements ResponseHandler<Void> {

		@Override
		public Void handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
			LOG.info(String.valueOf(response.getStatusLine().getStatusCode()));
			LOG.info(response.getStatusLine().getReasonPhrase());
			LOG.info(IOUtils.toString(response.getEntity().getContent()));
			return null;
		}

	}
	/**
	 * Internal classes for accessing the VCAP_Services
	 */

	public static class VCAP_Services {

		@SerializedName("cloudantNoSQLDB")
		private CloudantNoSQLDB[] cloudantNoSQLDB;
		@SerializedName("Geospatial Analytics")
		private GeospatialAnalytics[] geospatialAnalytics;

	}
	@SuppressWarnings("unused")
	public static class CloudantNoSQLDB {

		private String name;
		private String label;
		private String plan;
		@SerializedName("credentials")
		private Credentials credentials;

	}
	@SuppressWarnings("unused")
	public static class GeospatialAnalytics {

		private String name;
		private String label;
		private String plan;
		@SerializedName("credentials")
		private GeoCredentials credentials;

	}
	@SuppressWarnings("unused")
	public static class Credentials {

		private String password;
		private String username;
		private String host;
		private String port;
		private String url;

	}
	@SuppressWarnings("unused")
	public static class GeoCredentials {

		private String password;
		private String geo_host;
		private String dashboard_path;
		private String stop_path;
		private String geo_port;
		private String remove_region_path;
		private String restart_path;
		private String start_path;
		private String add_region_path;
		private String userid;
		private String status_path;
	}
	@SuppressWarnings("unused")
	public static class GeoMqtt {
		private String mqtt_uid;
		private String mqtt_pw;
		private String mqtt_uri;
		private String mqtt_input_topics;
		private String mqtt_notify_topic;
		private String device_id_attr_name;
		private String latitude_attr_name;
		private String longitude_attr_name;
		private String mqtt_client_id_input;
		private String mqtt_client_id_notify;
	}

}
