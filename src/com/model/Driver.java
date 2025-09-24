package com.model;

/**
 * Represents a driver in the system.
 */
public class Driver extends User {
    private String carDetails;
    private Location currentLocation;
    private boolean available;

    public Driver(String id, String name, String carDetails, Location initialLocation) {
        super(id, name, "driver_pass");
        this.carDetails = carDetails;
        this.currentLocation = initialLocation;
        this.available = true;
    }

    public Location getCurrentLocation() { return currentLocation; }
    public boolean isAvailable() { return available; }
    public String getCarDetails() { return carDetails; }

    public void setAvailable(boolean available) { this.available = available; }
    public void setCurrentLocation(Location currentLocation) { this.currentLocation = currentLocation; }
}