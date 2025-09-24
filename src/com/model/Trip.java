package com.model;

/**
 * Represents a single travel itinerary.
 * This class is designed to handle both active trips with live object references
 * and historical trips loaded from data files.
 */
public class Trip {
    private String tripId;
    private Passenger passenger;
    private Driver driver;
    private Location origin;
    private Location destination;
    private TripStatus status;
    private double fare;

    // This field is specifically for storing the driver's name in historical records,
    private String driverName;

    /**
     * Constructor for creating a new, active trip request.
     * @param tripId A unique identifier for the trip.
     * @param passenger The passenger requesting the trip.
     * @param origin The starting location.
     * @param destination The ending location.
     */
    public Trip(String tripId, Passenger passenger, Location origin, Location destination) {
        this.tripId = tripId;
        this.passenger = passenger;
        this.origin = origin;
        this.destination = destination;
        this.status = TripStatus.REQUESTED; // A new trip always starts as 'REQUESTED'.
    }

    /**
     * Constructor for loading a historical trip from a data source(JSON file).
     * @param tripId The trip's unique identifier.
     * @param origin The starting location.
     * @param destination The ending location.
     * @param fare The final calculated fare for the trip.
     * @param status The final status of the trip (e.g., COMPLETED).
     * @param driverName The name of the driver who completed the trip.
     */
    public Trip(String tripId, Location origin, Location destination, double fare, TripStatus status, String driverName) {
        this.tripId = tripId;
        this.origin = origin;
        this.destination = destination;
        this.fare = fare;
        this.status = status;
        this.driverName = driverName;
        // Note: The 'passenger' and 'driver' fields will be null for historical trips.
    }

    // --- Getters ---

    public String getTripId() { return tripId; }
    public Passenger getPassenger() { return passenger; }
    public Driver getDriver() { return driver; }
    public Location getOrigin() { return origin; }
    public Location getDestination() { return destination; }
    public TripStatus getStatus() { return status; }
    public double getFare() { return fare; }

    /**
     * Gets the driver's name.
     * It intelligently returns the name from the live Driver object if the trip is active,
     * or from the stored driverName field if it's a historical trip.
     * @return The driver's name.
     */
    public String getDriverName() {
        if (driver != null) {
            return driver.getName();
        }
        return driverName;
    }

    // --- Setters ---

    /**
     * Assigns a driver to the trip and also sets the driver's name for historical purposes.
     * @param driver The Driver object to assign to the trip.
     */
    public void setDriver(Driver driver) {
        this.driver = driver;
        if (driver != null) {
            this.driverName = driver.getName();
        }
    }

    public void setStatus(TripStatus status) {
        this.status = status;
    }

    public void setFare(double fare) {
        this.fare = fare;
    }
}