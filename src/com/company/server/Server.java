package com.company.server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Server {
    private static final String SETTINGS_FILE = "settings.txt";
    private int port;
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private List<ClientHandler> clients;
    private FileWriter logFile;

    public static void main(String[] args) {
        new Server().start();
    }

    public void start() {
        readSettings();
        clients = new CopyOnWriteArrayList<>();
        threadPool = Executors.newCachedThreadPool();

        try {
            serverSocket = new ServerSocket(port);
            openLogFile();
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

    private void readSettings() {
        try (BufferedReader reader = new BufferedReader(new FileReader(SETTINGS_FILE))) {
            port = Integer.parseInt(reader.readLine().trim());
        } catch (Exception e) {
            System.err.println("Using default port 12345");
            port = 12345;
        }
    }

    private void openLogFile() throws IOException {
        logFile = new FileWriter("server.log", true);
    }

    public synchronized void broadcast(String message, ClientHandler sender) {
        String formatted = "[" + new Date() + "] " + message;
        try {
            logFile.write(formatted + "\n");
            logFile.flush();
        } catch (IOException e) {
            System.err.println("Log error: " + e.getMessage());
        }

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

}