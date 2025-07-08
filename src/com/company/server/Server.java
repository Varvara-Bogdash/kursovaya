package com.company.server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Server {
    private static final String SETTINGS_FILE = "settings.txt";
    private int port;
    private List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private ExecutorService threadPool = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        new Server().start();
    }

    public void start() {
        loadSettings();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler client = new ClientHandler(clientSocket, this);
                clients.add(client);
                threadPool.execute(client);
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    private void loadSettings() {
        try (Scanner scanner = new Scanner(new File(SETTINGS_FILE))) {
            port = Integer.parseInt(scanner.nextLine().trim());
        } catch (Exception e) {
            System.err.println("Using default port 12345");
            port = 12345;
        }
    }

    public synchronized void broadcast(String message, ClientHandler sender) {
        String formatted = "[" + new Date() + "] " + message;
        logToFile("server.log", formatted);

        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(formatted);
            }
        }
    }

    public void removeClient(ClientHandler client) {
        clients.remove(client);
        broadcast(client.getUsername() + " left the chat", null);
    }

    private void logToFile(String filename, String message) {
        try (FileWriter fw = new FileWriter(filename, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(message);
        } catch (IOException e) {
            System.err.println("Log error: " + e.getMessage());
        }
    }


}