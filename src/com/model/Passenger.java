package com.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a passenger who can request trips.
 */
public class Passenger extends User {
    private List<Trip> tripHistory;

    public Passenger(String id, String name, String password) {
        super(id, name, password);
        this.tripHistory = new ArrayList<>();
    }

    public void addTripToHistory(Trip trip) {
        this.tripHistory.add(trip);
    }

    public List<Trip> getTripHistory() {
        return tripHistory;
    }
}