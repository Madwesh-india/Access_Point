package com.madwesh_india.access_point;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import tech.gusavila92.websocketclient.WebSocketClient;

public class MainActivity extends AppCompatActivity {
    WebSocketClient webSocketClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        createWebSocketClient();
        Button b = findViewById(R.id.button);
        b.setOnClickListener(view -> {
            webSocketClient.close();
            TextView textView = findViewById(R.id.txt);
            String t = "closed";
            textView.setText(t);
        });
    }



    private void createWebSocketClient() {
        URI uri;
        try {
            uri = new URI("ws://192.168.0.108:8000/");
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
                final String message = s;
                runOnUiThread(() -> {
                    try {
                        TextView textView = findViewById(R.id.txt);
                        textView.setText(message);
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
        webSocketClient.setReadTimeout(60000);
        webSocketClient.enableAutomaticReconnection(5000);
        webSocketClient.connect();
    }
}