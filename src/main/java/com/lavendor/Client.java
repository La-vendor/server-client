package com.lavendor;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private Socket clientSocket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;

    public Client(Socket socket){
        try{
            this.clientSocket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }catch (IOException e){
            closeAll();
        }
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);

        Socket socket = new Socket("localhost",3254);
        Client client = new Client(socket);

        while(socket.isConnected()){
            System.out.println("Please enter your user ID");
            String userId = scanner.nextLine();

            client.sendUserId(userId);
            client.listenForMessage();
        }

    }

    public void sendUserId(String userId){
        try {
            bufferedWriter.write(userId);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        }catch (IOException e){
            closeAll();
        }
    }

    public void listenForMessage(){
        String messageFromServer;

        try{
            messageFromServer = bufferedReader.readLine();
            System.out.println(messageFromServer);
        }catch (IOException e){
            closeAll();
        }
    }

    private void closeAll() {
        try {
            if (clientSocket != null) {
                clientSocket.close();
            }
            if(bufferedReader != null){
                bufferedReader.close();
            }
            if(bufferedWriter != null){
                bufferedWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
