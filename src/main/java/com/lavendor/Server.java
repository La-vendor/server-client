package com.lavendor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lavendor.model.InsuranceOffer;
import com.lavendor.model.User;
import com.lavendor.model.Vehicle;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private static final int PORT = 3254;

    private ServerSocket serverSocket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;

    private Socket socket;

    User user;
    private List<Vehicle> vehicleList;
    private List<InsuranceOffer> insuranceOfferList;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        Server server = new Server(serverSocket);

        while (!serverSocket.isClosed()) {
            server.listenForClientConnection();

            while (!server.socket.isClosed()) {
                System.out.println("Waiting for client ID");
                String userId = server.listenForUserId();

                if (userId != null) {
                    server.readDataFromDB(userId);
                    server.writeMessage();
                    server.sendVehicleListToClient();
                    server.sendInsuranceOffersListToClient();
                }
            }
            server.closeSocket();
        }
    }

    public String listenForUserId() {
        String userId;
        try {
            userId = bufferedReader.readLine();
            System.out.println("Received user ID :" + userId);

        } catch (IOException e) {
            closeSocket();
            System.out.println("Client disconnected: " + e.getMessage());
            return null;
        }
        return userId;
    }

    public void listenForClientConnection() {
        try {
            socket = serverSocket.accept();
            System.out.println("Client connected");

            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        } catch (IOException e) {
            closeSocket();
        }
    }

    private void sendVehicleListToClient() {
        if (user != null) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                String vehicleListJSON = objectMapper.writeValueAsString(vehicleList);
                try {
                    bufferedWriter.write(vehicleListJSON);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                    System.out.println("Vehicles data sent to client");
                } catch (IOException e) {
                    closeSocket();
                }
            } catch (JsonProcessingException e) {
                System.err.println("An issue occurred while converting data to JSON format: " + e.getMessage());
            }
        }
    }

    private void sendInsuranceOffersListToClient() {
        if (user != null) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                String insuranceOffersListJSON = objectMapper.writeValueAsString(insuranceOfferList);
                try {
                    bufferedWriter.write(insuranceOffersListJSON);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                    System.out.println("Insurance offers data sent to client \n");
                } catch (IOException e) {
                    closeSocket();
                }
            } catch (JsonProcessingException e) {
                System.err.println("An issue occurred while converting data to JSON format: " + e.getMessage());
            }
        }
    }

    public void writeMessage() {
        if (bufferedWriter != null) {
            if (user != null) {
                try {
                    bufferedWriter.write("Insurance offers for :" + user.getNick());
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                } catch (IOException e) {
                    closeSocket();
                    System.err.println("Error writing insurance offers to the client: " + e.getMessage());
                }
            } else {
                try{
                    bufferedWriter.write("User not found in the database.");
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                }catch (IOException e) {
                    closeSocket();
                    System.err.println("Error writing user not found message to the client: " + e.getMessage());
                }
            }
        }
    }



    public void readDataFromDB(String stringUserId) {
        long userId = Long.parseLong(stringUserId);
        user = null;
        try (Connection connection = DataBase.connect()) {

            if (connection != null) {
                user = getUserById(connection, userId);
                if (user != null) {
                    System.out.println("Requested data for user: " + user.getNick());

                    vehicleList = getVehiclesByLogin(connection, user.getLogin());

                    insuranceOfferList = getInsuranceOffersByLogin(connection, user.getLogin());
                } else {
                    System.out.println("User not found in the database. \n");
                }
            }
        } catch (SQLException e) {
            System.err.println("Could not establish a connection to the database server: " + e.getMessage());
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private static User getUserById(Connection connection, long userId) {
        String loginQuery = "SELECT * FROM users WHERE id = ?";
        User user = null;
        try (PreparedStatement loginStatement = connection.prepareStatement(loginQuery)) {
            loginStatement.setLong(1, userId);
            try (ResultSet loginResult = loginStatement.executeQuery()) {
                if (loginResult.next()) {
                    user = new User();
                    user.setId(loginResult.getLong("id"));
                    user.setNick(loginResult.getString("nick"));
                    user.setLogin(loginResult.getString("login"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error in login query: " + e.getMessage());
        }
        return user;
    }

    private static List<Vehicle> getVehiclesByLogin(Connection connection, String login) {

        List<Vehicle> vehicleList = new ArrayList<>();
        String vehiclesQuery = "SELECT * FROM vehicles WHERE vehicles.login = ?";

        try (PreparedStatement vehiclesStatement = connection.prepareStatement(vehiclesQuery)) {
            vehiclesStatement.setString(1, login);

            try (ResultSet vehicleResult = vehiclesStatement.executeQuery()) {
                while (vehicleResult.next()) {
                    Vehicle vehicle = new Vehicle();
                    vehicle.setId(vehicleResult.getLong("id"));
                    vehicle.setLogin(login);
                    vehicle.setBrand(vehicleResult.getString("brand"));
                    vehicle.setModel(vehicleResult.getString("model"));
                    vehicleList.add(vehicle);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error vehicle query: " + e.getMessage());
        }
        return vehicleList;
    }

    private static List<InsuranceOffer> getInsuranceOffersByLogin(Connection connection, String login) {

        List<InsuranceOffer> insuranceOfferList = new ArrayList<>();
        String insuranceOffersQuery = "SELECT * FROM insurance_offers" +
                " WHERE vehicle_id IN(SELECT id FROM vehicles WHERE login = ?)";

        try (PreparedStatement insuranceOffersStatement = connection.prepareStatement(insuranceOffersQuery)) {
            insuranceOffersStatement.setString(1, login);

            try (ResultSet insuranceOffersResult = insuranceOffersStatement.executeQuery()) {
                while (insuranceOffersResult.next()) {
                    InsuranceOffer insuranceOffer = new InsuranceOffer();
                    insuranceOffer.setId(insuranceOffersResult.getLong("id"));
                    insuranceOffer.setVehicleId(insuranceOffersResult.getLong("vehicle_id"));
                    insuranceOffer.setInsurer(insuranceOffersResult.getString("insurer"));
                    insuranceOffer.setPrice(insuranceOffersResult.getFloat("price"));

                    insuranceOfferList.add(insuranceOffer);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error in insurance offer query: " + e.getMessage());
        }
        return insuranceOfferList;
    }

    public void closeSocket() {
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("An error occurred while closing the socket and associated streams: " + e.getMessage());
        }
    }
}
