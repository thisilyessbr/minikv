package com.minikv;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class StressTest {
    private static final int PORT = 6381;
    private static final int THREAD_COUNT = 20;
    private static final int OPS_PER_THREAD = 2000;

    public static void main(String[] args) throws Exception {
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch doneGate = new CountDownLatch(THREAD_COUNT);
        List<String> allKeys = Collections.synchronizedList(new ArrayList<>());

        for (int t = 0; t < THREAD_COUNT; t++) {
            final int threadId = t;
            Thread worker = new Thread(() -> {
                try (Socket socket = new Socket("localhost", PORT);
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                     PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                    startGate.await();

                    for (int j = 0; j < OPS_PER_THREAD; j++) {
                        String key = "t" + threadId + "-" + j;
                        String value = "value-" + threadId + "-" + j;
                        out.println("SET " + key + " " + value);
                        in.readLine();
                        allKeys.add(key + "=" + value);
                    }
                } catch (Exception e) {
                    System.out.println("Thread " + threadId + " failed: " + e.getMessage());
                } finally {
                    doneGate.countDown();
                }
            });
            worker.start();
        }

        System.out.println("Firing " + THREAD_COUNT + " threads x " + OPS_PER_THREAD + " SETs...");
        startGate.countDown();
        doneGate.await();

        System.out.println("All writes done. Verifying...");

        int missing = 0, corrupted = 0, ok = 0;
        try (Socket socket = new Socket("localhost", PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            for (String entry : allKeys) {
                String[] kv = entry.split("=", 2);
                String key = kv[0];
                String expected = kv[1];

                out.println("GET " + key);
                String actual = in.readLine();

                if (actual == null || actual.equals("(nil)") || actual.equals("NULL")) {
                    missing++;
                } else if (!actual.equals(expected)) {
                    corrupted++;
                } else {
                    ok++;
                }
            }
        }

        System.out.println("Total keys written: " + allKeys.size());
        System.out.println("OK:        " + ok);
        System.out.println("MISSING:   " + missing);
        System.out.println("CORRUPTED: " + corrupted);
    }
}