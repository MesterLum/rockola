package com.example.mesterlum.rocko.process;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.mesterlum.rocko.R;
import com.example.mesterlum.rocko.process.server.ServerIO;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.google.android.youtube.player.YouTubePlayerView;


import java.util.ArrayList;


/**
 * Created by mesterlum on 23/09/17.
 */

public class ServerActivity extends AppCompatActivity implements YouTubePlayer.OnInitializedListener{

    private LinearLayout layout;

    public static ArrayAdapter<String> itemsAdapter;
    public static ArrayAdapter<String> cancionesAdapter;
    private ArrayList<String> cancionesArray;
    private ArrayList<String> clients ;
    private YouTubePlayerSupportFragment youTubePlayer;
    private YouTubePlayer youtube;
    private Toolbar tool;
    private String port;


    private String user;
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.server);
        this.layout = (LinearLayout) findViewById(R.id.server_layout);
        this.clients = new ArrayList<String>();
        this.cancionesArray = new ArrayList<String>();
        this.itemsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, clients);
        this.cancionesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, cancionesArray);




    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_server, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if (id == R.id.play){
            this.youtube.play();
        }
        if (id == R.id.next){
            this.youtube.next();
        }
        if (id == R.id.previous){
            this.youtube.previous();
        }
        if (id == R.id.pause){
            this.youtube.pause();
        }
        return super.onOptionsItemSelected(item);
    }

    public synchronized void createSocket(View view){
        String ip = "0.0.0.0";
        /*try {
           ip = ServerIO.getIp("wlan");
        }
        catch(Exception e){
            e.printStackTrace();
        }*/
        EditText port = (EditText) findViewById(R.id.port);
        this.port =port.getText().toString();

        if (Constants.DEBUG)
            Log.i("IP", "IP: " + ip + ":" + port.getText().toString());

        if (!ip.isEmpty()){
            this.layout.removeAllViews();
            setContentView(R.layout.server_socket);
            ListView list = (ListView) findViewById(R.id.list_clientes);
            ListView listCanciones = (ListView) findViewById(R.id.list_canciones);
            listCanciones.setAdapter(this.cancionesAdapter);
            list.setAdapter(this.itemsAdapter);
            TextView ipText = (TextView) findViewById(R.id.ipPort);
            ipText.setText(ip + ":" + port.getText().toString());
            this.youTubePlayer = (YouTubePlayerSupportFragment) getSupportFragmentManager().findFragmentById(R.id.youtube);
            tool = (Toolbar) findViewById(R.id.toolbar_server);
            setSupportActionBar(tool);

            youTubePlayer.initialize("AIzaSyAl7vVCG8_kDU_L-ptPAqpFUR-7hf08AVc", this);



        }
        else{
            Log.e("IP", "No estas en una red de wifi");
        }


    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
        this.youtube = youTubePlayer;
        ServerIO server = new ServerIO(Integer.parseInt(this.port),clients,cancionesArray, this.youtube, this);
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

    }
}
