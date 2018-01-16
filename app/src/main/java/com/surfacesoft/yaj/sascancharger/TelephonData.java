package com.surfacesoft.yaj.sascancharger;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import java.util.ArrayList;

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
     * Obtiene un codigo de activacion basado en los
     * datos basicos del telefono
     */
    public static String getCodigoActivacion(StatusSQLiteHelper dbH) {
        ArrayList<String> data = TelephonData.getBasicsPhoneDataFromDB(dbH);

        String secretPass = Cryptography.getCryptoPass();
        String telefono = data.get(0);
        String imei = data.get(1);

        String middleCod = Cryptography.encryptIt(secretPass + telefono + secretPass).trim();
        String finalCod = Cryptography.encryptIt(telefono + imei + middleCod + imei + telefono).trim();

        return finalCod;
    }

    /**
     * Obtiene un codigo de activacion basado en los
     * datos basicos del telefono y
     * el CodigoIntermedio
     */
    public static String getCodigoActivacion(String middleCod, StatusSQLiteHelper dbH) {
        ArrayList<String> data = TelephonData.getBasicsPhoneDataFromDB(dbH);

        String telefono = data.get(0);
        String imei = data.get(1);

        String finalCod = Cryptography.encryptIt(telefono + imei + middleCod + imei + telefono).trim();

        return finalCod;
    }

    // Obtener codigo de activacion de BD
    public static String getCodigoActivacionFromDB(StatusSQLiteHelper dbH) {
        String cod = "";
        SQLiteDatabase dbStatus = dbH.getWritableDatabase();
        if (dbStatus != null) {
            Cursor c = dbStatus.rawQuery("SELECT " + dbH.codigoactiv + " FROM Status WHERE " + dbH.id + "=1", null);
            if (c.moveToFirst()) {
                if (!c.getString(0).equals("")) {
                    cod = c.getString(0);
                }
            }
        }
        dbStatus.close();
        return cod.trim();
    }

    /**
     * Obtiene un codigo de activacion intermedio basado en los
     * datos basicos del telefono
     */
    public static String getMiddleCodigoActivacion(String telefono) {
        String secretPass = Cryptography.getCryptoPass();

        return Cryptography.encryptIt(secretPass + telefono + secretPass).trim();
    }

    public static String cleanPhoneNumber(String phone){
        phone = phone.replace(" ", "")
                .replace("(", "")
                .replace(")", "")
                .replace("-", "")
                .replace("_", "")
                .replace("+53","")
                .replace("+","");

        return phone;
    }
}
