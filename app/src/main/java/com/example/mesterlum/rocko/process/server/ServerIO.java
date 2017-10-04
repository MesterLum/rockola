package com.example.mesterlum.rocko.process.server;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.StrictMode;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;


import com.example.mesterlum.rocko.process.DataClient;

import com.example.mesterlum.rocko.process.ServerActivity;
import com.google.android.youtube.player.YouTubePlayer;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;

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
    ArrayList<String>codeCancion;
    ArrayAdapter<String> adapterItems;
    ArrayAdapter<String> adapterCanciones;
    ListView list;
    ListView listCanciones;


    public ServerIO(int port, ArrayList<String> listClients, ArrayList<String> canciones, YouTubePlayer youtube, ArrayAdapter<String> itemsAdapter, ArrayAdapter<String> cancionesAdapter, ListView listCanciones, ListView list){
        this.start();
        this.listClients = listClients;
        this.canciones = canciones;
        this.listIpClients = new ArrayList<String>();
        this.socketList = new ArrayList<Socket>();
        this.port = port;
        this.youtube = youtube;
        this.list = list;
        this.listCanciones = listCanciones;
        this.adapterCanciones = cancionesAdapter;
        this.adapterItems = itemsAdapter;
        this.codeCancion = new ArrayList<String>();
        youtubeEvents();


    }
    private void addUser(String user){
        this.listClients.add(user);
        this.list.post(new Runnable() {
            @Override
            public void run() {
                adapterItems.notifyDataSetChanged();
            }
        });


    }
    private void votation(ArrayList<Socket> sockets){
        for (Socket clients : sockets){
            try {
                ObjectOutputStream out = new ObjectOutputStream(clients.getOutputStream());
                ArrayList<String> can = canciones;
                can.remove(0);
                out.writeObject(can);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void addCancion(String cancion, String cancionCode){
        this.canciones.add(cancion);
        this.codeCancion.add(cancionCode);
        refreshCanciones();

    }

    private void refreshCanciones(){
        this.listCanciones.post(new Runnable() {
            @Override
            public void run() {
                adapterCanciones.notifyDataSetChanged();
            }
        });
    }
    private static boolean is20 = false;
    public void run(){
        try {

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

            StrictMode.setThreadPolicy(policy);
            //Log.i("Port", ""+this.port);
            ServerSocket serverSocket = new ServerSocket(this.port);
            while(true){

                Log.i("Socket", "Esperando...");
                final Socket clientSocket = serverSocket.accept();
                final String ip= clientSocket.getInetAddress().getHostAddress().toString();
                Log.e("Ip client", ip);

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

                                    if (!youtube.isPlaying() && canciones.isEmpty())
                                        youtube.loadVideo(dataClient.getMusicId());
                                    addCancion(dataClient.getMusic(), dataClient.getMusicId());
                                    if (canciones.size() > 19){

                                        is20 = true;
                                        votation(socketList);
                                    }



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

    public void youtubeEvents(){
        youtube.setPlayerStateChangeListener(new YouTubePlayer.PlayerStateChangeListener() {
            @Override
            public void onLoading() {

            }

            @Override
            public void onLoaded(String s) {

            }

            @Override
            public void onAdStarted() {

            }

            @Override
            public void onVideoStarted() {
                if (!canciones.isEmpty()) {
                    canciones.set(0, canciones.get(0) + "(Reproduciendose)");
                    refreshCanciones();
                }
            }

            @Override
            public void onVideoEnded() {
                if (!canciones.isEmpty()){
                    if (canciones.size() > 0)
                        youtube.loadVideo(codeCancion.get(0));
                    codeCancion.remove(0);
                    canciones.remove(0);
                    refreshCanciones();
                }
            }

            @Override
            public void onError(YouTubePlayer.ErrorReason errorReason) {

            }
        });
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
