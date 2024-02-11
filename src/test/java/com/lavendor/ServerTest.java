package com.lavendor;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.ServerSocket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

class ServerTest {

    private BufferedReader mockBufferedReader;
    private BufferedWriter mockBufferedWriter;
    private ServerSocket serverSocket;
    private Server server;

    @BeforeEach
    public void init() throws IOException {
        serverSocket = new ServerSocket(1234);
        mockBufferedReader = mock(BufferedReader.class);
        mockBufferedWriter = mock(BufferedWriter.class);

        server = new Server(serverSocket,mockBufferedReader, mockBufferedWriter);
    }

    @AfterEach
    public void finish() throws IOException {
        serverSocket.close();
    }

    @Test
    public void testListenForUserId() throws IOException {
        String expectedUserId = "123";
        when(mockBufferedReader.readLine()).thenReturn(expectedUserId);

        //Call tested method
        String resultUserId = server.listenForUserId();

        verify(mockBufferedReader).readLine();

        assertEquals(expectedUserId, resultUserId);
    }

    @Test
    public void testListenForUserIdIOException() throws IOException {
        when(mockBufferedReader.readLine()).thenThrow(new IOException("Expected IOException"));

        //Call tested method
        String resultUserId = server.listenForUserId();

        assertNull(resultUserId);
    }

}