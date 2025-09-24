package com.service;

import com.model.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages saving and loading of application data to/from JSON files.
 */
public class DataManager {

    private static final String DATA_DIR = "data";
    private static final String PASSENGERS_FILE = DATA_DIR + "/passengers.json";
    private static final String DRIVERS_FILE = DATA_DIR + "/drivers.json";

    public DataManager() {
        new File(DATA_DIR).mkdirs();
    }

    /**
     * Saves the current state of passengers and drivers.
     */
    public void saveData(Map<String, Passenger> passengers, Map<String, Driver> drivers) {
        savePassengers(passengers);
        saveDrivers(drivers);
        System.out.println("Data saved successfully to JSON files.");
    }

    private void savePassengers(Map<String, Passenger> passengers) {
        JSONArray passengerArray = new JSONArray();
        for (Passenger p : passengers.values()) {
            JSONObject passengerJson = new JSONObject();
            passengerJson.put("id", p.getId());
            passengerJson.put("name", p.getName());
            passengerJson.put("password", p.getPassword());

            JSONArray historyArray = new JSONArray();
            for (Trip trip : p.getTripHistory()) {
                JSONObject tripJson = new JSONObject();
                tripJson.put("tripId", trip.getTripId());
                tripJson.put("fare", trip.getFare());
                tripJson.put("status", trip.getStatus().toString());
                tripJson.put("driverName", trip.getDriverName());

                JSONObject originJson = new JSONObject();
                originJson.put("x", trip.getOrigin().getX());
                originJson.put("y", trip.getOrigin().getY());
                tripJson.put("origin", originJson);

                JSONObject destJson = new JSONObject();
                destJson.put("x", trip.getDestination().getX());
                destJson.put("y", trip.getDestination().getY());
                tripJson.put("destination", destJson);
                
                historyArray.put(tripJson);
            }
            passengerJson.put("tripHistory", historyArray);
            passengerArray.put(passengerJson);
        }

        try (FileWriter file = new FileWriter(PASSENGERS_FILE)) {
            file.write(passengerArray.toString(4));
        } catch (IOException e) {
            System.err.println("Error saving passengers to JSON: " + e.getMessage());
        }
    }

    private void saveDrivers(Map<String, Driver> drivers) {
        JSONArray driverArray = new JSONArray();
        for (Driver d : drivers.values()) {
            JSONObject driverJson = new JSONObject();
            driverJson.put("id", d.getId());
            driverJson.put("name", d.getName());
            driverJson.put("carDetails", d.getCarDetails());
            driverJson.put("available", d.isAvailable());

            JSONObject locationJson = new JSONObject();
            locationJson.put("x", d.getCurrentLocation().getX());
            locationJson.put("y", d.getCurrentLocation().getY());
            driverJson.put("currentLocation", locationJson);

            driverArray.put(driverJson);
        }

        try (FileWriter file = new FileWriter(DRIVERS_FILE)) {
            file.write(driverArray.toString(4));
        } catch (IOException e) {
            System.err.println("Error saving drivers to JSON: " + e.getMessage());
        }
    }

    public Map<String, Passenger> loadPassengers() {
        Map<String, Passenger> passengers = new HashMap<>();
        try {
            File file = new File(PASSENGERS_FILE);
            if (!file.exists()) return passengers;

            String content = new String(Files.readAllBytes(Paths.get(file.toURI())));
            JSONArray passengerArray = new JSONArray(content);

            for (int i = 0; i < passengerArray.length(); i++) {
                JSONObject passengerJson = passengerArray.getJSONObject(i);
                Passenger p = new Passenger(
                    passengerJson.getString("id"),
                    passengerJson.getString("name"),
                    passengerJson.getString("password")
                );

                if (passengerJson.has("tripHistory")) {
                    JSONArray historyArray = passengerJson.getJSONArray("tripHistory");
                    for (int j = 0; j < historyArray.length(); j++) {
                        JSONObject tripJson = historyArray.getJSONObject(j);
                        JSONObject originJson = tripJson.getJSONObject("origin");
                        JSONObject destJson = tripJson.getJSONObject("destination");

                        Trip historicalTrip = new Trip(
                            tripJson.getString("tripId"),
                            new Location(originJson.getInt("x"), originJson.getInt("y")),
                            new Location(destJson.getInt("x"), destJson.getInt("y")),
                            tripJson.getDouble("fare"),
                            TripStatus.valueOf(tripJson.getString("status")),
                            tripJson.getString("driverName")
                        );
                        p.addTripToHistory(historicalTrip);
                    }
                }
                passengers.put(p.getId(), p);
            }
        } catch (Exception e) {
            System.err.println("Error loading passengers from JSON: " + e.getMessage());
        }
        return passengers;
    }

    public Map<String, Driver> loadDrivers() {
        Map<String, Driver> drivers = new HashMap<>();
        try {
            File file = new File(DRIVERS_FILE);
            if (!file.exists()) return drivers;

            String content = new String(Files.readAllBytes(Paths.get(file.toURI())));
            JSONArray driverArray = new JSONArray(content);

            for (int i = 0; i < driverArray.length(); i++) {
                JSONObject driverJson = driverArray.getJSONObject(i);
                JSONObject locationJson = driverJson.getJSONObject("currentLocation");
                Location loc = new Location(locationJson.getInt("x"), locationJson.getInt("y"));

                Driver d = new Driver(
                    driverJson.getString("id"),
                    driverJson.getString("name"),
                    driverJson.getString("carDetails"),
                    loc
                );
                d.setAvailable(driverJson.getBoolean("available"));
                drivers.put(d.getId(), d);
            }
        } catch (Exception e) {
            System.err.println("Error loading drivers from JSON: " + e.getMessage());
        }
        return drivers;
    }
}