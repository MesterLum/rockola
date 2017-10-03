package com.example.mesterlum.rocko.process;

import android.util.Log;

/**
 * Created by mesterlum on 23/09/17.
 */

public class Login {

    /*

        Esta funcion hará una consulta a una api rest para conectar,
        devolvera un JSON y posteriormente sí hay conexion lo retornara.

     */
    public static boolean connection(String id, String password){
        if (Constants.DEBUG){
            Log.i("Login", "User: " + id);
            Log.i("Login", "Password: " + password);
        }

        if (id.equalsIgnoreCase("mesterlum") && password.equals("palafox88")){
            return true;
        }


        return false;
    }
}
