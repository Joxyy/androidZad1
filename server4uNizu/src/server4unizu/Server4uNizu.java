/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package server4unizu;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joxy
 */
public class Server4uNizu {

    private ServerSocket ssocket;
    private int port;
    private ArrayList<ConnectedPlayers> players;
    private ArrayList<ConnectedPlayers> avPlayers;

    public ServerSocket getSsocket() {
        return ssocket;
    }

    public void setSsocket(ServerSocket ssocket) {
        this.ssocket = ssocket;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
    
    public void acceptPlayers() {
        Socket client = null;
        Thread thr;
        while (true) {
            try {
                System.out.println("Waiting for new clients..");
                client = this.ssocket.accept();
            } catch (IOException ex) {
                Logger.getLogger(Server4uNizu.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (client != null) {
                //Povezao se novi klijent, kreiraj objekat klase ConnectedChatRoomClient
                //koji ce biti zaduzen za komunikaciju sa njim
                ConnectedPlayers clnt = new ConnectedPlayers(client, players, avPlayers);
                //i dodaj ga na listu povezanih klijenata jer ce ti trebati kasnije
                players.add(clnt);
                avPlayers.add(clnt);
                //kreiraj novu nit (konstruktoru prosledi klasu koja implementira Runnable interfejs)
                thr = new Thread(clnt);
                //..i startuj ga
                thr.start();
            } else {
                break;
            }
        }
    }
    
    public Server4uNizu(int port) {
        this.players = new ArrayList<>();
        this.avPlayers = new ArrayList<>();
        try {
            this.port = port;
            this.ssocket = new ServerSocket(port);
        } catch (IOException ex) {
            Logger.getLogger(Server4uNizu.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {
        Server4uNizu server = new Server4uNizu(2812);

        System.out.println("Server pokrenut, slusam na portu 2812");

        //Prihvataj klijente u beskonacnoj petlji
        server.acceptPlayers();
    }
}
