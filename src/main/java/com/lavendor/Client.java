package com.lavendor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lavendor.model.InsuranceOffer;
import com.lavendor.model.Vehicle;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

public class Client {
    private static final int PORT = 3254;

    private Socket clientSocket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;

    private List<Vehicle> vehicleList;
    private List<InsuranceOffer> insuranceOfferList;

    public Client(Socket socket) {
        try {
            this.clientSocket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            closeAll();
        }
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);

        // Connect to the server
        Socket socket = new Socket("localhost", PORT);
        Client client = new Client(socket);

        // Continuously interact with the server
        while (socket.isConnected()) {
            System.out.println("Please enter your user ID");
            String userId = scanner.nextLine();

            // Send user ID to the server
            client.sendUserId(userId);

            // Listen for server response and process data
            if (client.listenForMessage()) {
                client.listenForVehicleList();
                client.listenForInsuranceOffersList();
                client.displayVehiclesAndOffers();
            }
        }
    }

    // Send user ID to the server
    public void sendUserId(String userId) {
        try {
            bufferedWriter.write(userId);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            closeAll();
            System.err.println("Error sending user ID to the client: " + e.getMessage());
        }
    }

    // Listen for a message from the server
    public boolean listenForMessage() {
        String messageFromServer;

        try {
            messageFromServer = bufferedReader.readLine();
            System.out.println(messageFromServer + "\n");
            return messageFromServer.contains("Data for :");
        } catch (IOException e) {
            closeAll();
            System.err.println("Error listening for message from the server: " + e.getMessage());
            return false;
        }
    }

    // Listen for and deserialize the vehicle list from the server
    private void listenForVehicleList() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String vehicleListJSON = bufferedReader.readLine();
            vehicleList = objectMapper.readValue(vehicleListJSON, new TypeReference<>() {
            });
        } catch (Exception e) {
            System.err.println("An error occurred while processing the JSON representation of the vehicle list: " + e.getMessage());

        }
    }

    // Listen for and deserialize the insurance offers list from the server
    private void listenForInsuranceOffersList() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String insuranceOffersJSON = bufferedReader.readLine();
            insuranceOfferList = objectMapper.readValue(insuranceOffersJSON, new TypeReference<>() {
            });
        } catch (Exception e) {
            System.err.println("An error occurred while processing the JSON representation of the insurance offers list: " + e.getMessage());
        }
    }
    // Display vehicles and their associated insurance offers
    private void displayVehiclesAndOffers() {
        if(!vehicleList.isEmpty()){
            for (Vehicle vehicle : vehicleList) {
                System.out.println("     Brand: " + vehicle.getBrand() + ", Model: " + vehicle.getModel());
                boolean offersCheck = false;
                for (InsuranceOffer insuranceOffer : insuranceOfferList) {
                    if (insuranceOffer.getVehicleId() == vehicle.getId()) {
                        System.out.println("            Insurer: " + insuranceOffer.getInsurer() + ", Price: " + insuranceOffer.getPrice());
                        offersCheck = true;
                    }
                }
                if (!offersCheck) System.out.println("            *No available insurance offers for this vehicle*");
                System.out.println();
            }
        }else{
            System.out.println("No vehicles registered for this user \n");
        }

    }
    // Close socket and associated streams
    private void closeAll() {
        try {
            if (clientSocket != null) {
                clientSocket.close();
            }
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
        } catch (IOException e) {
            System.err.println("An error occurred while closing the socket and associated streams: " + e.getMessage());
        }
    }

}
