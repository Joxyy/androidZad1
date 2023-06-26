package com.example.klijent4unizu;

import java.io.IOException;
import java.net.Socket;

public class SocketSingleton {
    private static Socket socket;
    private static SocketSingleton single_instance = null;

    private static SocketSingleton sSoleInstance;
    private SocketSingleton(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
    }  //private constructor.

    public static Socket getSocket() {
        return socket;
    }

    public static synchronized SocketSingleton getInstance(String host, int port) throws IOException {

        if (sSoleInstance == null){ //if there is no instance available... create new one
            sSoleInstance = new SocketSingleton(host, port);
        }

        return sSoleInstance;
    }

}
