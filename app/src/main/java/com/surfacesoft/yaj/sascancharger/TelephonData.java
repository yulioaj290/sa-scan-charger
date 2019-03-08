package com.surfacesoft.yaj.sascancharger;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import java.util.ArrayList;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Created by Yulio on 25/07/2015.
 */
public class TelephonData {

    /**
     * Obtiene los datos basicos del telefono
     * [0] Numero de telefono
     * [1] Numero IMEI
     */
    public static ArrayList<String> getBasicsPhoneData(Context cont) {
        ArrayList<String> data = new ArrayList<>(2);

        // Obteniendo Numero del telefono
        TelephonyManager tMgr = (TelephonyManager) cont.getSystemService(Context.TELEPHONY_SERVICE);

        if (ContextCompat.checkSelfPermission(cont,
                Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) cont,
                    Manifest.permission.READ_PHONE_STATE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions((Activity) cont,
                        new String[]{Manifest.permission.READ_PHONE_STATE},
                        1);
            }
        } else {
            // Permission has already been granted

            String telefono = tMgr.getLine1Number().trim();

            telefono = cleanPhoneNumber(telefono);

            if (telefono.length() > 8) {
                telefono = telefono.substring(telefono.length() - 8);
            }

            data.add(telefono);

            // Obteniendo el IMEI del telefono
            String deviceId;
            if (tMgr.getDeviceId() != null) {
                deviceId = tMgr.getDeviceId();
            } else {
                deviceId = Settings.Secure.getString(cont.getContentResolver(), Settings.Secure.ANDROID_ID);
            }
            data.add(deviceId.trim());

        }

        return data;
    }

    /**
     * Obtiene los datos basicos del telefono desde la BD
     * [0] Numero de telefono
     * [1] Numero IMEI
     */
    public static ArrayList<String> getBasicsPhoneDataFromDB(StatusSQLiteHelper dbH) {
        ArrayList<String> data = new ArrayList<>(2);
        SQLiteDatabase dbStatus = dbH.getWritableDatabase();
        if (dbStatus != null) {
            Cursor c = dbStatus.rawQuery("SELECT " + dbH.telefono + ", " + dbH.imei + " FROM Status WHERE " + dbH.id + "=1", null);
            if (c.moveToFirst()) {
                data.add(Cryptography.decryptIt(c.getString(0)).trim());
                data.add(Cryptography.decryptIt(c.getString(1)).trim());
            }
        }
        dbStatus.close();
        return data;
    }

    /**
     * Obtiene un codigo de activacion intermedio basado en los
     * datos basicos del telefono
     */
    public static String getMiddleCodigoActivacion(String telefono) {
        String secretPass = Cryptography.getCryptoPass();

        return Cryptography.encryptIt(secretPass + telefono + secretPass).trim();
    }

    public static String cleanPhoneNumber(String phone) {
        phone = phone.replace(" ", "")
                .replace("(", "")
                .replace(")", "")
                .replace("-", "")
                .replace("_", "")
                .replace("+53", "")
                .replace("+", "");

        return phone;
    }
}
