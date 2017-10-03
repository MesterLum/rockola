package com.example.mesterlum.rocko;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.example.mesterlum.rocko.R;
import com.example.mesterlum.rocko.process.ClientActivity;
import com.example.mesterlum.rocko.process.ServerActivity;

/**
 * Created by mesterlum on 23/09/17.
 */

public class MainActivity extends AppCompatActivity {

    private String user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layaout);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        this.user = extras.getString("User");
        Log.e("User", this.user);
    }

    public void Server(View view){
        Intent server = new Intent(this, ServerActivity.class);
        server.putExtra("User", this.user);
        startActivity(server);
    }

    public void Client(View view){
        Intent client = new Intent(this, ClientActivity.class);
        client.putExtra("User", this.user);
        startActivity(client);
    }
}
