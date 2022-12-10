package com.madwesh_india.access_point;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;

import tech.gusavila92.websocketclient.WebSocketClient;

public class MainActivity extends AppCompatActivity {
    WebSocketClient webSocketClient = null;

    Button pc_on;
    Button pc_off;
    Button teamviewer;
    Button jupyternotebook;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        if(webSocketClient == null){
            Runnable runnable = this::createWebSocketClient;
            Thread thread = new Thread(runnable);
            thread.start();

            pc_on = findViewById(R.id.pc_on);
            pc_off = findViewById(R.id.pc_off);
            teamviewer = findViewById(R.id.teamviewer);
            jupyternotebook = findViewById(R.id.jupyternotebook);

            JSONObject jo = new JSONObject();
            try {
                jo.put("isfirst", "false");
                jo.put("device", "mobile");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            pc_on.setOnClickListener(view -> {
                try {
                    jo.put("target", "ESP");
                    jo.put("value", "pc_on");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                webSocketClient.send(String.valueOf(jo));
            });
            pc_off.setOnClickListener(view -> {
                try {
                    jo.put("target", "ESP");
                    jo.put("value", "pc_off");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                webSocketClient.send(String.valueOf(jo));
            });
            jupyternotebook.setOnClickListener(view -> {
                try {
                    jo.put("target", "pc");
                    jo.put("value", "jupyternotebook");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                webSocketClient.send(String.valueOf(jo));
            });
            teamviewer.setOnClickListener(view -> {
                try {
                    jo.put("target", "pc");
                    jo.put("value", "teamviewer");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                webSocketClient.send(String.valueOf(jo));
            });
        }

//        Button b = findViewById(R.id.button);
//        b.setOnClickListener(view -> {
//            webSocketClient.close();
//            TextView textView = findViewById(R.id.txt);
//            String t = "closed";
//            textView.setText(t);
//        });
    }



    private void createWebSocketClient() {
        URI uri;
        try {
            uri = new URI("ws://192.168.0.108:3000/");
        }
        catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        webSocketClient = new WebSocketClient(uri) {

            @Override
            public void onOpen() {
                Log.i("WebSocket", "Session is starting");
                JSONObject jo = new JSONObject();
                try {
                    jo.put("isfirst", "true");
                    jo.put("device", "mobile");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                this.send(String.valueOf(jo));
            }

            @Override
            public void onTextReceived(String s) {
                Log.i("WebSocket", "Message received");
                JSONObject jo;
                JSONObject dev = null;
                String type = null;
                try {
                    jo = new JSONObject(s);
                    String conection = jo.getString("conection");
                    String devices = jo.getString("devices");
                    dev = new JSONObject(devices);
                    switch (conection) {
                        case "true": {
                            type = "You are Connected";
                            break;
                        }
                        case "newConnection": {
                            type = "New device Connected";
                            break;
                        }
                        case "alreadyConected": {
                            type = "You are already Connected";
                            break;
                        }
                        case "lost":{
                            type = "A device lost Connection";
                            break;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String finalType = type;
                JSONObject finalDev = dev;
                runOnUiThread(() -> {
                    try {
                        TextView textView = findViewById(R.id.txt);
                        SwitchCompat esp = findViewById(R.id.esp);
                        SwitchCompat pc = findViewById(R.id.pc);
                        SwitchCompat mob = findViewById(R.id.mobile);
                        if(finalDev.getString("ESP").equals("true")){
                            esp.setChecked(true);
                            esp.setBackgroundColor(Color.GREEN);
                            pc_on.setEnabled(true);
                            pc_off.setEnabled(true);
                            pc_on.setBackgroundColor(Color.parseColor("#8a00c3"));
                            pc_off.setBackgroundColor(Color.parseColor("#8a00c3"));
                        }
                        else{
                            esp.setChecked(false);
                            esp.setBackgroundColor(Color.RED);
                            pc_on.setEnabled(false);
                            pc_off.setEnabled(false);
                            pc_on.setBackgroundColor(Color.GRAY);
                            pc_off.setBackgroundColor(Color.GRAY);
                        }
                        if(finalDev.getString("mobile").equals("true")){
                            mob.setChecked(true);
                            mob.setBackgroundColor(Color.GREEN);
                        }
                        else{
                            mob.setChecked(false);
                            mob.setBackgroundColor(Color.RED);
                        }
                        if(finalDev.getString("pc").equals("true")){
                            pc.setChecked(true);
                            pc.setBackgroundColor(Color.GREEN);
                            teamviewer.setEnabled(true);
                            jupyternotebook.setEnabled(true);
                            teamviewer.setBackgroundColor(Color.parseColor("#8a00c3"));
                            jupyternotebook.setBackgroundColor(Color.parseColor("#8a00c3"));
                        }
                        else{
                            pc.setChecked(false);
                            pc.setBackgroundColor(Color.RED);
                            teamviewer.setEnabled(false);
                            jupyternotebook.setEnabled(false);
                            teamviewer.setBackgroundColor(Color.GRAY);
                            jupyternotebook.setBackgroundColor(Color.GRAY);
                        }

                        textView.setText(s);
                        Toast toast = Toast.makeText(getApplicationContext(), finalType, Toast.LENGTH_SHORT);
                        toast.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public void onBinaryReceived(byte[] data) {
            }

            @Override
            public void onPingReceived(byte[] data) {
            }

            @Override
            public void onPongReceived(byte[] data) {
            }

            @Override
            public void onException(Exception e) {
                System.out.println(e.getMessage());
            }

            @Override
            public void onCloseReceived() {
                Log.i("WebSocket", "Closed ");
                TextView textView = findViewById(R.id.txt);
                String t = "closed";
                textView.setText(t);
            }
        };

        webSocketClient.setConnectTimeout(10000);
        webSocketClient.setReadTimeout(600000);
        webSocketClient.enableAutomaticReconnection(5000);
        webSocketClient.connect();
    }
}