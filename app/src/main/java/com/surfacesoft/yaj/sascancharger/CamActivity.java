package com.surfacesoft.yaj.sascancharger;

/**
 * @author Yulio
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.surfacesoft.yaj.sascancharger.camera.CameraManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class CamActivity extends Activity {
    //  Absolute path to store pictures and language data
    public static final String DATA_PATH = Environment
            .getExternalStorageDirectory().toString() + File.separator + "SAScanCharger" + File.separator;

    //  Default Language for OCR detect
    public static final String lang = "eng";

    private static final int CODIGO_ACTIVAR = 1;
    private static final int CODIGO_CPHONENUMBER = 2;

    // Base de datos Helper
    StatusSQLiteHelper dbH = new StatusSQLiteHelper(this, "Status", null, 1);

    Preview preview;
    ViewfinderView viewfinderView;
    CameraManager cameraManager;
    Activity act;
    Context ctx;
    File outFile;   //  file for storing the picture

    RelativeLayout infoView;
    LinearLayout loadingView;
    LinearLayout chargingView;
    EditText editChargeCode;
    ImageButton btnCapture;
    boolean isBtnCaptureEnabled;
    ProgressBar progressBar;
    RelativeLayout menu;
    boolean hideMenu;
    TranslateAnimation transMenu;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = this;
        act = this;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.main);

        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
        cameraManager = new CameraManager(getApplication());
        viewfinderView.setCameraManager(cameraManager);

        preview = new Preview(this, (SurfaceView) findViewById(R.id.surfaceView), cameraManager);
        preview.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        ((FrameLayout) findViewById(R.id.layout)).addView(preview);
        preview.setKeepScreenOn(true);
        this.resetCam();


        // Set listener to change the size of the viewfinder rectangle.
        viewfinderView.setOnTouchListener(new View.OnTouchListener() {
            int lastX = -1;
            int lastY = -1;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastX = -1;
                        lastY = -1;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        int currentX = (int) event.getX();
                        int currentY = (int) event.getY();

                        try {
                            Rect rect = cameraManager.getFramingRect();

                            final int BUFFER = 50;
                            final int BIG_BUFFER = 60;
                            if (lastX >= 0) {
                                // Adjust the size of the viewfinder rectangle. Check if the touch event occurs in the corner areas first, because the regions overlap.
                                if (((currentX >= rect.left - BIG_BUFFER && currentX <= rect.left + BIG_BUFFER) || (lastX >= rect.left - BIG_BUFFER && lastX <= rect.left + BIG_BUFFER))
                                        && ((currentY <= rect.top + BIG_BUFFER && currentY >= rect.top - BIG_BUFFER) || (lastY <= rect.top + BIG_BUFFER && lastY >= rect.top - BIG_BUFFER))) {
                                    // Top left corner: adjust both top and left sides
                                    cameraManager.adjustFramingRect(2 * (lastX - currentX), 2 * (lastY - currentY));
                                } else if (((currentX >= rect.right - BIG_BUFFER && currentX <= rect.right + BIG_BUFFER) || (lastX >= rect.right - BIG_BUFFER && lastX <= rect.right + BIG_BUFFER))
                                        && ((currentY <= rect.top + BIG_BUFFER && currentY >= rect.top - BIG_BUFFER) || (lastY <= rect.top + BIG_BUFFER && lastY >= rect.top - BIG_BUFFER))) {
                                    // Top right corner: adjust both top and right sides
                                    cameraManager.adjustFramingRect(2 * (currentX - lastX), 2 * (lastY - currentY));
                                } else if (((currentX >= rect.left - BIG_BUFFER && currentX <= rect.left + BIG_BUFFER) || (lastX >= rect.left - BIG_BUFFER && lastX <= rect.left + BIG_BUFFER))
                                        && ((currentY <= rect.bottom + BIG_BUFFER && currentY >= rect.bottom - BIG_BUFFER) || (lastY <= rect.bottom + BIG_BUFFER && lastY >= rect.bottom - BIG_BUFFER))) {
                                    // Bottom left corner: adjust both bottom and left sides
                                    cameraManager.adjustFramingRect(2 * (lastX - currentX), 2 * (currentY - lastY));
                                } else if (((currentX >= rect.right - BIG_BUFFER && currentX <= rect.right + BIG_BUFFER) || (lastX >= rect.right - BIG_BUFFER && lastX <= rect.right + BIG_BUFFER))
                                        && ((currentY <= rect.bottom + BIG_BUFFER && currentY >= rect.bottom - BIG_BUFFER) || (lastY <= rect.bottom + BIG_BUFFER && lastY >= rect.bottom - BIG_BUFFER))) {
                                    // Bottom right corner: adjust both bottom and right sides
                                    cameraManager.adjustFramingRect(2 * (currentX - lastX), 2 * (currentY - lastY));
                                } else if (((currentX >= rect.left - BUFFER && currentX <= rect.left + BUFFER) || (lastX >= rect.left - BUFFER && lastX <= rect.left + BUFFER))
                                        && ((currentY <= rect.bottom && currentY >= rect.top) || (lastY <= rect.bottom && lastY >= rect.top))) {
                                    // Adjusting left side: event falls within BUFFER pixels of left side, and between top and bottom side limits
                                    cameraManager.adjustFramingRect(2 * (lastX - currentX), 0);
                                } else if (((currentX >= rect.right - BUFFER && currentX <= rect.right + BUFFER) || (lastX >= rect.right - BUFFER && lastX <= rect.right + BUFFER))
                                        && ((currentY <= rect.bottom && currentY >= rect.top) || (lastY <= rect.bottom && lastY >= rect.top))) {
                                    // Adjusting right side: event falls within BUFFER pixels of right side, and between top and bottom side limits
                                    cameraManager.adjustFramingRect(2 * (currentX - lastX), 0);
                                } else if (((currentY <= rect.top + BUFFER && currentY >= rect.top - BUFFER) || (lastY <= rect.top + BUFFER && lastY >= rect.top - BUFFER))
                                        && ((currentX <= rect.right && currentX >= rect.left) || (lastX <= rect.right && lastX >= rect.left))) {
                                    // Adjusting top side: event falls within BUFFER pixels of top side, and between left and right side limits
                                    cameraManager.adjustFramingRect(0, 2 * (lastY - currentY));
                                } else if (((currentY <= rect.bottom + BUFFER && currentY >= rect.bottom - BUFFER) || (lastY <= rect.bottom + BUFFER && lastY >= rect.bottom - BUFFER))
                                        && ((currentX <= rect.right && currentX >= rect.left) || (lastX <= rect.right && lastX >= rect.left))) {
                                    // Adjusting bottom side: event falls within BUFFER pixels of bottom side, and between left and right side limits
                                    cameraManager.adjustFramingRect(0, 2 * (currentY - lastY));
                                }
                            }
                        } catch (NullPointerException e) {
                        }
                        v.invalidate();
                        lastX = currentX;
                        lastY = currentY;
                        return true;
                    case MotionEvent.ACTION_UP:
                        lastX = -1;
                        lastY = -1;
                        return true;
                }
                return false;
            }
        });

        //  Initialize the Basic Data and Dirs
        this.initDataDirs();

        //  Take photo using the shutter button
        btnCapture = (ImageButton) findViewById(R.id.btnCapture);
        isBtnCaptureEnabled = true;

        btnCapture.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(CamActivity.this, "Realizando autoenfoque", Toast.LENGTH_SHORT).show();
                resetCam();
                return true;
            }
        });

        btnCapture.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                btnCapture.setEnabled(false);
                preview.cameraManager.getCamera().takePicture(shutterCallback, rawCallback, jpegCallback);
            }
        });

        // Creating references to the views
        infoView = (RelativeLayout) findViewById(R.id.infoView);
        int heightScreen = cameraManager.getScreenResolution().y / 2;
        infoView.getLayoutParams().height = (heightScreen >= 250) ? heightScreen : 250;
        infoView.requestLayout();
        loadingView = (LinearLayout) findViewById(R.id.loadingView);
        chargingView = (LinearLayout) findViewById(R.id.chargingView);
        editChargeCode = (EditText) findViewById(R.id.chargeCode);
        editChargeCode.setText("");
        btnCapture = (ImageButton) findViewById(R.id.btnCapture);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        menu = (RelativeLayout) findViewById(R.id.viewMenu);
        hideMenu = true;

        FrameLayout.LayoutParams rlParams =
                (FrameLayout.LayoutParams) menu.getLayoutParams();
        rlParams.setMargins(cameraManager.getScreenResolution().x, 0, -1 * cameraManager.getScreenResolution().x, 0);
        menu.setLayoutParams(rlParams);
        menu.requestLayout();
//        progressBar.setProgressDrawable(getResources().getDrawable(R.color.color_blue));
//        progressBar.requestLayout();

        //  Guardando datos por defecto en la BD
        this.storeBasicsPhoneData();

        // Store default Shared Preferences
        this.initPreferences();

        // Presentations of Paints Sliding
        this.beginPresentation();
    }

    public void hideToggleViewFinder(View v) {
        viewfinderView.setVisibility(viewfinderView.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
    }

    public void hideToggleMenu(View v) {
        if (hideMenu) {
            transMenu = new TranslateAnimation(cameraManager.getScreenResolution().x, 0, 0, 0);
        } else {
            transMenu = new TranslateAnimation(-1 * cameraManager.getScreenResolution().x, 0, 0, 0);
        }
        transMenu.setDuration(300);
        menu.startAnimation(transMenu);

        if (hideMenu) {
            FrameLayout.LayoutParams rlParams =
                    (FrameLayout.LayoutParams) menu.getLayoutParams();
            rlParams.setMargins(0, 0, 0, 0);
            menu.setLayoutParams(rlParams);
            menu.requestLayout();
        } else {
            FrameLayout.LayoutParams rlParams =
                    (FrameLayout.LayoutParams) menu.getLayoutParams();
            rlParams.setMargins(cameraManager.getScreenResolution().x, 0, -1 * cameraManager.getScreenResolution().x, 0);
            menu.setLayoutParams(rlParams);
            menu.requestLayout();
        }
        hideMenu = hideMenu ? false : true;

        ImageButton btnMenu = (ImageButton) findViewById(R.id.btnMenu);
        btnMenu.setVisibility(btnMenu.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);

//        ImageButt+lose.setVisibility(btnMenuClose.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);

        //menu.setVisibility(menu.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);

        hideToggleViewFinder(v);
        btnCapture.setEnabled(isBtnCaptureEnabled ? false : true);
        isBtnCaptureEnabled = isBtnCaptureEnabled ? false : true;
    }

    public void launchActivate(View v) {
        hideToggleMenu(v);
        CamActivity.this.startActivityForResult(new Intent(CamActivity.this, Activate.class), CODIGO_ACTIVAR);
    }

    public void launchSetting(View v) {
        hideToggleMenu(v);
        CamActivity.this.startActivity(new Intent(CamActivity.this, SettingsActivity.class));
    }

    public void launchAbout(View v) {
        hideToggleMenu(v);
        CamActivity.this.startActivity(new Intent(CamActivity.this, About.class));
    }

    public void initPreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        if (!prefs.contains("pref_first_time")) {
            editor.putBoolean("pref_first_time", true);
        }
        if (!prefs.contains("pref_error_margin")) {
            editor.putString("pref_error_margin", "0");
        }
        if (!prefs.contains("pref_delay_autofocus")) {
            editor.putString("pref_delay_autofocus", "5000");
        }
        if (!prefs.contains("pref_show_introduction")) {
            editor.putBoolean("pref_show_introduction", true);
        }
        if (!prefs.contains("pref_first_time") || !prefs.contains("pref_error_margin")
                || !prefs.contains("pref_delay_autofocus") || !prefs.contains("pref_show_introduction")) {
            editor.commit();
        }
    }

    public void beginPresentation() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        boolean firstTime = prefs.getBoolean("pref_first_time", true);
        boolean showPreset = prefs.getBoolean("pref_show_introduction", false);

        if (showPreset) {
            if (firstTime) {
                editor.putBoolean("pref_first_time", false);
                editor.putBoolean("pref_show_introduction", false);
                editor.commit();
            }
            //  show the App Intro
            Intent appIntro = new Intent(CamActivity.this, Intro.class);
            startActivity(appIntro);
        }
    }

    public void storeBasicsPhoneData() {
        ArrayList<String> data = TelephonData.getBasicsPhoneData(this);
        SQLiteDatabase dbStatus = this.dbH.getWritableDatabase();
        if (dbStatus != null) {
            Cursor c = dbStatus.rawQuery("SELECT Count(*) FROM Status WHERE " + dbH.id + "=1", null);
            if (c.moveToFirst()) {
                if (c.getInt(0) <= 0) {
                    dbStatus.execSQL("INSERT INTO Status (" + dbH.id + ", " + dbH.telefono + ", "
                            + dbH.imei + ", " + dbH.codigoactiv + ") " +
                            "VALUES (1, '" + Cryptography.encryptIt(data.get(0)) + "'," +
                            "'" + Cryptography.encryptIt(data.get(1)) + "','')");
                }
            } else {
                dbStatus.execSQL("INSERT INTO Status (" + dbH.id + ", " + dbH.telefono + ", "
                        + dbH.imei + ", " + dbH.codigoactiv + ") " +
                        "VALUES (1, '" + Cryptography.encryptIt(data.get(0)) + "'," +
                        "'" + Cryptography.encryptIt(data.get(1)) + "','')");
            }
        }
        dbStatus.close();

        //  Asking for basic data stored in DB
        ArrayList<String> dataDB = TelephonData.getBasicsPhoneDataFromDB(dbH);
        //  Requesting Phone number
        if (dataDB.get(0).isEmpty() || dataDB.get(0).length() != 8 || dataDB.get(0).charAt(0) != '5') {
            startActivityForResult(new Intent(CamActivity.this, ConfigurePhoneNumber.class), CODIGO_CPHONENUMBER);
        }
    }

    private void initDataDirs() {
        String[] paths = new String[]{DATA_PATH, DATA_PATH + "tessdata" + File.separator};

        //  Creating basic tree dirs
        for (String path : paths) {
            File dir = new File(path);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Toast.makeText(this, "Error al crear directorio \"tessdata\" en la SDCard", Toast.LENGTH_SHORT);
                    return;
                }
            }

        }

        // Asking for the Language Data and loading it to the App SDCard directory
        if (!(new File(DATA_PATH + "tessdata" + File.separator + lang + ".traineddata")).exists()) {
            try {

                AssetManager assetManager = getAssets();
                InputStream in = assetManager.open("tessdata" + File.separator + lang + ".traineddata");
                //GZIPInputStream gin = new GZIPInputStream(in);
                OutputStream out = new FileOutputStream(DATA_PATH + "tessdata" + File.separator + lang + ".traineddata");

                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                //while ((lenf = gin.read(buff)) > 0) {
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                //gin.close();
                out.close();

            } catch (IOException e) {
                Toast.makeText(this, "No se pudo copiar el archivo de idioma al directorio \"tessdata\"", Toast.LENGTH_SHORT);
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            preview.initCamera(preview.mHolder);
            preview.cameraManager.startPreview();
        } catch (RuntimeException ex) {
            Toast.makeText(ctx, getString(R.string.camera_not_found), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPause() {
//        if (camera != null) {
        preview.cameraManager.stopPreview();
        preview.cameraManager.closeDriver();
//            camera.release();
//            camera = null;
//        }
        super.onPause();
    }

    private void resetCam() {
        //  stopping
        preview.cameraManager.stopPreview();
        preview.cameraManager.closeDriver();

        //  starting
        preview.initCamera(preview.mHolder);
        preview.cameraManager.startPreview();
    }

    private void refreshGallery(File file) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(Uri.fromFile(file));
        sendBroadcast(mediaScanIntent);
    }

    ShutterCallback shutterCallback = new ShutterCallback() {
        public void onShutter() {
            //			 Log.d(TAG, "onShutter'd");
        }
    };

    PictureCallback rawCallback = new PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            //			 Log.d(TAG, "onPictureTaken - raw");
        }
    };

    PictureCallback jpegCallback = new PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {

            FileOutputStream outStream = null;

            // Write to SD Card
            try {
                File dir = new File(DATA_PATH);

                String fileName = System.currentTimeMillis() + ".jpg";
                outFile = new File(dir, fileName);

                outStream = new FileOutputStream(outFile);
                outStream.write(data);
                outStream.flush();
                outStream.close();

                refreshGallery(outFile);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
            }
            resetCam();

            new ProgressSpinnerTask().execute();
        }
    };

    private void doOCR() {
        String _path = outFile.getPath();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;

        Bitmap bitmap = BitmapFactory.decodeFile(_path, options);

        try {
            ExifInterface exif = new ExifInterface(_path);
            int exifOrientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            int rotate = 0;

            switch (exifOrientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
            }

            if (rotate != 0) {

                // Getting width & height of the given image.
                int w = bitmap.getWidth();
                int h = bitmap.getHeight();

                // Setting pre rotate
                Matrix mtx = new Matrix();
                mtx.preRotate(rotate);

                // Rotating Bitmap
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, false);
            }

            //  Convert to ARGB_8888, required by tess
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

            //  Selecting framing rect area
            Rect fRect = cameraManager.getFramingRectInPreview(true);

//            ((TextView) findViewById(R.id.data)).setText(
//                    "-----ScreenResolution-----"
//                            + "\nX: " + cameraManager.getScreenResolution().x
//                            + "\nY: " + cameraManager.getScreenResolution().y
//                            + "\n\n-----CameraResolution-----"
//                            + "\nX: " + cameraManager.getCameraResolution().x
//                            + "\nY: " + cameraManager.getCameraResolution().y
//                            + "\n\n-----Bitmap-----"
//                            + "\nWidth: " + bitmap.getWidth()
//                            + "\nHeight: " + bitmap.getHeight()
//                            + "\nWidthCalc: " + (fRect.right - fRect.left)
//                            + "\nHeightCalc: " + (fRect.bottom - fRect.top)
//                            + "\n\n-----Framing-----"
//                            + "\nLeft: " + fRect.left
//                            + "\nRight: " + fRect.right
//                            + "\nTop: " + fRect.top
//                            + "\nBottom: " + fRect.bottom);

            Matrix matrix = new Matrix();
            matrix.postScale(1f, 1f);
            bitmap = Bitmap.createBitmap(bitmap, fRect.left, fRect.top, fRect.right - fRect.left, fRect.bottom - fRect.top, matrix, true);
            bitmap = BitmapHandler.convertBitmapGreyScale(bitmap);

//            //  guardando la foto con los cambios [segmentacion]
//            FileOutputStream out = null;
//            try {
//                File dir = new File(DATA_PATH);
//
//                String fileName = System.currentTimeMillis()+ "seg" + ".jpg";
//                File outFile = new File(dir, fileName);
//
//                out = new FileOutputStream(outFile);
//                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
//                // PNG is a lossless format, the compression factor (100) is ignored
//
//                refreshGallery(outFile);
//            } catch (Exception e) {
//                e.printStackTrace();
//            } finally {
//                try {
//                    if (out != null) {
//                        out.flush();
//                        out.close();
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            //  FIN - guardando la foto con los cambios [segmentacion]


        } catch (IOException e) {
        }

        TessBaseAPI baseApi = new TessBaseAPI();
        baseApi.setDebug(true);
        //  Cambiando a modo numerico
        baseApi.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "0123456789");
        String characters = "!?@#$%&*()<>_-+=/.,:;'\"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        baseApi.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST, characters);
//        baseApi.setVariable("classify_bln_numeric_mode", "1");
        baseApi.init(DATA_PATH, lang);
        baseApi.setImage(bitmap);

        String recognizedText = baseApi.getUTF8Text().replaceAll("[a-zA-z]", "").trim();
        recognizedText = this.cleanChargeCode(recognizedText);

        baseApi.end();

        //  deleting the file of the device
        outFile.delete();
        refreshGallery(outFile);

        // You now have the text in recognizedText var, you can do anything with it.
        // We will display a stripped out trimmed alpha-numeric version of it (if lang is eng)
        // so that garbage doesn't make it to the display.

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int error = Integer.parseInt(prefs.getString("pref_error_margin", "0"));

        if (recognizedText.length() >= (16 - error) && recognizedText.length() <= (16 + error)) {
            editChargeCode.setText(recognizedText);
        } else {
            editChargeCode.setText("");
        }
    }


    /**
     * Displays an error message dialog box to the user on the UI thread.
     *
     * @param title   The title for the dialog box
     * @param message The error message to be displayed
     */
    void showErrorMessage(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .show();
    }

    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return false;
        } catch (NullPointerException e) {
            return false;
        }
        // only got here if we didn't return false
        return true;
    }

    public String cleanChargeCode(String chargeCode) {
        String finalCode = "";
        String charActual = "";
        for (int i = 0; i < chargeCode.length(); i++) {
            charActual = String.valueOf(chargeCode.charAt(i));
            if (isInteger(charActual) || charActual == " ") {
                finalCode += charActual;
            }
        }
        return finalCode.trim();
    }


    public void closeView(View v) {
        loadingView.setVisibility(View.GONE);
        chargingView.setVisibility(View.GONE);
        infoView.setVisibility(View.GONE);
        btnCapture.setVisibility(View.VISIBLE);
        viewfinderView.setVisibility(View.VISIBLE);
    }

    public void recharge(View v) {
        //  Executing the USSD code
        String chargeCode = editChargeCode.getText().toString().trim().replaceAll("\\s", "");

        // Codigo de activacion en BD
        String codBD = TelephonData.getCodigoActivacionFromDB(dbH);
        // Codigo de activacion generado por el telefono
        String codGen = TelephonData.getCodigoActivacion(dbH);

        if (!codBD.equals("") && codBD.equals(codGen)) {
            if (chargeCode.length() != 16) {
                Toast.makeText(getApplicationContext(), "C\u00f3digo incorrecto.", Toast.LENGTH_SHORT).show();
            } else {
                String encodedNumSimbol = Uri.encode("#");
                String ussd = "*" + "662" + "*" + chargeCode + encodedNumSimbol;
                startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:" + ussd)));
                this.closeView(v);
            }
        } else {
            CamActivity.this.startActivityForResult(new Intent(CamActivity.this, Activate.class), CODIGO_ACTIVAR);
        }

    }

    class ProgressSpinnerTask extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected void onPreExecute() {
            infoView.setVisibility(View.VISIBLE);
            loadingView.setVisibility(View.VISIBLE);
            chargingView.setVisibility(View.GONE);
            btnCapture.setVisibility(View.GONE);
            viewfinderView.setVisibility(View.GONE);
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                doOCR();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            String hasOCR = editChargeCode.getText().toString().trim();
            if (!hasOCR.isEmpty()) {
                loadingView.setVisibility(View.GONE);
                chargingView.setVisibility(View.VISIBLE);
            } else {
                infoView.setVisibility(View.GONE);
                loadingView.setVisibility(View.GONE);
                chargingView.setVisibility(View.GONE);
                btnCapture.setVisibility(View.VISIBLE);
                viewfinderView.setVisibility(View.VISIBLE);
                messages();
            }
            btnCapture.setEnabled(true);
            super.onPostExecute(result);
        }
    }

    public void messages() {
        Toast.makeText(getApplicationContext(), "C\u00f3digo de recarga inv\u00e1lido\nVerifique la luminosidad y enfoque de la c\u00e1mara", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case CODIGO_ACTIVAR:
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "La aplicaci\u00f3n se activ\u00f3 con \u00e9xito!!!", Toast.LENGTH_SHORT).show();
                }
                break;
            case CODIGO_CPHONENUMBER:
                if (resultCode == Activity.RESULT_CANCELED) {
                    this.finish();
                }
                break;
        }
    }

}


