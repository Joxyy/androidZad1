/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server4unizu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joxy
 */
public class ConnectedPlayers implements Runnable{
    //atributi koji se koriste za komunikaciju sa klijentom
    private Socket socket;
    private String userName;
    private BufferedReader br;
    private PrintWriter pw;
    private ArrayList<ConnectedPlayers> allPlayers;

    //getters and setters
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
    
        //Konstruktor klase, prima kao argument socket kao vezu sa uspostavljenim klijentom
    public ConnectedPlayers(Socket socket, ArrayList<ConnectedPlayers> allPlayers) {
        this.socket = socket;
        this.allPlayers = allPlayers;

        //iz socket-a preuzmi InputStream i OutputStream
        try {
            //posto se salje tekst, napravi BufferedReader i PrintWriter
            //kojim ce se lakse primati/slati poruke (bolje nego da koristimo Input/Output stream
            this.br = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), "UTF-8"));
            this.pw = new PrintWriter(new OutputStreamWriter(this.socket.getOutputStream()), true);
            //zasad ne znamo user name povezanog klijenta
            this.userName = "";
        } catch (IOException ex) {
            Logger.getLogger(ConnectedPlayers.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Metoda prolazi i pravi poruku sa trenutno povezanik korisnicima u formatu
     * Users: ImePrvog ImeDrugog ImeTreceg ... kada se napravi poruka tog
     * formata, ona se salje svim povezanim korisnicima
     */
    
    void connectedClientsUpdateStatus() {
        //priprema string sa trenutno povezanim korisnicima u formatu 
        //Users: Milan Dusan Petar
        //i posalji svim korisnicima koji se trenutno nalaze u chat room-u
        String connectedUsers = "Users:";
        for (ConnectedPlayers c : this.allPlayers) {
            connectedUsers += " " + c.getUserName();
        }

        //prodji kroz sve klijente i svakom posalji info o novom stanju u sobi
        for (ConnectedPlayers svimaUpdateCB : this.allPlayers) {
            svimaUpdateCB.pw.println(connectedUsers);
        }

        System.out.println(connectedUsers);
    }

    @Override
    public void run() {
        //Server prima od svakog korisnika najpre njegovo korisnicko ime
        //a kasnije poruke koje on salje ostalim korisnicima u chat room-u
        while (true) {
            try {   
                //ako nije poslato ime, najpre cekamo na njega
                if (this.userName.equals("")) {                             //OBTAIN NICKNAME
                    this.userName = this.br.readLine();
                    if (this.userName != null) {
                        System.out.println("Connected user: " + this.userName);
                        //informisi sve povezane klijente da imamo novog 
                        //clana u chat room-u
                        connectedClientsUpdateStatus();
                    } else {
                        //ako je userName null to znaci da je terminiran klijent thread
                        System.out.println("Disconnected user: " + this.userName);
                        for (ConnectedPlayers cl : this.allPlayers) {
                            if (cl.getUserName().equals(this.userName)) {
                                this.allPlayers.remove(cl);
                                break;
                            }
                        }
                        connectedClientsUpdateStatus();
                        break;
                    }
                    ////////CEKAMO PORUKU/////////
                } else {
                    //vec nam je korisnik poslao korisnicko ime, poruka koja je 
                    //stigla je za nekog drugog korisnika iz chat room-a (npr Milana) u 
                    //formatu Milan: Cao Milane, kako si?
                    System.out.println("waiting for request");
                    String line = this.br.readLine();
                    System.out.println(line);
                    System.out.println("stigao zahtev");
                    if (line != null) {
                        /*prepoznaj za koga je poruka, pronadji tog 
                        korisnika i njemu prosledi poruku
                        Npr stigla je poruka "Milan: Cao Milane, kako si?" od Dusana
                        sa kojim komuniciramo u ovoj niti. 
                        To znaci da server treba da prosledi poruku Milanu sa tekstom
                        Dusan: Cao Milane, kako si?
                         */
                        
                        if (line.startsWith("Send request to: ")) {
                            String[] informacija = line.split(": ");
                            String opponent = informacija[1].trim();

                            System.out.println(this.userName + " zeli da igra sa igracem " + opponent);


                            for (ConnectedPlayers clnt : this.allPlayers) {
                                if (clnt.getUserName().equals(opponent)) {
                                    System.out.println(clnt.getUserName());
                                    //prosledi poruku namenjenom korisniku
                                    clnt.pw.println("Request from: " +this.userName);
                                    System.out.println(opponent + "");
                                } else {
                                    //ispisi da je korisnik kome je namenjena poruka odsutan
                                    if (opponent.equals("")) {
                                        this.pw.println("Igrac " + opponent + " je odsutan!");
                                    }
                                }
                            }
                        }else if(line.startsWith("Request accepted: ")){
                            String[] informacija = line.split(": ");
                            String opponent = informacija[1].trim();

                            System.out.println(this.userName + " je prihvatio da igra sa " + opponent);


                            for (ConnectedPlayers clnt : this.allPlayers) {
                                if (clnt.getUserName().equals(opponent)) {
                                    System.out.println(clnt.getUserName());
                                    //prosledi poruku namenjenom korisniku
                                    clnt.pw.println("Request from: " +this.userName);
                                    System.out.println(opponent + "");
                                } else {
                                    //ispisi da je korisnik kome je namenjena poruka odsutan
                                    if (opponent.equals("")) {
                                        this.pw.println("Igrac " + opponent + " je odsutan!");
                                    }
                                }
                            }
                        }
                        else if(line.startsWith("Request denied: ")){}
                        else{
                            
                        }

                    } else {
                        //slicno kao gore, ako je line null, klijent se diskonektovao
                        //ukloni tog korisnika iz liste povezanih korisnika u chat room-u
                        //i obavesti ostale da je korisnik napustio sobu
                        System.out.println("Disconnected user: " + this.userName);

                        //Ovako se uklanja element iz kolekcije 
                        //ne moze se prolaziti kroz kolekciju sa foreach a onda u 
                        //telu petlje uklanjati element iz te iste kolekcije
                        Iterator<ConnectedPlayers> it = this.allPlayers.iterator();
                        while (it.hasNext()) {
                            if (it.next().getUserName().equals(this.userName)) {
                                it.remove();
                            }
                        }
                        connectedClientsUpdateStatus();

                        this.socket.close();
                        break;
                    }

                }
            } catch (IOException ex) {
                System.out.println("Disconnected user: " + this.userName);
                //npr, ovakvo uklanjanje moze dovesti do izuzetka, pogledajte kako je 
                //to gore uradjeno sa iteratorom
                for (ConnectedPlayers cl : this.allPlayers) {
                    if (cl.getUserName().equals(this.userName)) {
                        this.allPlayers.remove(cl);
                        connectedClientsUpdateStatus();
                        return;
                    }
                }

            }

        }
    }
}
