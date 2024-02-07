package com.lavendor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataBase {

    public static Connection connect() {

        String jdbcUrl = "jdbc:postgresql://localhost:5432/insurance_db";
        String username = "postgres";
        String password = "Proxy3245!";

        try {
            return DriverManager.getConnection(jdbcUrl, username, password);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void closeConnection(Connection connection){
        try{
            if(connection != null && !connection.isClosed()){
                connection.close();
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
    }



}
