package com.example.klijent4unizu;

import androidx.appcompat.app.AppCompatActivity;

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
    private boolean winner;
    private String opponent;
    int rows = 6;
    int columns = 7;
    private String boja;
    HashMap<String, ImageView> circles;


    public BufferedReader getBr() {
        return br;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        intent = getIntent();
        String plyr1 = (String) intent.getExtras().getString(MainActivity.PLYR1);
        String plyr2 = (String) intent.getExtras().getString(MainActivity.PLYR2);

        new Thread(() -> {
            if (GameActivity.this.socket == null) {
                try {
                    //loopback adresa u Androidu je 10.0.2.2 slicno kao 127.0.0.1 u dosadasnjim
                    //konzolnim/GUI Java aplikacijama
                    SocketSingleton s2 = SocketSingleton.getInstance(); //vraca singlton objekat
                    GameActivity.this.socket = s2.getSocket();
                    GameActivity.this.br = s2.getBr();
                    GameActivity.this.pw = s2.getPw();
                    new Thread(new ReceiveMessageFromServerInGame(GameActivity.this)).start();
                    runOnUiThread(() -> {
                        Toast.makeText(GameActivity.this, "Povezani ste sa serverom", Toast.LENGTH_SHORT).show();
                    });
                } catch (IOException e) {
                    runOnUiThread(() -> Toast.makeText(GameActivity.this, "Neuspesna konekcija", Toast.LENGTH_SHORT).show());
                }

            }
        }).start();



        //POCETAK IGRICE**************
        //samo jedan moze da klice
        if(!plyr1.equals(MainActivity.getWhoami())){
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            Toast.makeText(GameActivity.this, plyr2 + " je na potezu", Toast.LENGTH_SHORT).show();

            zeleni=false;
            boja="purple";
            opponent=plyr1;
        }else {
            Toast.makeText(GameActivity.this, " Tvoj potez", Toast.LENGTH_SHORT).show();
            zeleni= true;
            boja="green";
            opponent=plyr2; //ZELENI IGRA PRVII UVEEK
        }

        circles = new HashMap<String, ImageView>();
        LinearLayout llmain = findViewById(R.id.lvmain);

        for (int row = 1; row <= rows; row++){
            LinearLayout llrow = new LinearLayout(this);
            llrow.setOrientation(LinearLayout.HORIZONTAL);
            for (int col = 1; col <= columns; col++){
                ImageView iv = new ImageView(this);
                iv.setTag(R.id.pos, row+","+col);
                iv.setTag(R.id.colour, "gray");
                circles.put(row+","+col, iv);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0,160);
                layoutParams.weight = 1;
                iv.setLayoutParams(layoutParams);
                iv.setImageResource(R.drawable.gray);
                iv.setOnClickListener((v)->{
                    if(v.getTag(R.id.colour).toString().equals("gray")){
                        Toast.makeText(GameActivity.this, "Kliknuo si na poziciju "+v.getTag(R.id.pos).toString(), Toast.LENGTH_SHORT).show();

                        int r= Integer.parseInt(v.getTag(R.id.pos).toString().split(",")[0].trim());
                        int k=Integer.parseInt(v.getTag(R.id.pos).toString().split(",")[1].trim());
                        for(int i=6;i>=r;i--){
                            if(Objects.requireNonNull(circles.get(i + "," + k)).getTag(R.id.colour).toString().equals("gray")){
                                /*for(int broj=r; broj<i;broj++) {
                                    if (zeleni)
                                        circles.get(i + "," + k).setImageResource(R.drawable.android);
                                    else
                                        circles.get(i + "," + k).setImageResource(R.drawable.purple);
                                    handler.postDelayed(() -> {}, 300);
                                    circles.get(i + "," + k).setImageResource(R.drawable.gray);
                                    */


                                    drawMyCircle(i + "," + k);
                                    sendMessage("Draw: " + i + "," + k + ": " + opponent);  //saljem za koga je poruka i parametre za hash mapu
                                    Toast.makeText(GameActivity.this, opponent + " je na potezu", Toast.LENGTH_SHORT).show();
                                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                    /*if(checkWinner(i + "," + k)){
                                        Toast.makeText(GameActivity.this, "POBEDILI STE", Toast.LENGTH_SHORT).show();
                                    }*/
                                    break;
                            }
                        }
                    }
                    else Toast.makeText(GameActivity.this, "Mozes kliknuti samo na prazne kruzice", Toast.LENGTH_SHORT).show();
                });
                llrow.addView(iv);
            }
            llmain.addView(llrow);
        }

    }
    public void sendMessage(String message){
        // Slicno kao kod kreiranja Socketa, slanje poruke se mora vrsiti u zasebnoj niti, ali
        // da se pri tome koristi prethodno kreirani Socket odnosno PrintWriter baziran na njemu
        new Thread(() -> {
            if (GameActivity.this.pw != null) {
                GameActivity.this.pw.println(message);
            }
        }).start();
    }
    public void drawOtherCircle(String pos){
        if (zeleni) {
            Objects.requireNonNull(circles.get(pos)).setImageResource(R.drawable.purple);
            circles.get(pos).setTag(R.id.colour, "purple");
        } else {
            circles.get(pos).setImageResource(R.drawable.android);
            circles.get(pos).setTag(R.id.colour, "green");
        }
    }
    public void drawMyCircle(String pos){
        if (zeleni) {
            Objects.requireNonNull(circles.get(pos)).setImageResource(R.drawable.android);
            circles.get(pos).setTag(R.id.colour, "green");
        } else {
            circles.get(pos).setImageResource(R.drawable.purple);
            circles.get(pos).setTag(R.id.colour, "purple");
        }
    }
    public void setNewReceivedMessage(String message){
        Toast.makeText(GameActivity.this, message, Toast.LENGTH_SHORT).show();
    }
    public void enableCircles(){
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
    public boolean checkWinner(String pos){
        winner = true;
        int r= Integer.parseInt(pos.split(",")[0].trim());
        int k=Integer.parseInt(pos.split(",")[1].trim());
        for(int i=1;i<=3;i++){
            if(circles.get((r+i) + "," + (k+i))!=null) {
                if (!circles.get((r+i) + "," + (k+i)).getTag(R.id.colour).toString().equals(boja))
                    winner = false;
            }
            if(circles.get((r-i) + "," + (k-i))!=null) {
                if (!circles.get((r-i) + "," + (k-i)).getTag(R.id.colour).toString().equals(boja))
                    winner = false;
            }
            if(circles.get((r-i) + "," + (k+i))!=null) {
                if (!circles.get((r-i) + "," + (k+i)).getTag(R.id.colour).toString().equals(boja))
                    winner = false;
            }
            if(circles.get((r+i) + "," + (k-i))!=null) {
                if (!circles.get((r+i) + "," + (k-i)).getTag(R.id.colour).toString().equals(boja))
                    winner = false;
            }
            if(circles.get(r + "," + (k-i))!=null) {
                if (!circles.get(r+ "," + (k-i)).getTag(R.id.colour).toString().equals(boja))
                    winner = false;
            }
            if(circles.get(r + "," + (k+i))!=null) {
                if (!circles.get(r+ "," + (k+i)).getTag(R.id.colour).toString().equals(boja))
                    winner = false;
            }
            if(circles.get((r+i) + "," + k)!=null) {
                if (!circles.get((r+i) + "," + k).getTag(R.id.colour).toString().equals(boja))
                    winner = false;
            }
            if(circles.get((r-i) + "," + k)!=null) {
                if (!circles.get((r-i) + "," + k).getTag(R.id.colour).toString().equals(boja))
                    winner = false;
            }
        }
        /*for(int i=1;i<=3;i++){
            for (int j=1;j<=4;i++) {
                for (int k=0;k<4;k++) {
                    if (!circles.get((i + k) + "," + (j + k)).getTag(R.id.colour).toString().equals(boja))
                        winner = false;
                    else if (!circles.get(i + "," + (j + k)).getTag(R.id.colour).toString().equals(boja))
                        winner = false;
                    else if (!circles.get((i + k) + "," + j).getTag(R.id.colour).toString().equals(boja))
                        winner = false;
                }
            }
        }
        for(int i=1;i<=3;i++){
            for (int j=5;j<=7;i++) {
                for (int k=0;k<4;k++) {
                    if (!circles.get((i + k) + "," + (j - k)).getTag(R.id.colour).toString().equals(boja))
                        winner = false;
                    else if (!circles.get((i + k) + "," + j).getTag(R.id.colour).toString().equals(boja))
                        winner = false;
                }
            }
        }*/

    return winner;
    }
}