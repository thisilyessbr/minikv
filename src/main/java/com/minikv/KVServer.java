package com.minikv;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

public class KVServer {
    public static void main(String[] args) throws IOException {
        Map<String, String> store = new HashMap<>();
        ServerSocket serverSocket = new ServerSocket(6381);
        System.out.println("Waiting for connection...");

        Socket client = serverSocket.accept();
        System.out.println("Connection established!");

        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        PrintWriter out = new PrintWriter(client.getOutputStream(), true);

        String line;
        while((line = in.readLine()) != null){
            String[] parts = line.split(" ", 3);
            String command = parts[0].toUpperCase();

            if(command.equals("SET") && parts.length == 3){
                String key = parts[1];
                String value = parts[2];
                store.put(key, value);
                out.println("OK");
            } else if (command.equals("GET") && parts.length == 2) {
                String key = parts[1];
                String value = store.get(key);
                if(value != null){
                    out.println(value);
                } else {
                    out.println("NULL");
                }
            } else {
                out.println("ERROR: Invalid command");
            }
        }


    }
}