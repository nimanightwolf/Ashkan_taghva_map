package com.app.dadepardazan.find_c_point_map;

import android.app.Application;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

class App  extends Application {
   public RequestQueue requestQueue;
   @Override
   public void onCreate() {
      super.onCreate();
      requestQueue = Volley.newRequestQueue(this);
   }
}
