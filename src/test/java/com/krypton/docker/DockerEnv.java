package com.krypton.docker;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class DockerEnv {
    
    private static final int  MAX_CONNECTION_ATTEMPTS = 40;

    public static void start() throws Exception {
        String startDockerScriptPath = System.getProperty("user.dir")+"/scripts/start-docker-env.sh";
        File file = new File(startDockerScriptPath);
        file.setExecutable(true);
        ProcessBuilder pb = new ProcessBuilder(startDockerScriptPath);
        pb.start();

        URL url = new URL("http://localhost:5000");
        HttpURLConnection req = (HttpURLConnection) url.openConnection();
        int attemptNumber = 0;
        boolean isConnected = false;
        while(!isConnected && attemptNumber <= MAX_CONNECTION_ATTEMPTS){
            try {
                req.connect();
                isConnected = true;
            } catch (IOException exception){ 
                Thread.sleep(1000);
                attemptNumber++;
            }
        }

        if (attemptNumber == MAX_CONNECTION_ATTEMPTS && !isConnected){
            throw new Exception("Impossible to start Krypton");
        } else {
            System.out.println("Krypton started successfully at: http://localhost:5000");
        }
    }

    public static void stop() throws Exception {
        String closeDockerScriptPath = System.getProperty("user.dir")+"/scripts/stop-docker-env.sh";
        File file = new File(closeDockerScriptPath);
        file.setExecutable(true);
        ProcessBuilder pb = new ProcessBuilder(closeDockerScriptPath);
        pb.start();

        URL url = new URL("http://localhost:5000");
        HttpURLConnection req = (HttpURLConnection) url.openConnection();
        
        int attemptNumber = 0;
        boolean isDisconnected = false;
        while(!isDisconnected && attemptNumber <= MAX_CONNECTION_ATTEMPTS){
            try {
                req.connect();
                Thread.sleep(1000);
                attemptNumber++;
            } catch (IOException exception){
                isDisconnected = true;
            }
        }

        if (attemptNumber == MAX_CONNECTION_ATTEMPTS && !isDisconnected){
            throw new Exception("Impossible to stop Krypton");
        } else {
            System.out.println("Krypton stopped successfully.");
        }
    }
}