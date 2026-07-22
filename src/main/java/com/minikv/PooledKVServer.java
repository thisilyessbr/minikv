package com.minikv;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PooledKVServer {

  public static void main(String[] args) throws IOException {
    Map<String, String> store = new HashMap<>();

    ExecutorService pool = Executors.newFixedThreadPool(10);
    ServerSocket serverSocket = new ServerSocket(6381);
    System.out.println("Server started, waiting for connections...");

        while (true) {
            Socket client = serverSocket.accept();
            System.out.println("New client connected: " + client.getInetAddress());
            pool.submit(() -> handleClient(client, store));
        }

  }
  private static void handleClient(Socket client, Map<String, String> store) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter out = new PrintWriter(client.getOutputStream(), true);

            String line;
            while ((line = in.readLine()) != null) {
                String[] parts = line.split(" ", 3);
                String command = parts[0].toUpperCase();

                if (command.equals("SET") && parts.length == 3) {
                    store.put(parts[1], parts[2]);
                    out.println("OK");
                } else if (command.equals("GET") && parts.length == 2) {
                    String value = store.get(parts[1]);
                    out.println(value == null ? "(nil)" : value);
                } else {
                    out.println("ERROR: unknown command or wrong number of arguments");
                }
            }
        } catch (IOException e) {
            System.out.println("Client disconnected or error: " + e.getMessage());
        } finally {
            try { client.close(); } catch (IOException ignored) {}
        }
    }



}