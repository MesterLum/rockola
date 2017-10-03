package com.example.mesterlum.rocko.process;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.devazt.networking.HttpClient;
import com.devazt.networking.OnHttpRequestComplete;
import com.devazt.networking.Response;
import com.example.mesterlum.rocko.R;
import com.example.mesterlum.rocko.process.server.ClientIO;
import com.example.mesterlum.rocko.process.server.ServerIO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by mesterlum on 23/09/17.
 */

public class ClientActivity extends AppCompatActivity {

    private Toolbar toolbr;
    private LinearLayout layout;
    private String user;
    private ListView listSearch;
    private ArrayAdapter<String> arrayAdapterSearch;
    private ArrayList<String> arraySearch;
    private Socket socket;
    private String ip;
    private int port;
    private DataClient client;
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client_find_socket);
        this.layout = (LinearLayout) findViewById(R.id.cliente_find_socket);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        this.user = extras.getString("User");


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_client, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                arraySearch.clear();
                arrayId.clear();
                arrayTittle.clear();
                ApiConsult(newText);
                return false;
            }
        });
        return true;
    }
    ArrayList<String> arrayId = new ArrayList<String>();
    ArrayList<String> arrayTittle = new ArrayList<String>();
    public void ApiConsult(String query){
        String URL_API = "https://www.googleapis.com/youtube/v3/search?part=snippet&q="+query+"&type=video&key=AIzaSyDOteAJfJjKaX1JBJxCQ7wp5vH5irZXCu8";

        new HttpClient(new OnHttpRequestComplete() {
            @Override
            public void onComplete(Response status) {
                if (status.isSuccess()){
                    try {
                        JSONObject jsonObject = new JSONObject(status.getResult());
                        JSONArray jsonArray = jsonObject.getJSONArray("items");
                        for (int i=0; i<jsonArray.length(); i++){
                            JSONObject jsonFor = new JSONObject(jsonArray.getString(i));
                            arrayId.add(jsonFor.getJSONObject("id").getString("videoId"));
                            arrayTittle.add(jsonFor.getJSONObject("snippet").getString("title"));

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }
        }).excecute(URL_API);

        for (String title : arrayTittle){
            this.arraySearch.add(title);
        }
        this.arrayAdapterSearch.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if (id == R.id.action_search){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void searchSocket(View view){
        EditText iptxt = (EditText) findViewById(R.id.txt_ip);
        EditText portTxt = (EditText) findViewById(R.id.txt_port);

        String ip = iptxt.getText().toString();
        this.ip = ip;
        this.port = Integer.parseInt(portTxt.getText().toString());


        if (Constants.DEBUG){
            Log.i("Connection-Sock", "ip: " + ip);
            Log.i("Connection-Sock", "port: " + port);

        }
        this.socket = ClientIO.sockConnection(ip,this.port, this.user);
        this.client = new DataClient();
        this.client.setUser(this.user);
        try {
            ObjectOutputStream clientOS = new ObjectOutputStream(this.socket.getOutputStream());
            clientOS.writeObject(this.client);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (this.socket != null){
            this.layout.removeAllViews();
            setContentView(R.layout.client);
            this.listSearch = (ListView) findViewById(R.id.lista_search);
            this.arraySearch = new ArrayList<String>();
            this.arrayAdapterSearch = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, arraySearch);
            this.listSearch.setAdapter(this.arrayAdapterSearch);

            //Añado el evento
            setEvent();
            toolbr = (Toolbar) findViewById(R.id.toolbar_client);
            toolbr.setTitle("Client");
            setSupportActionBar(toolbr);

        }
        else{
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setMessage("No se encontro este server").show();
        }

    }
    private void setEvent(){
        this.listSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String videoId = arrayId.get(i);
                String video = arrayTittle.get(i);

                if (socket != null){


                    client.setMusicId(videoId);
                    client.setMusic(video);
                    try {
                        ObjectOutputStream clientOS = new ObjectOutputStream(socket.getOutputStream());
                        clientOS.writeObject(client);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else{
                    AlertDialog.Builder alert = new AlertDialog.Builder(ClientActivity.this);
                    alert.setMessage("Hubo un error con el server").show();
                }

            }
        });
    }
}
