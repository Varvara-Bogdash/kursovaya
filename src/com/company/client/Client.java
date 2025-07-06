package com.company.client;
import java.io.*;
import java.net.*;
import java.util.*;

public class Client {
    private static final String SETTINGS_FILE = "settings.txt";
    private String serverAddress;
    private int serverPort;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private FileWriter logFile;
    private String username;

    public static void main(String[] args) {
        new Client().start();
    }

    public void start() {
        readSettings();

        try (BufferedReader console = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.print("Enter your name: ");
            username = console.readLine();

            socket = new Socket(serverAddress, serverPort);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            logFile = new FileWriter("client.log", true);

            out.println(username);

            new Thread(new IncomingMessageHandler()).start();

            String message;
            while ((message = console.readLine()) != null) {
                if ("/exit".equalsIgnoreCase(message)) {
                    out.println("/exit");
                    break;
                }
                log("Sent: " + message);
                out.println(message);
            }
        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
        } finally {
            closeResources();
        }
    }

    private void readSettings() {
        try (BufferedReader reader = new BufferedReader(new FileReader(SETTINGS_FILE))) {
            serverAddress = reader.readLine().trim();
            serverPort = Integer.parseInt(reader.readLine().trim());
        } catch (Exception e) {
            System.err.println("Using default: localhost:12345");
            serverAddress = "localhost";
            serverPort = 12345;
        }
    }

    private void log(String message) throws IOException {
        String logEntry = "[" + new Date() + "] " + message;
        logFile.write(logEntry + "\n");
        logFile.flush();
    }

    private void closeResources() {
        try {
            if (socket != null) socket.close();
            if (logFile != null) logFile.close();
        } catch (IOException e) {
            System.err.println("Resource close error: " + e.getMessage());
        }
    }

    private class IncomingMessageHandler implements Runnable {
        @Override
        public void run() {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println(message);
                    log("Received: " + message);
                }
            } catch (IOException e) {
                System.out.println("Disconnected from server");
            }
        }
    }
}