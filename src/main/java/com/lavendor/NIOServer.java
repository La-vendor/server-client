package com.lavendor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lavendor.model.InsuranceOffer;
import com.lavendor.model.User;
import com.lavendor.model.Vehicle;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NIOServer {

    private static final int PORT = 3254;
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    private ObjectMapper objectMapper = new ObjectMapper();

    public NIOServer() throws IOException {
        // Open selector and server socket channel
        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(PORT));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    public static void main(String[] args){
        NIOServer server;
        try {
            server = new NIOServer();
        } catch (IOException e) {
            System.err.println("An error occurred while creating the server: " + e.getMessage());
            return;
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                server.close();
            } catch (IOException e) {
                System.err.println("Error closing server during shutdown: " + e.getMessage());
            }
        }));

        try {
            server.start();
        } catch (Exception e) {
            System.err.println("An error occurred while starting the server: " + e.getMessage());
        }
    }

    public void start() throws IOException, SQLException {
        while (true) {
            selector.select();

            Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
            while (selectedKeys.hasNext()) {
                SelectionKey key = selectedKeys.next();
                selectedKeys.remove();

                if (key.isAcceptable()) {
                    handleAccept(key);
                } else if (key.isReadable()) {
                    handleRead(key);
                } else if (key.isWritable()) {
                    handleWrite(key);
                }
            }
        }
    }

    private void handleAccept(SelectionKey key) throws IOException {
        SocketChannel clientChannel = serverSocketChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);
        System.out.println("Client connected");
    }

    private void handleRead(SelectionKey key) throws IOException, SQLException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int bytesRead = clientChannel.read(buffer);

        if (bytesRead == -1) {
            closeClient(clientChannel);
            return;
        }

        buffer.flip();
        String stringUserId = new String(buffer.array()).trim();
        System.out.println("Received user ID: " + stringUserId);

        long userId = Long.parseLong(stringUserId);
        User user = null;
        List<Vehicle> vehicleList = new ArrayList<>();
        List<InsuranceOffer> insuranceOfferList = new ArrayList<>();

        try (Connection connection = DataBase.connect()) {

            if (connection != null) {

                user = getUserById(connection, userId);
                vehicleList = user != null ? getVehiclesByLogin(connection, user.getLogin()) : new ArrayList<>();
                insuranceOfferList = user != null ? getInsuranceOffersByLogin(connection, user.getLogin()) : new ArrayList<>();
            }
        }

        String welcomeMessage = user != null ? "Insurance offers for: " + user.getNick() : "User not found in the database.";
        String vehicleListJSON = objectMapper.writeValueAsString(vehicleList);
        String insuranceOfferListJSON = objectMapper.writeValueAsString(insuranceOfferList);

        key.attach(new MessageQueue(welcomeMessage,"\n", vehicleListJSON,"\n", insuranceOfferListJSON, "MSG_END"));
        key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    }

    private void handleWrite(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        MessageQueue messageQueue = (MessageQueue) key.attachment();

        if (messageQueue.hasNext()) {
            String message = messageQueue.next();
            ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
            clientChannel.write(buffer);
            System.out.println("Sent message: " + message);
        } else {
            key.cancel();
//            closeClient(clientChannel);
        }
    }

    private void closeClient(SocketChannel clientChannel) throws IOException {
        clientChannel.close();
        System.out.println("Client disconnected");
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

    public void close() throws IOException {
        if (selector != null) {
            try {
                selector.close();
            } catch (IOException e) {
                System.err.println("Error closing selector: " + e.getMessage());
            }
        }

        if (serverSocketChannel != null) {
            try {
                serverSocketChannel.close();
            } catch (IOException e) {
                System.err.println("Error closing server socket channel: " + e.getMessage());
            }
        }

        for (SelectionKey key : selector.keys()) {
            SocketChannel channel = (SocketChannel) key.channel();
            try {
                if (channel != null) {
                    channel.close();
                }
            } catch (IOException e) {
                System.err.println("Error closing client connection: " + e.getMessage());
            }
        }
    }
}