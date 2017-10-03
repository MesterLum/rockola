package com.example.mesterlum.rocko.process.server;

import android.os.StrictMode;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by mesterlum on 24/09/17.
 */

public class ClientIO {

    public static Socket sockConnection(String ip, int port, String user){
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            Socket client = new Socket(ip, port);
            return client;


        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }
}
