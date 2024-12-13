package com.app.dadepardazan.find_c_point_map;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class Splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Handler h=new Handler(getMainLooper());
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i=new Intent(Splash.this,MapsActivity.class);
                startActivity(i);
                finish();
            }
        }, 2000);
    }
}