package com.surfacesoft.yaj.sascancharger;

/**
 * Created by Yulio on 24/07/2015.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Estructura de la BD
 * <p/>
 * STATUS
 * - [data1] id (Serial)
 * - [data2] telefono (text)
 * - [data3] IMEI (text)
 * - [data4] codigoactivacion (text)
 */
public class StatusSQLiteHelper extends SQLiteOpenHelper {

    // Campos de la Tabla
    public final String id = "data1";
    public final String telefono = "data2";
    public final String imei = "data3";
    public final String codigoactiv = "data4";

    //Sentencia SQL para crear la tabla Status
    String sqlCreate = "CREATE TABLE Status (" + id + " INTEGER PRIMARY KEY, " + telefono + " TEXT, " +
            imei + " TEXT, " + codigoactiv + " TEXT)";

    public StatusSQLiteHelper(Context contexto, String nombre, CursorFactory factory, int version) {
        super(contexto, nombre, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Se ejecuta la sentencia SQL de creación de la tabla
        db.execSQL(sqlCreate);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int versionAnterior, int versionNueva) {
        //NOTA: Por simplicidad del ejemplo aquí utilizamos directamente
        // la opción de eliminar la tabla anterior y crearla de nuevo
        // vacía con el nuevo formato.
        // Sin embargo lo normal será que haya que migrar datos de la
        // tabla antigua a la nueva, por lo que este método debería
        // ser más elaborado.
        //Se elimina la versión anterior de la tabla
        db.execSQL("DROP TABLE IF EXISTS Status");
        //Se crea la nueva versión de la tabla
        db.execSQL(sqlCreate);
    }
}
