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


import java.io.DataInputStream;
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
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by mesterlum on 23/09/17.
 */

public class ServerIO extends Thread{

    private int port;
    private ArrayList<String> listClients;
    private ArrayList<String> listIpClients;
    private ArrayList<Socket> socketList;
    private static boolean is20 = false;
    private YouTubePlayer youtube;
    private Activity activity;
    private ArrayList<String>canciones;
    private ArrayList<String>codeCancion;
    private ArrayAdapter<String> adapterItems;
    private ArrayAdapter<String> adapterCanciones;
    private ListView list;
    private ListView listCanciones;
    private Timer timerVotation;
    private int numberCanciones;
    private ArrayList<Integer> votacionPoints = new ArrayList<Integer>();


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
        refreshClients();

    }
    private void votation(ArrayList<Socket> sockets){
        timerVotation = new Timer();
        TimerTask timerTaskVotation = new TimerTask() {
            @Override
            public void run() {
                System.out.println("Termino");
                bubbleSort(votacionPoints);
                youtube.play();
                timerVotation.cancel();
                numberCanciones = 0;


            }
        };

        timerVotation.schedule(timerTaskVotation, 10000);

        for (final Socket clients : sockets){
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

    private void refreshClients(){
        this.list.post(new Runnable() {
            @Override
            public void run() {
                adapterItems.notifyDataSetChanged();
            }
        });
    }

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


                                ObjectInputStream clientIO = new ObjectInputStream(clientSocket.getInputStream());
                                DataClient dataClient =(DataClient) clientIO.readObject();
                                if (dataClient.isExit()){
                                    Log.e("DESCONECTADO", "Desconectado");
                                    socketList.remove(clientSocket);
                                    listClients.remove(dataClient.getUser());
                                    listIpClients.remove(ip);
                                    refreshCanciones();
                                    refreshClients();

                                }else{
                                    if (dataClient.getHead().equalsIgnoreCase("votacion")){
                                        System.out.println("Votación");

                                        Log.w("Votación", Integer.toString(dataClient.getCancion()));
                                        votacionPoints.set(dataClient.getCancion(), votacionPoints.get(dataClient.getCancion()) +1);

                                    }else{
                                        System.out.println("No fué votacion");
                                        if (!listIpClients.contains(ip) || !listClients.contains(dataClient.getUser())){
                                            Log.i("Client", "El cliente no existe, se registrara");
                                            socketList.add(clientSocket);
                                            addUser(dataClient.getUser());
                                            listIpClients.add(ip);


                                        }else{

                                            if (!youtube.isPlaying() && canciones.isEmpty())
                                                youtube.loadVideo(dataClient.getMusicId());
                                            addCancion(dataClient.getMusic(), dataClient.getMusicId());
                                            numberCanciones++;
                                            if (numberCanciones > 20){
                                                youtube.pause();

                                                for (int i=0; i<canciones.size()-1; i++){
                                                    votacionPoints.add(0);
                                                }


                                                votation(socketList);

                                            }



                                        }
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


    private void bubbleSort(ArrayList<Integer>  intArray) {

        int n = intArray.size();
        int temp = 0;
       /* System.out.println("Lenghts: "  + n);
        System.out.println("Lenghts: "  + canciones.size());
        System.out.println("Lenghts: "  + codeCancion.size()); */


        for(int i=0; i < n; i++){
            for(int j=1; j < (n-i); j++){

                if(intArray.get(j-1) < intArray.get(j)){
                    //swap the elements!
                    temp = intArray.get(j-1);
                    intArray.set(j-1, intArray.get(j));
                    intArray.set(j, temp);
                    String tmpCancion = canciones.get(j-1);
                    canciones.set(j-1, canciones.get(j));
                    canciones.set(j, tmpCancion);
                    String tmpCodeCancion = codeCancion.get(j-1);
                    codeCancion.set(j-1, canciones.get(j));
                    codeCancion.set(j, tmpCodeCancion);
                }

            }
        }
        System.out.println(canciones);
        refreshCanciones();

    }

}
