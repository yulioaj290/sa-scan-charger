package com.surfacesoft.yaj.sascancharger;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;


public class Activate extends Activity {

    private static final int CODIGO_CPHONENUMBER = 1;

    // Base de datos Helper
    StatusSQLiteHelper dbH = new StatusSQLiteHelper(this, "Status", null, 1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activate);

        //  Obtain basic data from DB
        ArrayList<String> data = TelephonData.getBasicsPhoneDataFromDB(dbH);
        //  Requesting Phone number
        if (data.get(0).isEmpty() || data.get(0).length() != 8 || data.get(0).charAt(0) != '5') {
            startActivity(new Intent(Activate.this, ConfigurePhoneNumber.class));
        }
    }

    public void activateApp(View v) {
        String middleCod = ((EditText) findViewById(R.id.etCodigo)).getText().toString().trim();

        String finalUserCode = TelephonData.getCodigoActivacion(middleCod, dbH);
        String finalAppCode = TelephonData.getCodigoActivacion(dbH);

        if (finalUserCode.equals(finalAppCode)) {
            SQLiteDatabase dbStatus = this.dbH.getWritableDatabase();
            if (dbStatus != null) {
                dbStatus.execSQL("UPDATE Status SET " + dbH.codigoactiv + "='" + finalAppCode + "' WHERE " + dbH.id + "=1");
            }
            dbStatus.close();

            this.setResult(Activity.RESULT_OK);
            this.finish();
        } else {
            Toast.makeText(this, "Código de activación incorrecto!", Toast.LENGTH_LONG).show();
            ((EditText) findViewById(R.id.etCodigo)).setText("");
        }
    }

    public void solicitarActivacion(View v) {
        String encodedNumSimbol = Uri.encode("#");
        String beneficiario = "54509765";
        String clave = "";
        String monto = ""; // 0.40

        String ussd = "*234*1*" + beneficiario + "*" + clave + "*" + monto + encodedNumSimbol;
        startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:" + ussd)));
//        Toast.makeText(this, Uri.parse("tel:" + ussd).toString(), Toast.LENGTH_LONG).show();
    }

    public void closeWindows(View v) {
        this.setResult(Activity.RESULT_CANCELED);
        this.finish();
    }

}
