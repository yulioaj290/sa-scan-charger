package com.surfacesoft.yaj.sascancharger;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;


public class About extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
    }

    public void closeWindows(View v) {
        this.finish();
    }

    public void contacting(View v) {
        Intent contactInt = new Intent(Intent.ACTION_SEND);
        contactInt.putExtra(Intent.EXTRA_EMAIL, "surfacesoft15@gmail.com");
        contactInt.putExtra(Intent.EXTRA_SUBJECT, "Opiniones SATransferUI");
        startActivity(contactInt);
    }
}
