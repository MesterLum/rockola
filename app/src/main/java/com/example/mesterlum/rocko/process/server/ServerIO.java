package com.example.mesterlum.rocko.process.server;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Looper;
import android.os.StrictMode;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.mesterlum.rocko.process.Constants;
import com.example.mesterlum.rocko.process.DataClient;
import com.example.mesterlum.rocko.process.ServerActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by mesterlum on 23/09/17.
 */

public class ServerIO extends Thread{

    private int port;
    private ArrayList<String> listClients;
    private ArrayList<String> listIpClients;
    private ArrayList<Socket> socketList;

    public static String getIp(String id){
        String ip = "";
        try {

            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                if (intf.getName().contains(id)) {
                    List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                    for (InetAddress addr : addrs) {

                        if (!addr.isLoopbackAddress()) {
                            String sAddr = addr.getHostAddress();
                            if (addr instanceof Inet4Address) {
                                return sAddr;
                            }
                        }

                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
    private YouTubePlayer youtube;
    private Activity activity;
    ArrayList<String>canciones;
    public ServerIO(int port, ArrayList<String> listClients,ArrayList<String>canciones, YouTubePlayer youtube, Activity activity){
        this.start();
        this.listClients = listClients;
        this.canciones = canciones;
        this.listIpClients = new ArrayList<String>();
        this.socketList = new ArrayList<Socket>();
        this.port = port;
        this.youtube = youtube;
        this.activity = activity;



    }
    private void addUser(String user){
        this.listClients.add(user);
        //ServerActivity.itemsAdapter.notifyDataSetChanged();

    }

    private void addCancion(String cancion){
        this.canciones.add(cancion);
        //ServerActivity.itemsAdapter.notifyDataSetChanged();

    }

    public void run(){
        try {

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

            StrictMode.setThreadPolicy(policy);
            Log.i("Port", ""+this.port);
            ServerSocket serverSocket = new ServerSocket(this.port);
            while(true){

                Log.i("Socket", "Esperando...");
                final Socket clientSocket = serverSocket.accept();
                final String ip= clientSocket.getInetAddress().getHostAddress().toString();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while(true){
                            try {

                                Log.i("Sock inf", "Esperando...");
                                ObjectInputStream clientIO = new ObjectInputStream(clientSocket.getInputStream());
                                DataClient dataClient =(DataClient) clientIO.readObject();
                                if (!listIpClients.contains(ip)){
                                    Log.i("Client", "El cliente no existe, se registrara");
                                    socketList.add(clientSocket);
                                    addUser(dataClient.getUser());
                                    listIpClients.add(ip);
                                }else{
                                    Log.i("Se agrego", dataClient.getMusic());
                                    addCancion(dataClient.getMusic());
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }).start();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    String resultado = "perro";
    private String clientAccepted(String user){

        AlertDialog.Builder alert=new AlertDialog.Builder(this.activity);

        alert.setMessage("La persona: " + user + "Intenta conectarse")
                .setCancelable(false)
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        resultado = "a";
                    }
                })
                .setNegativeButton("Rechazar", new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        resultado = "b";
                    }
                });
        alert.show();
        return resultado;
    }

}
