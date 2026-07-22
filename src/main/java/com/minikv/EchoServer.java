
package com.minikv;

import java.io.*;
import java.net.*;

public class EchoServer {
    public static void main(String[] args)throws IOException{
         ServerSocket serverSocket = new ServerSocket(6381);
         System.out.println("Waiting for connection...");

         Socket client = serverSocket.accept();
         System.out.println("Connection established!");

         BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
         PrintWriter out = new PrintWriter(client.getOutputStream(), true);

         String line;
         while((line = in.readLine()) != null){
             System.out.println("Received: " + line);
             out.println(line);
         }
    }
}