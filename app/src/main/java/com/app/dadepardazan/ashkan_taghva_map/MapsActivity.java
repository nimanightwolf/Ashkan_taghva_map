package com.app.dadepardazan.ashkan_taghva_map;

import static android.content.ContentValues.TAG;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.dadepardazan.ashkan_taghva_map.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class
MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    ImageView find_place;
    LatLng sydney;
    TextView tv1;
    Switch lock_lock;
    boolean is_lcok=true;
    boolean test = false;
    boolean test2 = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        find_place=findViewById(R.id.find_place);
        tv1=findViewById(R.id.tv1);
        lock_lock=findViewById(R.id.lock_lock);
        lock_lock.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                is_lcok=b;
            }
        });
        find_place.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(sydney, 18f);
                mMap.animateCamera(cameraUpdate);
            }
        });
        lock_lock.setChecked(true);

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
         sydney = new LatLng(32.6358828, 51.667056);
        mMap.addMarker(new MarkerOptions().position(sydney).title("موقعیت توپ"));
       // mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
//        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(new LatLngBounds(sydney), 17));
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(sydney, 18f);
        mMap.animateCamera(cameraUpdate);
        new send_info_user().execute();
    }
    public class send_info_user extends AsyncTask<Void, Void, String> {
        ProgressDialog pd = new ProgressDialog(MapsActivity.this);
        protected void onPreExecute() {
            super.onPreExecute();

            pd.setMessage("در حال دریافت اطلاعات");
            //pd.show();

        }
        @Override
        protected String doInBackground(Void... voids) {
            ArrayList<NameValuePair> namevaluepairs = new ArrayList<NameValuePair>();
            final JSONObject get_ad_list = new JSONObject();
            try {
                get_ad_list.put("limit", "10");
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            namevaluepairs.add(new BasicNameValuePair("myjson", get_ad_list.toString()));
            Log.e("rig_user", get_ad_list.toString());
//            Log.e("token", settings.getString("token", ""));
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost("http://192.168.1.1/get_status");
                httppost.setEntity(new UrlEncodedFormEntity(namevaluepairs, HTTP.UTF_8));
//                httppost.setHeader("Content-Type","application/json");
//                httppost.setHeader("Authorization", "Bearer " + settings.getString("token",""));
//                httppost.setHeader(HttpHeaders.CONTENT_TYPE,"application/json");
//                httppost.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + settings.getString("token",""));
                HttpResponse httpresponse = httpclient.execute(httppost);

                String response = EntityUtils.toString(httpresponse.getEntity());

                Log.e("data response", response);

                    final String finalResponse = response;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {try {
                            if (response.contains("{")&&response.contains("}")){
                                String respose2=response.substring(response.indexOf("{"), response.indexOf("}")+1);
                                JSONObject jsonObject= null;

                                    jsonObject = new JSONObject(respose2);

                                sydney = new LatLng(jsonObject.getDouble("lat"), jsonObject.getDouble("long"));
                                mMap.clear();
                                mMap.addMarker(new MarkerOptions().position(sydney).title("موقعیت توپ"));
                                tv1.setText("طول جغرافیایی: "+jsonObject.getString("lat")+"\n"+"عرض جغرافیایی: "+jsonObject.getString("long"));
                                // mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
//        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(new LatLngBounds(sydney), 17));
                                if (is_lcok) {
                                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(sydney, 18f);
                                    mMap.animateCamera(cameraUpdate);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                            Handler h = new Handler(getMainLooper());
                            h.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    new send_info_user().execute();
                                }
                            }, 1000);
                        }
                    });
            } catch (Exception e) {
                e.printStackTrace();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getBaseContext(), "خطا در دریافت اطلاعات", Toast.LENGTH_SHORT).show();
                        Handler h = new Handler(getMainLooper());
                        h.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                new send_info_user().execute();
                            }
                        }, 1000);
                    }
                });
            }
            return null;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            pd.hide();
            pd.dismiss();
        }
    }
    private void send_request() {


        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("در حال دریافت اطلاعات");
        progressDialog.setMessage("لطفا منتظر بمانید");
        progressDialog.show();
        String url = "http://0.0.0.0:5003/adc_samples";

// Request a string response from the provided URL.
//        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//                        // Display the first 500 characters of the response string.
//                        try {
//                            JSONObject jresult=new JSONObject(response);
//                            if (jresult.has("lat")&&jresult.has("lon")){
//
//                            }
//                            // Add a marker in Sydney and move the camera
//                            LatLng sydney = new LatLng(jresult.getDouble("lat"), jresult.getDouble("lon"));
//                            mMap.addMarker(new MarkerOptions().position(sydney).title("موقعیت توپ"));
//                            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(sydney, 18f);
//                            mMap.animateCamera(cameraUpdate);
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
////                        send_request();
//                    }
//                }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Toast.makeText(MapsActivity.this, "خطا در دریافت اطلاعات", Toast.LENGTH_SHORT).show();
////                send_request();
//            }
//        });

// Add the request to the RequestQueue.
         RequestQueue mRequestQueue;
        mRequestQueue = Volley.newRequestQueue(this);
        App app = (App) getApplication();
//        mRequestQueue.add(stringRequest);

        JsonObjectRequest
                jsonObjReq
                = new JsonObjectRequest(
                Request.Method.POST,
                url,
                null,
                new Response.Listener() {

                    @Override
                    public void onResponse(Object response)
                    {
                        Log.d(TAG, response.toString());
//                        pDialog.hide();
                    }
                },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        VolleyLog.d(TAG, "Error: "
                                + error.getMessage());
//                        pDialog.hide();
                    }
                }) {

            @Override
            protected Map getParams()
            {
                Map params = new HashMap();
                params.put("name", "Androidhive");
                params.put("email", "abc@androidhive.info");
                params.put("password", "password123");

                return params;
            }

        };

        app.requestQueue.add(jsonObjReq);
//                .addToRequestQueue(jsonObjReq, tag_json_obj);
    }
}