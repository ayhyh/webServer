package com.loop.hyh.core;

import com.loop.hyh.core.server.Server;

import java.util.Scanner;

public class BootStrap {
    
    public static void run(String port) {
        Server server;
        if(port == null) {
            server = new Server();
        }else {
            try{
                int p = Integer.parseInt(port);
                server = new Server(p);
            }catch (NumberFormatException e) {
                server = new Server();
            }
        }
        Scanner scanner = new Scanner(System.in);
        String order;
        while (scanner.hasNext()) {
            order = scanner.next();
            if (order.equals("EXIT")) {
                server.close();
                System.exit(0);
            }
        }
    }
}
