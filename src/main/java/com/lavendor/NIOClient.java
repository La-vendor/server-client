package com.lavendor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lavendor.model.InsuranceOffer;
import com.lavendor.model.Vehicle;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Scanner;

public class NIOClient {

    private SocketChannel clientSocketChannel;
    private ObjectMapper objectMapper = new ObjectMapper();

    String initialMessage;
    private List<Vehicle> vehicleList;
    private List<InsuranceOffer> insuranceOfferList;

    public NIOClient(SocketChannel socketChannel) {
        this.clientSocketChannel = socketChannel;
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);

        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("localhost", 3254));
        socketChannel.configureBlocking(false);

        NIOClient client = new NIOClient(socketChannel);

        while (socketChannel.isConnected()) {
            System.out.println("Please enter your user ID");
            String userId = scanner.nextLine();

            client.sendUserId(userId);
            client.receiveData();
        }
    }

    public void sendUserId(String userId) {
        System.out.println("22");
        try {
            ByteBuffer buffer = ByteBuffer.wrap(userId.getBytes());
            clientSocketChannel.write(buffer);
        } catch (IOException e) {
            closeAll();
            System.err.println("Error sending user ID to the client: " + e.getMessage());
        }
    }

    private void receiveData() {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(4096);
            StringBuilder messageBuilder = new StringBuilder();
            boolean receiving = true;
            while (receiving) {
                int bytesRead = clientSocketChannel.read(buffer);

                if (bytesRead == -1) {
                    System.out.println("Server connection closed.");
                    break;
                }

                buffer.flip();
                while (buffer.hasRemaining()) {
                    byte b = buffer.get();
                    messageBuilder.append((char) b);

                    if (messageBuilder.toString().contains("MSG_END")){
                        processMessage(messageBuilder.toString());
                        messageBuilder.setLength(0);
                        buffer.clear();
                        System.out.println("11");
                        receiving = false;
                        break;
                    }
                }
                buffer.clear();
            }
        } catch (IOException e) {
            closeAll();
            System.err.println("Error receiving data from the server: " + e.getMessage());
        }
    }

    private void processMessage(String message) {

        if (message.contains("Insurance offers for")) {
            try {
                String[] messages = message.split("\n");
                initialMessage = messages[0];
                vehicleList = objectMapper.readValue(messages[1], new TypeReference<>() {
                });
                insuranceOfferList = objectMapper.readValue(messages[2], new TypeReference<>() {
                });
                System.out.println(initialMessage);
                displayVehiclesAndOffers();
            } catch (IOException e) {
                System.err.println("Error processing received data: " + e.getMessage());
            }
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

    private void closeAll() {
        try {
            if (clientSocketChannel != null) {
                clientSocketChannel.close();
            }
        } catch (IOException e) {
            System.err.println("An error occurred while closing the socket channel: " + e.getMessage());
        }
    }
}

