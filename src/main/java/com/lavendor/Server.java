package com.lavendor;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Server {

    private ServerSocket serverSocket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;

    private Socket socket;

    private long userId;


    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void startServer() {
        try {
            while (!serverSocket.isClosed()) {
                this.socket = serverSocket.accept();
                System.out.println("Client connected");

                this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                this.userId = Long.parseLong(bufferedReader.readLine());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void closeSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(3254);
        Server server = new Server(serverSocket);
        server.startServer();
    }

    public void readDataFromDB() {
        try (Connection connection = DataBase.connect()) {
            System.out.println("Connected to Database.");
            String userLogin = getLoginByUserId(connection, userId);
            List<Vehicle> vehicleList = getVehiclesByLogin(connection,userLogin);
            List<InsuranceOffer> insuranceOfferList = getInsuranceOffersByLogin(connection, userLogin);


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
            System.out.println("Error during login prepare statement");
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
            e.printStackTrace();
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
            e.printStackTrace();
        }
        return insuranceOfferList;
    }
}
