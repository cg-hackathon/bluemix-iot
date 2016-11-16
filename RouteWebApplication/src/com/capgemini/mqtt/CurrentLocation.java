package com.capgemini.mqtt;

/**
 * This is a class for current locations of cars/ambulances. Each location has a
 * latitude and a longitude
 *
 * @author cmammado
 */
public class CurrentLocation {

	Double currentLatitude;
	Double currentLongitude;

	public CurrentLocation(Double latitude, Double longitude) {
		this.currentLatitude = latitude;
		this.currentLongitude = longitude;

	}

	public Double getCurrentLatitude() {
		return currentLatitude;
	}

	public void setCurrentLatitude(Double currentLatitude) {
		this.currentLatitude = currentLatitude;
	}

	public Double getCurrentLongitude() {
		return currentLongitude;
	}

	public void setCurrentLongitude(Double currentLongitude) {
		this.currentLongitude = currentLongitude;
	}

}
