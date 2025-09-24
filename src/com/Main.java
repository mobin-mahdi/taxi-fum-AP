package com;

import com.model.*;
import com.service.TaxiManager;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

/**
 * The main entry point for the console application.
 * Implements a state-based menu system for a clearer user flow.
 */
public class Main {
    private static TaxiManager taxiManager = new TaxiManager();
    private static Scanner scanner = new Scanner(System.in);
    private static Passenger currentPassenger = null;

    public static void main(String[] args) {
        // The main application loop.
        while (true) {
            if (currentPassenger == null) {
                showLoginMenu();
            } else {
                showInitialPassengerMenu();
            }
        }
    }

    /**
     * Displays the first menu for logging in or registering.
     */
    private static void showLoginMenu() {
        System.out.println("\n--- Welcome to Online Taxi Service ---");
        System.out.println("1. Register as a new Passenger");
        System.out.println("2. Login");
        System.out.println("3. Save and Exit");
        System.out.print("Choose an option: ");
        try {
            int choice = scanner.nextInt();
            scanner.nextLine(); //newline

            switch (choice) {
                case 1:
                    handleRegistration();
                    break;
                case 2:
                    handleLogin();
                    break;
                case 3:
                    taxiManager.saveData();
                    System.out.println("Exiting...");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid option. Please enter a number between 1 and 3.");
            }
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter a number.");
            scanner.nextLine(); // Clear the invalid input
        }
    }

    /**
     * Displays the main menu for a logged-in passenger.
     */
    private static void showInitialPassengerMenu() {
        System.out.println("\n--- Main Menu ---");
        System.out.println("Welcome, " + currentPassenger.getName());
        System.out.println("1. Request a new Trip");
        System.out.println("2. View Trip History");
        System.out.println("3. Logout");
        System.out.print("Choose an option: ");
        try {
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    handleRequestTrip();
                    break;
                case 2:
                    handleViewHistory();
                    break;
                case 3:
                    currentPassenger = null;
                    System.out.println("Logged out successfully.");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter a number.");
            scanner.nextLine();
        }
    }

    /**
     * Handles the process of requesting a trip. If successful, it transitions
     * to the active trip menu.
     */
    private static void handleRequestTrip() {
        try {
            System.out.print("Enter origin X coordinate: ");
            int x1 = scanner.nextInt();
            System.out.print("Enter origin Y coordinate: ");
            int y1 = scanner.nextInt();
            Location origin = new Location(x1, y1);

            System.out.print("Enter destination X coordinate: ");
            int x2 = scanner.nextInt();
            System.out.print("Enter destination Y coordinate: ");
            int y2 = scanner.nextInt();
            Location destination = new Location(x2, y2);
            scanner.nextLine(); 

            if (origin.equals(destination)) {
                System.out.println("Error: Origin and destination cannot be the same.");
                return;
            }

            Trip activeTrip = taxiManager.requestTrip(currentPassenger, origin, destination);

            // If a driver was found and the trip is now active, show the active trip menu.
            if (activeTrip != null) {
                showActiveTripMenu(activeTrip);
            }
        } catch (InputMismatchException e) {
            System.out.println("Invalid coordinate. Please enter numbers only.");
            scanner.nextLine();
        }
    }

    /**
     * Displays a dedicated menu for managing an IN_PROGRESS trip.
     * This menu will loop until the trip is either cancelled or ended.
     * @param activeTrip The trip that is currently in progress.
     */
    private static void showActiveTripMenu(Trip activeTrip) {
        while (true) {
            System.out.println("\n--- Active Trip Menu ---");
            System.out.println("Trip with driver " + activeTrip.getDriverName() + " is in progress.");
            System.out.println("1. End Active Trip");

            // We show cancel trip just if the trip is in progress
            if(activeTrip.getStatus() == TripStatus.IN_PROGRESS){
                System.out.println("2. Cancel Active Trip");
            }

            System.out.print("Choose an option: ");
            try {
                int choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1:
                        taxiManager.endTrip(activeTrip);
                        System.out.println("Your trip has ended. Returning to main menu.");
                        return; // Exit this menu and go back to the initial passenger menu
                    case 2:
                        if(activeTrip.getStatus() == TripStatus.IN_PROGRESS){
                            taxiManager.cancelTrip(activeTrip);
                            System.out.println("Your trip has been cancelled. Returning to main menu.");
                            return; // Exit this menu
                        }else{
                            System.out.println("Invalid option.");
                            break;
                        }

                    default:
                        System.out.println("Invalid option. Please choose 1 or 2.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine();
            }
        }
    }

    private static void handleViewHistory() {
        System.out.println("\n--- Your Trip History ---");
        List<Trip> history = currentPassenger.getTripHistory();
        if (history.isEmpty()) {
            System.out.println("You have no past trips.");
        } else {
            for (Trip trip : history) {
                // Update the printf format to show Toman and use the correct driver name method
                System.out.printf("Trip ID: %s, Driver: %s, From: %s, To: %s, Fare: %.0f Toman, Status: %s\n",
                    trip.getTripId(),
                    trip.getDriverName(), 
                    trip.getOrigin().toString(),
                    trip.getDestination().toString(),
                    trip.getFare(),
                    trip.getStatus());
            }
        }
    }
    private static void handleRegistration() {
        String name;
        while (true) {
            System.out.print("Enter your name: ");
            name = scanner.nextLine();
            if (name == null || name.trim().isEmpty()) {
                System.out.println("Name cannot be empty.");
            } else {
                break;
            }
        }

        String password;
        while (true) {
            System.out.print("Enter a password: ");
            password = scanner.nextLine();
            if (password == null || password.trim().isEmpty()) {
                System.out.println("Password cannot be empty.");
            } else {
                break;
            }
        }

        Passenger newPassenger = taxiManager.registerPassenger(name, password);
        if (newPassenger == null) {
            System.out.println("Registration failed. Please try again with a different name.");
        }
    }

    private static void handleLogin() {
        System.out.print("Enter your Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter your password: ");
        String password = scanner.nextLine();

        if (name.trim().isEmpty() || password.trim().isEmpty()) {
            System.out.println("Name or Password cannot be empty.");
            return;
        }

        currentPassenger = taxiManager.loginPassenger(name, password);
        if (currentPassenger == null) {
            System.out.println("Login failed. Invalid name or password.");
        } else {
            System.out.println("Login successful!");
        }
    }
}