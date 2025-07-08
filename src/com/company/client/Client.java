package com.company.client;
import java.io.*;
import java.net.*;
import java.util.*;

public class Client {
    private static final String SETTINGS_FILE = "settings.txt";
    private String serverAddress = "localhost";
    private int serverPort = 12345;
    private String username;

    public static void main(String[] args) {
        new Client().start();
    }

    public void start() {
        loadSettings();

        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Enter your name: ");
            username = scanner.nextLine();

            try (Socket socket = new Socket(serverAddress, serverPort);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                out.println(username);

                new Thread(() -> receiveMessages(in)).start();

                String message;
                while (true) {
                    message = scanner.nextLine();
                    if ("/exit".equalsIgnoreCase(message)) {
                        out.println("/exit");
                        break;
                    }
                    out.println(message);
                    logToFile("client_" + username + ".log", "Sent: " + message);
                }
            }
        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
        }
    }

    private void receiveMessages(BufferedReader in) {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println(message);
                logToFile("client_" + username + ".log", "Received: " + message);
            }
        } catch (IOException e) {
            System.out.println("Disconnected from server");
        }
    }

    private void loadSettings() {
        try (Scanner scanner = new Scanner(new File(SETTINGS_FILE))) {
            serverAddress = scanner.nextLine().trim();
            serverPort = Integer.parseInt(scanner.nextLine().trim());
        } catch (Exception e) {
            System.err.println("Using default settings (localhost:12345)");
        }
    }

    private void logToFile(String filename, String message) {
        try (FileWriter fw = new FileWriter(filename, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println("[" + new Date() + "] " + message);
        } catch (IOException e) {
            System.err.println("Log error: " + e.getMessage());
        }
    }
}