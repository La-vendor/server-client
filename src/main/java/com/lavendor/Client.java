package com.lavendor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

public class Client {

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

        Socket socket = new Socket("localhost", 3254);
        Client client = new Client(socket);

        while (socket.isConnected()) {
            System.out.println("Please enter your user ID");
            String userId = scanner.nextLine();

            client.sendUserId(userId);
            client.listenForMessage();
            System.out.println();
            client.listenForVehicleList();
            client.listenForInsuranceOffersList();
            client.displayVehiclesAndOffers();
        }

    }

    private void displayVehiclesAndOffers() {
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
    }

    private void listenForVehicleList() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String vehicleListJSON = bufferedReader.readLine();
            vehicleList = objectMapper.readValue(vehicleListJSON, new TypeReference<List<Vehicle>>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void listenForInsuranceOffersList() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String insuranceOffersJSON = bufferedReader.readLine();
            insuranceOfferList = objectMapper.readValue(insuranceOffersJSON, new TypeReference<List<InsuranceOffer>>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendUserId(String userId) {
        try {
            bufferedWriter.write(userId);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            closeAll();
        }
    }

    public void listenForMessage() {
        String messageFromServer;

        try {
            messageFromServer = bufferedReader.readLine();
            System.out.println(messageFromServer);
        } catch (IOException e) {
            closeAll();
        }
    }

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
            e.printStackTrace();
        }
    }


}
