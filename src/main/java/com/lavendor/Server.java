package com.lavendor;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Server {

    private ServerSocket serverSocket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;

    private Socket socket;


    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
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

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(3254);
        Server server = new Server(serverSocket);

        while (!serverSocket.isClosed()) {
            server.listenForClientConnection();

            while (!server.socket.isClosed()) {
                String userId = server.listenForUserId();
                server.writeMessage(userId);
                server.readDataFromDB(userId);
            }
            server.closeSocket();
        }
    }

    private void writeMessage(String message) {
        if (bufferedWriter != null) {
            try {
                bufferedWriter.write("Server received :" + message);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            } catch (IOException e) {
                closeSocket();
            }
        }
    }

    private String listenForUserId() throws IOException {
        String userId = null;
        try {
            userId = bufferedReader.readLine();
            System.out.println("Received :" + userId);

        } catch (IOException e) {
            closeSocket();
            System.out.println("Client disconnected ");
            return null;
        }
        return userId;
    }

    public void readDataFromDB(String stringUserId) {
        long userId = Long.parseLong(stringUserId);

        try (Connection connection = DataBase.connect()) {

            if (connection != null) {
//                System.out.println("Connected to Database.");
                String userLogin = getLoginByUserId(connection, userId);
                System.out.println(userLogin);
                if (userLogin != null) {
                    List<Vehicle> vehicleList = getVehiclesByLogin(connection, userLogin);
                    for(Vehicle vehicle : vehicleList) System.out.println(vehicle.toString());
                    List<InsuranceOffer> insuranceOfferList = getInsuranceOffersByLogin(connection, userLogin);
                    for(InsuranceOffer insuranceOffer : insuranceOfferList) System.out.println(insuranceOffer.toString());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static String getLoginByUserId(Connection connection, long userId) {
        String userLogin = null;
        String loginQuery = "SELECT login FROM users WHERE id = ?";

        try (PreparedStatement loginStatement = connection.prepareStatement(loginQuery)) {
            loginStatement.setLong(1, userId);
            try (ResultSet loginResult = loginStatement.executeQuery()) {
                if (loginResult.next()) {
                    userLogin = loginResult.getString("login");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error in login statement");
            e.printStackTrace();
        }
        return userLogin;
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
                    vehicle.setBrand(vehicleResult.getString("brand"));
                    vehicle.setModel(vehicleResult.getString("model"));
                    vehicleList.add(vehicle);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error vehicle statement");
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
            System.out.println("Error in insurance offer statement");
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
            e.printStackTrace();
        }
    }
}
