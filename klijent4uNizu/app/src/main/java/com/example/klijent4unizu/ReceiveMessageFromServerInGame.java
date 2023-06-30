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
        this.br = parent.getBr();
    }

    @Override
    public void run() {
        //Beskonacna petlja
        while (true) {
            String line;
            try {


                line = this.br.readLine();


                if (line.startsWith("Draw: ")) {
                    String pos = line.split(":")[1].trim();
                    parent.runOnUiThread(() -> {
                        parent.setNewReceivedMessage("Stigla poruka");
                        parent.drawOtherCircle(pos);
                        parent.enableCircles();

                    });

                }


            } catch (IOException ex) {
                parent.runOnUiThread(() -> Toast.makeText(parent, "Ne mogu da primim poruku!", Toast.LENGTH_LONG).show());
            }
        }
    }

}
