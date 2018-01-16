package com.surfacesoft.yaj.sascancharger;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class ConfigurePhoneNumber extends Activity {

    // Base de datos Helper
    StatusSQLiteHelper dbH = new StatusSQLiteHelper(this, "Status", null, 1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_phone_number);

        // Cancelando por defecto
        setResult(Activity.RESULT_CANCELED);
    }

    public void actionConfigurePhone(View v) {
        String phoneNum = ((EditText) findViewById(R.id.etPhoneConfigure2)).getText().toString().trim();

        if (phoneNum.trim().isEmpty()) {
            Toast.makeText(this, "Introduzca datos por favor", Toast.LENGTH_SHORT).show();
        } else if (phoneNum.trim().length() != 8) {
            Toast.makeText(this, "El n\u00famero de m\u00f3vil debe tener 8 digitos", Toast.LENGTH_SHORT).show();
        } else if (phoneNum.charAt(0) != '5') {
            Toast.makeText(this, "El n\u00famero de m\u00f3vil debe comenzar con 5", Toast.LENGTH_SHORT).show();
        } else {
            SQLiteDatabase dbStatus = this.dbH.getWritableDatabase();

            if (dbStatus != null) {
                dbStatus.execSQL("UPDATE Status SET " +
                        dbH.telefono + "='" + Cryptography.encryptIt(phoneNum) + "' WHERE " + dbH.id + "=1");
            }
            dbStatus.close();

            this.setResult(Activity.RESULT_OK);
            this.finish();
        }
    }

    public void closeWindows(View v) {
        this.setResult(Activity.RESULT_CANCELED);
        this.finish();
    }

}
