package com.example.klijent4unizu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketSingleton {
    private static Socket socket;
    private static BufferedReader br;
    private static PrintWriter pw;

    public static Socket getSocket() {
        return socket;
    }

    public static BufferedReader getBr() {
        return br;
    }

    public static PrintWriter getPw() {
        return pw;
    }

    private static SocketSingleton sSoleInstance = null;
    private SocketSingleton(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        this.br = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        this.pw = new PrintWriter(this.socket.getOutputStream(),true);

    }

    public static synchronized SocketSingleton getInstance(String host, int port) throws IOException {

        if (sSoleInstance == null){ //if there is no instance available... create new one
            sSoleInstance = new SocketSingleton(host, port);
        }

        return sSoleInstance;
    }
    public static synchronized SocketSingleton getInstance() throws IOException {
        return sSoleInstance;
    }



    /*private static SocketSingleton single_instance = null;

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
    public static synchronized SocketSingleton getInstance() throws IOException {
        return sSoleInstance;
    }*/

}
