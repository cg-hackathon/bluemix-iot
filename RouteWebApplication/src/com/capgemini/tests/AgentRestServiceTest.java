package com.capgemini.tests;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.Test;

/**
 * 
 * @author cmammado 
 * This test checks whether the REST Service is working
 */
public class AgentRestServiceTest {

	@Test
	public void test() throws IOException {

		String strUrl = "http://localhost:9080/RouteWebApplication/postEmergencyPosition";

		try {
			URL url = new URL(strUrl);
			HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
			urlConn.connect();

			assertEquals(HttpURLConnection.HTTP_OK, urlConn.getResponseCode());
			
			InputStreamReader is = new InputStreamReader(urlConn.getInputStream());
			BufferedReader br = new BufferedReader(is);
			String content = br.readLine();
			assertEquals("REST Service is working!", content);
			
		} catch (IOException e) {
			System.err.println("Error creating HTTP connection");
			e.printStackTrace();
			throw e;

		}

	}

}
