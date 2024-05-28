package com.example.klijent4unizu;


import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;

public class ReceiveMessageFromServerInGame implements Runnable{
    GameActivity parent;
    BufferedReader br;



    public ReceiveMessageFromServerInGame(GameActivity parent) {
        //parent ce nam trebati da bismo mogli iz ovog thread-a da menjamo sadrzaj
        //komponenti u osnovnoj aktivnosti (npr da popunjavamo Spinner sa listom
        //korisnika
        this.parent = parent;
        //BufferedReader koristimo za prijem poruka od servera, posto su sve
        //poruke u formi Stringa i linija teksta, BufferedReader je zgodniji nego
        //da citamo poruke iz InputStream objekta direktno
        //this.br = parent.getBr();
    }

    @Override
    public void run() {
        //Beskonacna petlja
        String line="";
        while (true) {

                    if(!SocketSingleton.getBrCp().equals(line)) {
                        line = SocketSingleton.getBrCp();


                        if (line.startsWith("Draw: ")) {
                            String pos = line.split(":")[1].trim();
                            parent.runOnUiThread(() -> {
                                parent.setNewReceivedMessage("Protivnik uneo " + pos);
                                parent.drawOtherCircle(pos);
                                parent.enableCircles();
                                //SocketSingleton.setBrCp("cekajNovuPoruku");
                                                        /*if(checkWinner(){
                                    Toast.makeText(GameActivity.this, "POBEDILI STE", Toast.LENGTH_SHORT).show();
                                }*/

                            });
                        }
                        if (line.startsWith("Confetti: ")) {
                            String pos = line.split(":")[1].trim();
                            parent.runOnUiThread(() -> {
                                //parent.setNewReceivedMessage("Protivnik povezao " + pos);
                                parent.drawWinnerCircle(pos);

                            });
                        }
                        if (line.startsWith("Game over: ")) {
                            String opponent = line.split(":")[1].trim();
                            parent.runOnUiThread(() -> {
                                Toast.makeText(parent, opponent + " je pobedio", Toast.LENGTH_SHORT).show();
                                parent.enableCircles();
                                parent.playAgain();
                            });
                        }
                        if (line.startsWith("Dont play again: ")) {
                            String opponent = line.split(":")[1].trim();
                            parent.runOnUiThread(() -> {
                                Toast.makeText(parent, opponent + " ne zeli da igra opet", Toast.LENGTH_SHORT).show();
                                parent.exitTheGame();
                            });
                        }
                        if (line.startsWith("Play again: ")) {
                            String opponent = line.split(":")[1].trim();

                            parent.runOnUiThread(() -> {
                                parent.setFlgNewGame(true);
                                Toast.makeText(parent, opponent + " zeli da igra opet", Toast.LENGTH_SHORT).show();
                                parent.waitNewGame();
                            });
                        }
                    }

            }


        }
}

