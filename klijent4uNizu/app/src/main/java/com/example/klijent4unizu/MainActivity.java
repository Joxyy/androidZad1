package com.example.klijent4unizu;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    EditText etIP;
    EditText etPort;
    EditText etNickname;
    Button btnConnect;
    Button btnEnterRoom;
    Button btnPlay;

    Spinner spnPlayers;
    TextView tvOutputMessages;

    private Socket socket;
    private BufferedReader br;
    private PrintWriter pw;

    public Socket getSocket() {
        return socket;
    }

    public BufferedReader getBr() {
        return br;
    }

    public PrintWriter getPw() {
        return pw;
    }

    public Spinner getSpnPlayers() {
        return spnPlayers;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);         //initializes the activity and sets up the basic functionality, parameter is used to restore the activity's previous state
        setContentView(R.layout.activity_main);     //sets XML file as the content view for the current activity

        etIP = (EditText) findViewById(R.id.etIP);
        etPort = (EditText) findViewById(R.id.etPort);
        etNickname = (EditText) findViewById(R.id.etNickname);

        btnConnect = (Button) findViewById(R.id.btnConnect);
        btnEnterRoom = (Button) findViewById(R.id.btnEnterRoom);
        btnPlay = (Button) findViewById(R.id.btnPlay);
        spnPlayers = (Spinner) findViewById(R.id.spnPlayers);

        tvOutputMessages = (TextView) findViewById(R.id.tvOutputMessages);
        tvOutputMessages.setMovementMethod(new ScrollingMovementMethod());


        MainActivity.this.etNickname.setEnabled(false);
        MainActivity.this.btnEnterRoom.setEnabled(false);
        MainActivity.this.btnPlay.setEnabled(false);
        MainActivity.this.spnPlayers.setEnabled(false);

        btnConnect.setOnClickListener(view -> {

            //Kreiraj novi socket (ako nije localhost, treba promeniti IP adresu
            //Vazno!!! U Androidu, sve aktivnosti u vezi sa mrezom MORAJU da se vrse u zasebnoj
            //niti. Zbog toga se u metodi connectToServer kreira nova nit koja se povezuje sa
            //serverom. Na slican nacin ce se i slati poruke kasnije serveru.

            // kreiranje Socketa kora da se izvrsi u zasebnoj niti. VAZNO: Obzirom na to da je
            // ova metoda metoda klase MainActivity, u njoj mozemo da pristupimo atributima socket,
            // br (BufferedReader) i pw (PrintWriter) klase MainActivity. To je izuzetno vazno jer ne
            // zelimo da svaki put kada se kreira nova nit, kreiramo novi socket za komunikaciju sa
            // serverom, ili kasnije kada saljemo poruke serveru (ili primamo poruke od servera) da
            // koristimo pogresan Socket za to

            new Thread(() -> {
                if (MainActivity.this.socket == null) {
                    try {
                        //loopback adresa u Androidu je 10.0.2.2 slicno kao 127.0.0.1 u dosadasnjim
                        //konzolnim/GUI Java aplikacijama
                        SocketSingleton s1 = SocketSingleton.getInstance(MainActivity.this.etIP.getText().toString(), Integer.parseInt(MainActivity.this.etPort.getText().toString()));
                        MainActivity.this.socket = s1.getSocket();
                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this, "Povezani ste sa serverom", Toast.LENGTH_LONG).show();
                            MainActivity.this.etNickname.setEnabled(true);
                            MainActivity.this.btnEnterRoom.setEnabled(true);
                            MainActivity.this.btnConnect.setEnabled(false);
                            MainActivity.this.etIP.setEnabled(false);
                            MainActivity.this.etPort.setEnabled(false);
                        });
                    } catch (IOException e) {
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Neuspesna konekcija", Toast.LENGTH_LONG).show());
                    }
                    try {
                        MainActivity.this.br = new BufferedReader(new InputStreamReader(MainActivity.this.socket.getInputStream()));
                        MainActivity.this.pw = new PrintWriter(new OutputStreamWriter(MainActivity.this.socket.getOutputStream()), true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        });
        this.btnEnterRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!MainActivity.this.etNickname.getText().toString().equals("")) {
                    //posalji svoje ime serveru, kao kod kreiranja socket-a, to se mora uraditi u
                    //zasebnoj niti, pogledajte implementaciju funkcije sendMessage
                    sendMessage(MainActivity.this.etNickname.getText().toString());
                    //za prijem poruka od servera (stizace asinhrono) koristi poseban thread
                    //da bismo u novom thread-u mogli da menjamo sadrzaj komponenti (npr Combo Box-a)
                    //konstruktoru novog thread-a se prosledjuje MainActivity.this (obratite paznju da
                    //na ovom mestu u kodu ako stavimo samo this, to se odnosi na objekat
                    //View.OnClickListener klase). Obratite paznju da nema potreba praviti lokalnu
                    //promenljivu ili atribut klase MainActivity koji ce biti objekat klase
                    //ReceiveMessageFromServer, cak ni objekat klase Thread (doduse objekat klase
                    //Thread bi trebalo napraviti u slucaju da zelimo negde u kodu da cekamo da se
                    // ta nit terminira pozivom na Thread.join - ovde se to ne desava)
                    new Thread(new ReceiveMessageFromServer(MainActivity.this)).start();

                    //Dozvoli koriscenje odredjenih komponenti, a zabrani ostale
                    MainActivity.this.spnPlayers.setEnabled(true);
                    MainActivity.this.btnPlay.setEnabled(true);
                    MainActivity.this.etNickname.setEnabled(false);
                    MainActivity.this.btnEnterRoom.setEnabled(false);
                } else {
                    MainActivity.this.spnPlayers.setEnabled(false);
                    MainActivity.this.btnPlay.setEnabled(false);
                }
            }
        });
        this.btnPlay.setOnClickListener(view -> {
            //proveri da li se salje poruka samom sebi, ako ne, ispisi tekst poslate poruke
            //u TextView polju, a posalji poruku odgovarajuceg formata koristeci sendMessage metodu
            if (!MainActivity.this.etNickname.getText().toString().equals("") &&
                    !MainActivity.this.etNickname.getText().toString().equals(MainActivity.this.spnPlayers.getSelectedItem().toString())){
                sendMessage("Send request to: "+ MainActivity.this.spnPlayers.getSelectedItem().toString());
                Toast.makeText(MainActivity.this, "Zahtev za igranje uspesno poslat", Toast.LENGTH_LONG).show();
                MainActivity.this.spnPlayers.setEnabled(false);
                MainActivity.this.btnPlay.setEnabled(false);
            }
            else{
                if (MainActivity.this.etNickname.getText().toString().equals(MainActivity.this.spnPlayers.getSelectedItem().toString())){
                    Toast.makeText(MainActivity.this, "Ne mozete igrati sami sa sobom", Toast.LENGTH_LONG).show();
                }
            }
        });

    }
    public void sendMessage(String message){
        // Slicno kao kod kreiranja Socketa, slanje poruke se mora vrsiti u zasebnoj niti, ali
        // da se pri tome koristi prethodno kreirani Socket odnosno PrintWriter baziran na njemu
        new Thread(() -> {
            if (MainActivity.this.pw != null) {
                MainActivity.this.pw.println(message);
            }
        }).start();
    }
    public void setNewReceivedMessage(String message){
        this.tvOutputMessages.setText(message + "\n");
    }
    public void confirmRequest(String opponent){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setTitle("Potvrdi");
        builder.setMessage(opponent + " salje zahtev. Da li zelite da igrate?");

        builder.setPositiveButton("YES", (dialog, which) -> {
            // Do nothing but close the dialog
            sendMessage("Request accepted: " + opponent);
            dialog.dismiss();
        });

        builder.setNegativeButton("NO", (dialog, which) -> {

            // Do nothing
            sendMessage("Request denied: " + opponent);
            //u oba slucaja treba obavestiti protivnika o potvrdi/odbijanju
            dialog.dismiss();
        });

        AlertDialog alert = builder.create();
        alert.show();

    }
}