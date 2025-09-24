package com.service;

import com.model.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages all core operations of the taxi service.
 */
public class TaxiManager {
    private Map<String, Passenger> passengers;
    private Map<String, Driver> drivers;
    private List<Trip> trips = new ArrayList<>();
    private DataManager dataManager = new DataManager();
    private AtomicInteger passengerCounter;

    public TaxiManager() {
        this.passengers = dataManager.loadPassengers();
        this.drivers = dataManager.loadDrivers();

        // Initialize passenger counter based on loaded data
        int maxId = 0;
        for (String id : this.passengers.keySet()) {
            try {
                int numericId = Integer.parseInt(id.substring(1));
                if (numericId > maxId) {
                    maxId = numericId;
                }
            } catch (NumberFormatException e) {
                // Ignore IDs that don't fit the "P<number>" format
            }
        }
        this.passengerCounter = new AtomicInteger(maxId);


        if (this.drivers.isEmpty()) {
            System.out.println("No drivers file found. Initializing with default drivers.");
            drivers.put("D1", new Driver("D1", "Ali", "Peugeot 405 - White", new Location(3, 5)));
            drivers.put("D2", new Driver("D2", "Reza", "Pride - Black", new Location(10, 8)));
            drivers.put("D3", new Driver("D3", "Maryam", "Tiba 2 - Red", new Location(1, 1)));
        }
    }

    public void saveData() {
        dataManager.saveData(passengers, drivers);
    }


    public Passenger registerPassenger(String name, String password) {
        // Check if a passenger with the same name already exists (case-insensitive)
        for (Passenger existingPassenger : passengers.values()) {
            if (existingPassenger.getName().equalsIgnoreCase(name)) {
                System.out.println("Error: A passenger with the name '" + name + "' already exists.");
                return null; // Return null to indicate failure
            }
        }

        // If the name is unique, proceed with registration
        String id = "P" + passengerCounter.incrementAndGet();
        Passenger passenger = new Passenger(id, name, password);
        passengers.put(id, passenger);
        System.out.println("Passenger " + name + " registered successfully with ID: " + id);
        return passenger;
    }

    public Passenger loginPassenger(String name, String password) {
        // Find passenger by name
        for (Passenger passenger : passengers.values()) {
            if (passenger.getName().equalsIgnoreCase(name) && passenger.getPassword().equals(password)) {
                return passenger; // Return the found passenger
            }
        }
        return null; // Return null if no match is found
    }

    public Trip requestTrip(Passenger passenger, Location origin, Location destination) {
        // Define the maximum allowed distance for a single trip.
        final double MAX_TRIP_DISTANCE = 500.0;
        
        //Check the distance before proceeding ---
        double tripDistance = origin.distanceTo(destination);
        if (tripDistance > MAX_TRIP_DISTANCE) {
            System.out.printf("Error: Trip distance of %.1f units is too long (max is %.0f).\n", tripDistance, MAX_TRIP_DISTANCE);
            System.out.println("Please choose a destination closer to your origin.");
            return null; // Reject the trip request
        }

        String tripId = "T" + (System.currentTimeMillis() / 1000);
        Trip newTrip = new Trip(tripId, passenger, origin, destination);

        Driver assignedDriver = findNearestDriver(origin);
        if (assignedDriver != null) {
            assignedDriver.setAvailable(false);
            newTrip.setDriver(assignedDriver);
            newTrip.setStatus(TripStatus.IN_PROGRESS);
            System.out.println("Trip requested. Driver " + assignedDriver.getName() + " assigned.");
        } else {
            System.out.println("No available drivers at the moment. Please try again later.");
            return null;
        }

        trips.add(newTrip);
        return newTrip;
    }

    private Driver findNearestDriver(Location location) {
        Driver nearestDriver = null;
        double minDistance = Double.MAX_VALUE;

        for (Driver driver : drivers.values()) {
            if (driver.isAvailable()) {
                double distance = driver.getCurrentLocation().distanceTo(location);
                if (distance < minDistance) {
                    minDistance = distance;
                    nearestDriver = driver;
                }
            }
        }
        return nearestDriver;
    }


    public void endTrip(Trip trip) {
        if (trip == null || trip.getStatus() != TripStatus.IN_PROGRESS) {
            System.out.println("This trip cannot be ended.");
            return;
        }
        
        // Calculate currency
        double distance = trip.getOrigin().distanceTo(trip.getDestination());
        double baseRate = 30000;  // Base fare
        double perUnitDistanceRate = 5000; // Toman per unit of distance
        
        // Calculate the final fare
        double fare = baseRate + (distance * perUnitDistanceRate);
        
        trip.setFare(fare);
        trip.setStatus(TripStatus.COMPLETED);

        Driver driver = trip.getDriver();
        driver.setAvailable(true);
        driver.setCurrentLocation(trip.getDestination());
        
        // Add the completed trip to the passenger's history
        trip.getPassenger().addTripToHistory(trip);
        
        System.out.printf("Trip %s ended. Fare: %.0f Toman\n", trip.getTripId(), fare);
    }
    /**
     * Cancels a trip that is currently in progress.
     * This sets the trip's status to CANCELLED and makes the driver available again.
     * @param trip The trip to be cancelled.
     */
    public void cancelTrip(Trip trip) {
        // A trip can only be cancelled if it exists and is in progress.
        if (trip == null || trip.getStatus() != TripStatus.IN_PROGRESS) {
            System.out.println("This trip cannot be cancelled.");
            return;
        }

        trip.setStatus(TripStatus.CANCELLED);

        // Make the assigned driver available again.
        Driver driver = trip.getDriver();
        if (driver != null) {
            driver.setAvailable(true);
            System.out.println("Driver " + driver.getName() + " is now available.");
        }
        // The cancelled trip remains in the passenger's history.


        System.out.println("Trip " + trip.getTripId() + " has been successfully cancelled.");
    }

    /**
     * Finds the most recent trip for a passenger that is still in progress.
     * @param passenger The passenger whose active trip is to be found.
     * @return The active Trip object, or null if none is found.
     */
    public Trip findActiveTripForPassenger(Passenger passenger) {
        // Search backwards to find the most recent trip first.
        for (int i = trips.size() - 1; i >= 0; i--) {
            Trip trip = trips.get(i);
            if (trip.getPassenger().equals(passenger) && trip.getStatus() == TripStatus.IN_PROGRESS) {
                return trip;
            }
        }
        return null; // No active trip found
    }
}