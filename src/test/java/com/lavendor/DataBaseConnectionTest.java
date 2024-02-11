package com.lavendor;

import com.lavendor.model.InsuranceOffer;
import com.lavendor.model.User;
import com.lavendor.model.Vehicle;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class DataBaseConnectionTest {

    Connection mockConnection;
    PreparedStatement mockStatement;
    ResultSet mockResultSet;


    @BeforeEach
    public void init(){
        // Mocking Connection, PreparedStatement, and ResultSet
        mockConnection = mock(Connection.class);
        mockStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);
    }

    @Test
    public void testGetUserByIdFromDB() throws SQLException {

        long userId = 1;
        User expectedUser = new User(userId, "John Bean", "john_bean");
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);

        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getLong("id")).thenReturn(expectedUser.getId());
        when(mockResultSet.getString("nick")).thenReturn(expectedUser.getNick());
        when(mockResultSet.getString("login")).thenReturn(expectedUser.getLogin());

        //Call tested method
        User resultUser = Server.getUserById(mockConnection, userId);

        verify(mockConnection).prepareStatement("SELECT * FROM users WHERE id = ?");
        verify(mockStatement).setLong(1, userId);

        Assertions.assertEquals(expectedUser, resultUser);

    }

    @Test
    public void testGetVehiclesByLoginFromDB() throws IOException, SQLException {

        String userLogin = "john_bean";

        List<Vehicle> expectedVehicleList = new ArrayList<>();
        Vehicle testVehicle1 = new Vehicle(1L, "john_bean", "Tata", "Nano");
        Vehicle testVehicle2 = new Vehicle(2L, "john_bean", "Aston Martin", "Cygnet");
        expectedVehicleList.add(testVehicle1);
        expectedVehicleList.add(testVehicle2);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);


        when(mockResultSet.next()).thenReturn(true, true, false);

        when(mockResultSet.getLong("id")).thenReturn(testVehicle1.getId(), testVehicle2.getId());
        when(mockResultSet.getString("brand")).thenReturn(testVehicle1.getBrand(), testVehicle2.getBrand());
        when(mockResultSet.getString("model")).thenReturn(testVehicle1.getModel(), testVehicle2.getModel());

        //Call tested method
        List<Vehicle> resultVehicleList = Server.getVehiclesByLogin(mockConnection, userLogin);

        verify(mockConnection).prepareStatement("SELECT * FROM vehicles WHERE vehicles.login = ?");
        verify(mockStatement).setString(1, userLogin);

        Assertions.assertEquals(expectedVehicleList, resultVehicleList);
    }

    @Test
    public void testGetInsuranceOffersByLogin() throws SQLException {

        String userLogin = "john_bean";

        List<InsuranceOffer> expectedInsuranceOffersList = new ArrayList<>();
        InsuranceOffer testInsuranceOffer1 = new InsuranceOffer(1, 1, "Axa", 450);
        InsuranceOffer testInsuranceOffer2 = new InsuranceOffer(2, 2, "Link4", 500);

        expectedInsuranceOffersList.add(testInsuranceOffer1);
        expectedInsuranceOffersList.add(testInsuranceOffer2);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);

        when(mockResultSet.next()).thenReturn(true,true,false);
        when(mockResultSet.getLong("id")).thenReturn(testInsuranceOffer1.getId(),testInsuranceOffer2.getId());
        when(mockResultSet.getLong("vehicle_id")).thenReturn(testInsuranceOffer1.getVehicleId(),testInsuranceOffer2.getVehicleId());
        when(mockResultSet.getString("insurer")).thenReturn(testInsuranceOffer1.getInsurer(),testInsuranceOffer2.getInsurer());
        when(mockResultSet.getFloat("price")).thenReturn(testInsuranceOffer1.getPrice(),testInsuranceOffer2.getPrice());

        //Call tested method
        List<InsuranceOffer> resultInsuranceOffers = Server.getInsuranceOffersByLogin(mockConnection,userLogin);

        verify(mockConnection).prepareStatement("SELECT * FROM insurance_offers WHERE vehicle_id IN(SELECT id FROM vehicles WHERE login = ?)");
        verify(mockStatement).setString(1, userLogin);

        Assertions.assertEquals(expectedInsuranceOffersList, resultInsuranceOffers);

    }


}