package com.example.mesterlum.rocko;

import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;


import com.example.mesterlum.rocko.process.CheckConnectitivy;
import com.example.mesterlum.rocko.process.Constants;
import com.example.mesterlum.rocko.process.Login;

public class FirstActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (CheckConnectitivy.isOnline(this)){

            if (Constants.DEBUG)
                Log.i("Internet", "Hay internet");

            setContentView(R.layout.activity_first);
        }else{
            if (Constants.DEBUG)
                Log.e("Internet", "No hay conexion a internet...");

            setContentView(R.layout.no_internet);
        }


    }

    public void Login(View view){
        EditText user = (EditText) findViewById(R.id.user);
        EditText pass = (EditText) findViewById(R.id.password);

        if (Login.connection(user.getText().toString(), pass.getText().toString())){
            Intent main = new Intent(this, MainActivity.class);
            main.putExtra("User", user.getText().toString());
            startActivity(main);
        }


    }
}
