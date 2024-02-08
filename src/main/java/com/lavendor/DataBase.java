package com.lavendor;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DataBase {

    public static Connection connect() throws IOException {

        try (InputStream inputStream = DataBase.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (inputStream == null) {
                throw new IOException("Unable to find configuration file db.properties");
            }

            Properties properties = new Properties();
            properties.load(inputStream);

            String jdbcUrl = properties.getProperty("jdbcUrl");
            String username = properties.getProperty("dbUsername");
            String password = properties.getProperty("dbPassword");

            return DriverManager.getConnection(jdbcUrl, username, password);
        } catch (SQLException e) {
            System.out.println("Could not establish a connection to the database server.");
            return null;
        }
    }
}
