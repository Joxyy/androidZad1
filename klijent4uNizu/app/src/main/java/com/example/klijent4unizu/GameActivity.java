package com.example.klijent4unizu;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Objects;

public class GameActivity extends AppCompatActivity {
    public static String RESPONSE_MESSAGE = "Response_text";
    Intent intent;

    private Socket socket;
    private BufferedReader br;
    private PrintWriter pw;
    private boolean zeleni;
    //private boolean winner;
    private String opponent;
    int rows = 6;
    int columns = 7;
    private String boja;

    private String plyr1;
    private String plyr2;
    private boolean flgNewGame=false;
    private boolean flgIwannaPlay=false;
    HashMap<String, ImageView> circles;

    public void setPlyr1(String plyr1) {
        this.plyr1 = plyr1;
    }

    public void setPlyr2(String plyr2) {
        this.plyr2 = plyr2;
    }

    public String getPlyr1() {
        return plyr1;
    }

    public String getPlyr2() {
        return plyr2;
    }

    public BufferedReader getBr() {
        return br;
    }

    public void setFlgNewGame(boolean flgNewGame) {
        this.flgNewGame = flgNewGame;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        intent = getIntent();
        plyr1 = (String) intent.getExtras().getString(MainActivity.PLYR1);
        plyr2 = (String) intent.getExtras().getString(MainActivity.PLYR2);

        new Thread(() -> {
            if (GameActivity.this.socket == null) {
                try {
                    //loopback adresa u Androidu je 10.0.2.2 slicno kao 127.0.0.1 u dosadasnjim
                    //konzolnim/GUI Java aplikacijama
                    SocketSingleton s2 = SocketSingleton.getInstance(); //vraca singlton objekat
                    GameActivity.this.socket = SocketSingleton.getSocket();
                    //GameActivity.this.br = SocketSingleton.getBr();
                    GameActivity.this.pw = SocketSingleton.getPw();
                    new Thread(new ReceiveMessageFromServerInGame(GameActivity.this)).start();
                    runOnUiThread(() -> {
                        Toast.makeText(GameActivity.this, "Povezani ste sa serverom", Toast.LENGTH_SHORT).show();
                    });
                } catch (IOException e) {
                    runOnUiThread(() -> Toast.makeText(GameActivity.this, "Neuspesna konekcija", Toast.LENGTH_SHORT).show());
                }

            }
        }).start();


        startGame();    //odredjuje boju i plyr
        circles = new HashMap<String, ImageView>();
        LinearLayout llmain = findViewById(R.id.lvmain);

        for (int row = 1; row <= rows; row++) {
            LinearLayout llrow = new LinearLayout(this);
            llrow.setOrientation(LinearLayout.HORIZONTAL);
            for (int col = 1; col <= columns; col++) {
                ImageView iv = new ImageView(this);
                iv.setTag(R.id.pos, row + "," + col);
                iv.setTag(R.id.colour, "gray");
                circles.put(row + "," + col, iv);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, 160);
                layoutParams.weight = 1;
                iv.setLayoutParams(layoutParams);
                iv.setImageResource(R.drawable.gray);
                iv.setOnClickListener((v) -> {
                    if (v.getTag(R.id.colour).toString().equals("gray")) {
                        Toast.makeText(GameActivity.this, "Kliknuo si na poziciju " + v.getTag(R.id.pos).toString(), Toast.LENGTH_SHORT).show();

                        int r = Integer.parseInt(v.getTag(R.id.pos).toString().split(",")[0].trim());
                        int k = Integer.parseInt(v.getTag(R.id.pos).toString().split(",")[1].trim());

                        for (int i = 6; i >= r; i--) {
                            if (circles.get(i + "," + k).getTag(R.id.colour).toString().equals("gray")) {
                                /*for(int broj=r; broj<i;broj++) {
                                    if (zeleni)
                                        circles.get(i + "," + k).setImageResource(R.drawable.android);
                                    else
                                        circles.get(i + "," + k).setImageResource(R.drawable.purple);
                                    handler.postDelayed(() -> {}, 300);
                                    circles.get(i + "," + k).setImageResource(R.drawable.gray);
                                    */


                                drawMyCircle(i + "," + k);

                                if (checkWinner()) {
                                    Toast.makeText(GameActivity.this, "POBEDILI STE", Toast.LENGTH_SHORT).show();
                                    sendMessage("Game over: "  + opponent);
                                    playAgain();
                                }
                                else {
                                    sendMessage("Draw: " + i + "," + k + ": " + opponent);  //saljem za koga je poruka i parametre za hash mapu
                                    Toast.makeText(GameActivity.this, opponent + " je na potezu", Toast.LENGTH_SHORT).show();
                                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                }
                                break;
                            }
                        }


                    } else
                        Toast.makeText(GameActivity.this, "Mozes kliknuti samo na prazne kruzice", Toast.LENGTH_SHORT).show();
                });
                llrow.addView(iv);
            }
            llmain.addView(llrow);
        }
    }

    public void sendMessage(String message) {
        // Slicno kao kod kreiranja Socketa, slanje poruke se mora vrsiti u zasebnoj niti, ali
        // da se pri tome koristi prethodno kreirani Socket odnosno PrintWriter baziran na njemu
        new Thread(() -> {
            if (GameActivity.this.pw != null) {
                GameActivity.this.pw.println(message);
            }
        }).start();
    }

    public void drawOtherCircle(String pos) {
        if (zeleni) {
            circles.get(pos).setImageResource(R.drawable.purple);
            circles.get(pos).setTag(R.id.colour, "purple");
        } else {
            circles.get(pos).setImageResource(R.drawable.android);
            circles.get(pos).setTag(R.id.colour, "green");
        }
    }

    public void drawMyCircle(String pos) {
        if (zeleni) {
            Objects.requireNonNull(circles.get(pos)).setImageResource(R.drawable.android);
            circles.get(pos).setTag(R.id.colour, "green");
        } else {
            circles.get(pos).setImageResource(R.drawable.purple);
            circles.get(pos).setTag(R.id.colour, "purple");
        }
    }

    public void drawWinnerCircle(String pos) {
        Objects.requireNonNull(circles.get(pos)).setImageResource(R.drawable.confetti);
        circles.get(pos).setTag(R.id.colour, "green");
    }

    public void setNewReceivedMessage(String message) {
        Toast.makeText(GameActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    public void enableCircles() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public boolean checkWinner() {
        boolean winner = true;
        for (int i = 1; i <= 3; i++) {
            for (int j = 1; j <= 4; j++) {
                for (int k = 0; k < 4; k++) {

                    if (circles.get((i + k) + "," + (j + k)) != null) {
                        if (!circles.get((i + k) + "," + (j + k)).getTag(R.id.colour).toString().equals(boja))
                            winner = false;
                    }
                }
                if (winner) {
                    for (int k = 0; k < 4; k++) {
                        drawWinnerCircle((i + k) + "," + (j + k));
                        sendMessage("Confetti: " + (i + k) + "," + (j + k) + ": " + opponent);
                    }

                    return true;
                } else winner = true;
            }
        }

        for (int i = 1; i <= 3; i++) {
            for (int j = 4; j <= 7; j++) {
                for (int k = 0; k < 4; k++) {
                    if (circles.get((i + k) + "," + (j - k)) != null) {
                        if (!circles.get((i + k) + "," + (j - k)).getTag(R.id.colour).toString().equals(boja))
                            winner = false;
                    }
                }
                if (winner) {
                    for (int k = 0; k < 4; k++) {
                        drawWinnerCircle((i + k) + "," + (j - k));
                        sendMessage("Confetti: " + (i + k) + "," + (j - k) + ": " + opponent);
                    }
                    return true;
                } else winner = true;
            }
        }
            for (int i = 1; i <= 3; i++) {
                for (int j = 1; j <= 7; j++) {
                    for (int k = 0; k < 4; k++) {
                        if (circles.get((i + k) + "," + j) != null) {
                            if (!circles.get((i + k) + "," + j).getTag(R.id.colour).toString().equals(boja))
                                winner = false;
                        }
                    }
                    if (winner) {
                        for (int k = 0; k < 4; k++) {
                            drawWinnerCircle((i + k) + "," + j);
                            sendMessage("Confetti: " + (i + k) + "," + j + ": " + opponent);
                        }
                        return true;
                    } else winner = true;
                }

            }

            for (int i = 1; i <= 6; i++) {
                for (int j = 1; j <= 4; j++) {
                    for (int k = 0; k < 4; k++) {
                        if (circles.get(i + "," + (j + k)) != null) {
                            if (!circles.get(i + "," + (j + k)).getTag(R.id.colour).toString().equals(boja))
                                winner = false;
                        }
                    }
                    if (winner) {
                        for (int k = 0; k < 4; k++) {
                            drawWinnerCircle(i + "," + (j + k));
                            sendMessage("Confetti: " + i + "," + (j+k) + ": " + opponent);
                        }
                        return true;
                    } else winner = true;
                }
            }
            return false;
        }
    public void playAgain(){
        AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);

        builder.setTitle("Potvrdi");
        builder.setMessage(opponent + " salje zahtev. Da li zelite da igrate?");

        builder.setPositiveButton("DA", (dialog, which) -> {
            // Do nothing but close the dialog
            sendMessage("Play again: " + opponent);
            String tmp= getPlyr1();
            setPlyr1(getPlyr2());
            setPlyr2(tmp);
            flgIwannaPlay=true;
            waitNewGame();
            dialog.dismiss();
        });

        builder.setNegativeButton("NE", (dialog, which) -> {

            // Do nothing
            sendMessage("Dont play again: " + opponent);
            //u oba slucaja treba obavestiti protivnika o potvrdi/odbijanju
            //intent.putExtra(RESPONSE_MESSAGE, "poruka");
            exitTheGame();
            dialog.dismiss();
        });

        AlertDialog alert = builder.create();
        alert.show();

    }
    public void exitTheGame(){
        setResult(RESULT_OK, intent);
        finish();
    }
    public void startGame(){
        //POCETAK IGRICE**************
        //samo jedan moze da klice
        if (!plyr1.equals(MainActivity.getWhoami())) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            Toast.makeText(GameActivity.this, plyr2 + " je na potezu", Toast.LENGTH_SHORT).show();

            zeleni = false;
            boja = "purple";
            opponent = plyr1;
        } else {

            Toast.makeText(GameActivity.this, " Tvoj potez", Toast.LENGTH_SHORT).show();

            zeleni = true;
            boja = "green";
            opponent = plyr2; //ZELENI IGRA PRVII UVEEK
        }

    }
    public void waitNewGame(){
        resetCircles();
        if(flgNewGame&&flgIwannaPlay) {

            flgNewGame = false;
            flgIwannaPlay = false;
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            startGame();
        }
    }
    public void resetCircles(){
        for (int i = 1; i <= 6; i++) {
            for (int j = 1; j <= 7; j++) {
                    if (circles.get(i + "," + j) != null) {
                        circles.get(i + "," + j).setImageResource(R.drawable.gray);
                        circles.get(i + "," + j).setTag(R.id.colour, "gray");
                    }

            }
        }
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
}