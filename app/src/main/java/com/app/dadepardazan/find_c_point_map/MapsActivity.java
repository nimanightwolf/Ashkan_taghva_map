package com.app.dadepardazan.find_c_point_map;

import static android.content.ContentValues.TAG;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.dadepardazan.find_c_point_map.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;

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
    ImageView find_place;
    final Handler handler = new Handler();
    boolean is_run = true;

    LatLng sydney;
    TextView tv1;
    SharedPreferences setting;
    TextView tv2;
    TextView tv3;
    TextView tv6;
    TextView tv7;
    TextView tv8;

    Switch lock_lock;
    TextView tv9;
    boolean is_lcok = true;
    double angleA = 51.7;
    double angleB = 51.7;
    double sensor_angle = 51.7;
    double anglemen = 0;
    Polyline lineA = null;
    Polyline lineB = null;
    Polyline polyline;
    Marker markerA;
    Marker markerB;
    Marker markerC;
    Marker markerD;
    double distance = 1; // طول خط به متر
    double last_distance = 1500; // طول خط به متر
    boolean is_a_select = true;
    boolean is_a_select_device = true;
    ImageView image_add;
    ImageView image_minus;
    private LatLng pointA = new LatLng(32.623373, 51.636431);
    private LatLng pointB;
    private LatLng pointD;
    private LatLng endPointA;
    private LatLng endPointB;
    private LatLng pointC;
    final Runnable runnable_add = new Runnable() {
        @Override
        public void run() {
            if (is_a_select) {
                if (angleA < 360) {
                    angleA += .1;
                } else {
                    angleA = 0;
                }
                drawPointA();
            } else {
                if (angleB < 360) {
                    angleB += .1;
                } else {
                    angleB = 0;
                }
                drawPointB();
            }
            handler.postDelayed(this, 100);  // تکرار هر 100 میلی‌ثانیه

        }
    };
    final Runnable runnable_minus = new Runnable() {
        @Override
        public void run() {
            if (is_a_select) {
                if (angleA > 0) {
                    angleA -= .1;
                } else {
                    angleA = 360;
                }
                drawPointA();
            } else {
                if (angleB > 0) {
                    angleB -= .1;
                } else {
                    angleB = 360;
                }
                drawPointB();
            }
            handler.postDelayed(this, 100);  // تکرار هر 100 میلی‌ثانیه

        }
    };
    private double offset = 0;

    public static double calibrateCompass(double trueAngle, double sensorAngle) {
        // مرحله 1: محاسبه خطا
        double offset = trueAngle - sensorAngle;

        // مرحله 2: اعمال تصحیح
        double calibratedAngle = sensorAngle + offset;

        // مرحله 3: نرمال‌سازی زاویه
        calibratedAngle = (calibratedAngle + 360) % 360;

        return calibratedAngle;
    }

    public static double calculateAzimuth(double latA, double lonA, double latB, double lonB) {
        double dLon = Math.toRadians(lonB - lonA);
        double latA_rad = Math.toRadians(latA);
        double latB_rad = Math.toRadians(latB);

        double y = Math.sin(dLon) * Math.cos(latB_rad);
        double x = Math.cos(latA_rad) * Math.sin(latB_rad) -
                Math.sin(latA_rad) * Math.cos(latB_rad) * Math.cos(dLon);

        double azimuth = Math.toDegrees(Math.atan2(y, x));
        return (azimuth + 360) % 360; // زاویه همیشه مثبت
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.app.dadepardazan.find_c_point_map.databinding.ActivityMapsBinding binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setting = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
        findviewbyid();
        find_place = findViewById(R.id.find_place);

        lock_lock.setOnCheckedChangeListener((compoundButton, b) -> is_lcok = b);
        find_place.setOnClickListener(view -> {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(pointA, 18f);
            mMap.animateCamera(cameraUpdate);
            savepref();

        });
        lock_lock.setChecked(true);
        chose_a_b();
        add_minus_angle();

        tv6.setOnClickListener(v -> {
            if (is_a_select) {
                anglemen = angleA;
            } else {
                anglemen = angleB;
            }
        });
        offset = setting.getFloat("offset", 0.0f);


    }

    private void findviewbyid() {
        tv1 = findViewById(R.id.tv1);
        tv2 = findViewById(R.id.tv2);
        tv3 = findViewById(R.id.tv3);
        tv6 = findViewById(R.id.tv6);
        tv7 = findViewById(R.id.tv7);
        tv8 = findViewById(R.id.tv8);
        tv9 = findViewById(R.id.tv9);

        image_add = findViewById(R.id.image_add);
        image_minus = findViewById(R.id.image_minus);
        lock_lock = findViewById(R.id.lock_lock);
    }

    private void savepref() {
        if (pointA != null) {
            SharedPreferences.Editor editor = setting.edit();
            editor.putFloat("latA", (float) pointA.latitude);    // Saving float
            editor.putFloat("lonA", (float) pointA.longitude);    // Saving float
            editor.apply();
            editor.commit();
        }
    }

    private void getpref() {
        pointA = new LatLng(setting.getFloat("latA", 0.0f), setting.getFloat("lonA", 0.0f));
    }

    private void add_minus_angle() {
        image_add.setOnClickListener(v -> {
            if (is_a_select) {
                if (angleA < 360) {
                    angleA += .1;
                }
                drawPointA();
            } else {
                if (angleB < 360) {
                    angleB += .1;
                }
                drawPointB();
            }
        });
        image_add.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    handler.post(runnable_add);  // شروع افزایش مقدار
                    return true;
                case MotionEvent.ACTION_UP:
                    handler.removeCallbacks(runnable_add);  // توقف افزایش مقدار
                    return true;
            }
            return false;
        });
        image_minus.setOnClickListener(v -> {
            if (is_a_select) {
                if (angleA > 0) {
                    angleA -= .1;
                }
                drawPointA();
            } else {
                if (angleB > 0) {
                    angleB -= .1;
                }
                drawPointB();
            }

        });
        image_minus.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    handler.post(runnable_minus);  // شروع افزایش مقدار
                    return true;
                case MotionEvent.ACTION_UP:
                    handler.removeCallbacks(runnable_minus);  // توقف افزایش مقدار
                    return true;
            }
            return false;
        });
    }

    private void chose_a_b() {
        tv1.setOnClickListener(v -> {
            is_a_select = true;
            tv1.setBackground(getDrawable(R.drawable.background_select));
            tv2.setBackgroundColor(Color.parseColor("#57BD5B"));
        });

        tv2.setOnClickListener(v -> {
            is_a_select = false;
            tv2.setBackground(getDrawable(R.drawable.background_select));
            tv1.setBackgroundColor(Color.parseColor("#57BD5B"));
        });

    }

    public void calibrate(double trueAngle, double sensorAngle) {
        offset = trueAngle - sensorAngle;
        SharedPreferences.Editor editor = setting.edit();

        editor.putFloat("offset", (float) offset);
        editor.commit();
        editor.apply();
    }

    public double getCalibratedAngle(double sensorAngle) {
        double calibratedAngle = sensorAngle + offset;
        return (calibratedAngle + 360) % 360; // نرمال‌سازی زاویه
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.clear();
        getpref();
        sydney = new LatLng(32.6358828, 51.667056);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("موقعیت توپ"));
        // mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
//        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(new LatLngBounds(sydney), 17));
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(pointA, 18f);
        mMap.animateCamera(cameraUpdate);
        new send_info_user().execute();


        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {


                handleLongClick(latLng);

            }
        });
        mMap.setOnMapClickListener(latLng -> {


            // angleA=calibrateCompass(anglemen, sensor_angle);
        });

    }

    private void handleLongClick(LatLng latLng) {
        // Creating the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("انتخاب نقطه برای رسم");


        // Define the options for the dialog
        String[] options = {"نقطه دیده بان", "نقطه قبضه", "نقطه هدف", "کالیبره کردن"};

        // Set the options to the dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Handle the selected option

                switch (which) {
                    case 0:
                        choseManualAuto('A', which, latLng);
                        break;
                    case 1:
                        choseManualAuto('B', which, latLng);
                        //showAngleDialog('B');
                        break;
                    case 2:
                        choseManualAuto('C', which, latLng);
                        break;
                    case 3:
                        pointD = latLng;

                        double temp_ang = calculateAzimuth(pointA.latitude, pointA.longitude, pointD.latitude, pointD.longitude);
                        tv7.setText(temp_ang + "");
                        Log.e("pointed", latLng.latitude + "," + latLng.longitude);
                        anglemen = temp_ang;
                        calibrate(temp_ang, sensor_angle);
                        if (markerD != null) {
                            markerD.remove();
                        }
                        markerD = mMap.addMarker(new MarkerOptions().position(latLng).title("Point D"));
                        calibrate(temp_ang, sensor_angle);

                        handler.postDelayed(() -> {
                            markerD.remove();
                        }, 2500);


                        break;


                }

            }
        });

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void choseManualAuto(char point, int which2, LatLng latLng) {
        // Creating the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("انتخاب دستی یا از نقشه");


        // Define the options for the dialog
        String[] options = {"نقشه", "دستی"};

        // Set the options to the dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Handle the selected option

                switch (which) {
                    case 0:
                        handleLongClickFromMap(latLng, which2);
                        break;
                    case 1:
                        addManualPosition(point, which2);
                        break;


                }

            }
        });

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void handleLongClickFromMap(LatLng latLng, int which) {
        switch (which) {
            case 0:
                // User chose نقطه A
                //drawPoint('A');
                pointA = latLng;
                if (markerA != null) {
                    markerA.remove();
                }
                drawPointA();
                drawTriangle();
                // showAngleDialog('A');
                break;
            case 1:
                // User chose نقطه B
                //drawPoint('B');
                if (markerB != null) {
                    markerB.remove();
                }
                pointB = latLng;

                drawPointB();
                drawTriangle();
                //showAngleDialog('B');
                break;
            case 2:
                // User chose نقطه C
                // drawPoint('C');

                pointC = latLng;
                if (markerC != null) {
                    markerC.remove();
                }
                markerC = mMap.addMarker(new MarkerOptions().position(pointC).title("Point C").icon(BitmapDescriptorFactory.fromResource(R.drawable.target_icon)));
                //drawPointC();
                //  showAngleDialog('C');
                drawTriangle();
                break;
            case 3:
                pointD = latLng;

                double temp_ang = calculateAzimuth(pointA.latitude, pointA.longitude, pointD.latitude, pointD.longitude);
                tv7.setText(temp_ang + "");
                Log.e("pointed", latLng.latitude + "," + latLng.longitude);
                anglemen = temp_ang;
                calibrate(temp_ang, sensor_angle);
                if (markerD != null) {
                    markerD.remove();
                }
                markerD = mMap.addMarker(new MarkerOptions().position(latLng).title("Point D"));
                calibrate(temp_ang, sensor_angle);

                handler.postDelayed(() -> {
                    markerD.remove();
                }, 2500);


                break;


        }
    }

    private void drawTriangle() {

        if (pointA != null && pointB != null && pointC != null) {

            if (polyline != null) {
                polyline.remove();
            }
            polyline = mMap.addPolyline(
                    new PolylineOptions()
                            .add(pointA, pointB, pointC, pointA) // اضافه کردن نقاط به خط
                            .color(android.graphics.Color.BLUE) // رنگ خط
                            .width(5f) // عرض خط

            );


            tv8.setText("زاویه قبضه تا هدف:" + calculateBearing(pointB.latitude, pointB.longitude, pointC.latitude, pointC.longitude) + "\n" + "فاصله قبضه تا هدف:" + SphericalUtil.computeDistanceBetween(pointC, pointB));
            if (pointA != null && pointC != null) {
                tv3.setText("فاصله دیده بان تا هدف:" + SphericalUtil.computeDistanceBetween(pointC, pointA));
            }
            tv9.setText("طول جغرافیایی نقطه هدف:" + pointC.latitude + "\n" + "عرض جغرافیایی نقطه هدف:" + pointC.longitude);
        }

        // جابه‌جا کردن دوربین به اولین نقطه

    }

    private void addManualPosition(final char point, int which2) {
        // Create an EditText to input the angle
        final EditText input = new EditText(this);
        final EditText input2 = new EditText(this);
        final LinearLayout linear = new LinearLayout(this);
        linear.setOrientation(LinearLayout.VERTICAL);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input2.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("طول جغرافیایی " + " را وارد کنید");
        input2.setHint("عرض جغرافیایی" + " را وارد کنید");


        // Create an AlertDialog for angle input
        AlertDialog.Builder angleDialog = new AlertDialog.Builder(this);
        angleDialog.setTitle("وارد کردن دستی موقعیت ");
        linear.addView(input);
        linear.addView(input2);
        angleDialog.setView(linear);

        // Set positive button to submit the angle
        angleDialog.setPositiveButton("تایید", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if ((!input2.getText().toString().isEmpty()) & (!input.getText().toString().isEmpty())) {
                    LatLng temp = new LatLng(Double.valueOf(input.getText().toString()), Double.valueOf(input2.getText().toString()));

                    handleLongClickFromMap(temp, which2);
                }

            }
        });

        // Set negative button to cancel
        angleDialog.setNegativeButton("لغو", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Show the angle dialog
        angleDialog.show();
    }

    private void showAngleDialog(final char point) {
        // Create an EditText to input the angle
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("زاویه" + point + " را وارد کنید");
        if (point == 'A') {
            input.setText(angleA + "");
        } else if (point == 'B') {
            input.setText(angleB + "");
        }

        // Create an AlertDialog for angle input
        AlertDialog.Builder angleDialog = new AlertDialog.Builder(this);
        angleDialog.setTitle("وارد کردن زاویه برای نقطه " + point);
        angleDialog.setView(input);

        // Set positive button to submit the angle
        angleDialog.setPositiveButton("تایید", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String angle = input.getText().toString();
                if (point == 'A') {
                    angleA = Double.valueOf(angle);
                    drawPointA();
                } else if (point == 'B') {
                    angleB = Double.valueOf(angle);
                    drawPointB();
                }
                // Handle the angle input (convert to float or int as needed)
                // processAngle(point, angle);
            }
        });

        // Set negative button to cancel
        angleDialog.setNegativeButton("لغو", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Show the angle dialog
        angleDialog.show();
    }

    private void drawPointA() {
        if (lineA != null) {
            // lineA.remove();
            markerA.remove();
        }
        markerA = mMap.addMarker(new MarkerOptions().position(pointA).title("Point A").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_myplace)));
        // بزرگنمایی به نقطه انتخاب‌شده
        // mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        endPointA = SphericalUtil.computeOffset(pointA, distance, angleA);
        pointC = endPointA;
        if (last_distance != distance) {
            if (lineA != null) {
                lineA.remove();
            }
            lineA = mMap.addPolyline(new PolylineOptions()
                    .add(pointA, pointC)
                    .width(5)
                    .color(Color.RED));
            last_distance = distance;
        }
        if (markerC != null) {
            markerC.remove();

        }
        if (distance > 0)
            markerC = mMap.addMarker(new MarkerOptions().position(pointC).title("Point C").icon(BitmapDescriptorFactory.fromResource(R.drawable.target_icon)));
        //  markerC = mMap.addMarker(new MarkerOptions().position(endPointA).title("Point C"));
        tv3.setText("فاصله: دیده بان تا هدف:" + distance);
        tv1.setText("طول جغرافیایی نقطه دیده بان:" +

                +pointA.latitude +
                "\n" +
                " عرض جغرافیایی نقطه دیده بان:" + pointA.longitude +
                "زاویه:" + String.format("%.2f", angleA));
        if (pointC != null) {
            tv9.setText("طول جغرافیایی نقطه هدف:" + pointC.latitude + "\n" + "عرض جغرافیایی نقطه هدف:" + pointC.longitude);
        }

        //  lineA.setVisible(false);
        //find_draw_c_point();

    }

    private void drawPointB() {
        if (lineB != null) {
            lineB.remove();
            markerB.remove();
        }
        markerB = mMap.addMarker(new MarkerOptions().position(pointB).title("Point B").icon(BitmapDescriptorFactory.fromResource(R.drawable.fire_icon)));
        // بزرگنمایی به نقطه انتخاب‌شده
        // mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        endPointB = SphericalUtil.computeOffset(pointB, distance, angleB);
//        lineB = mMap.addPolyline(new PolylineOptions()
//                .add(pointB, endPointB)
//                .width(5)
//                .color(Color.BLUE));

        tv2.setText("طول جغرافیایی نقطه قبضه:" +
                +pointB.latitude +
                "\n" +
                " عرض جغرافیایی نقطه قبضه:" + pointB.longitude +
                "زاویه:" + String.format("%.2f", angleB));
        for (int i = 0; i < 50; i++) {


//        mMap.addCircle(
//                new CircleOptions()
//                        .center(pointB)
//                        .radius(i*20) // شعاع دایره
//                        .strokeColor(Color.RED) // رنگ لبه
//                        //.fillColor(0x30FF0000) // رنگ داخلی
//                        .strokeWidth(2f));
        }
        //  lineA.setVisible(false);
        //      find_draw_c_point();

    }

    public double calculateBearing(double lat1, double lon1, double lat2, double lon2) {
        // تبدیل درجه به رادیان
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLonRad = Math.toRadians(lon2 - lon1);

        // محاسبات y و x
        double y = Math.sin(deltaLonRad) * Math.cos(lat2Rad);
        double x = Math.cos(lat1Rad) * Math.sin(lat2Rad) -
                Math.sin(lat1Rad) * Math.cos(lat2Rad) * Math.cos(deltaLonRad);

        // محاسبه زاویه اولیه
        double initialBearing = Math.toDegrees(Math.atan2(y, x));

        // تبدیل زاویه به محدوده 0 تا 360 درجه
        return (initialBearing + 360) % 360;
    }

    void find_draw_c_point() {
        try {


            double x1 = pointA.longitude, y1 = pointA.latitude;
            double x2 = endPointA.longitude, y2 = endPointA.latitude;
            double x3 = pointB.longitude, y3 = pointB.latitude;
            double x4 = endPointB.longitude, y4 = endPointB.latitude;

            double denominator = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
            if (denominator == 0) {
                // خطوط موازی هستند و تقاطعی ندارند
                Toast.makeText(this, "خطوط موازی هستند و تقاطعی ندارند", Toast.LENGTH_SHORT).show();

            } else {

                double ua = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3)) / denominator;
                double x = x1 + ua * (x2 - x1);
                double y = y1 + ua * (y2 - y1);
                pointC = new LatLng(y, x);
                Log.e("pintc", pointC.latitude + "," + pointC.longitude);
                if (markerC != null) {
                    markerC.remove();
                }
                double distanceCA = SphericalUtil.computeDistanceBetween(pointA, pointC); // فاصله به متر
                if (distanceCA > 10000) {
                    // نمایش پیام به کاربر
                    Toast.makeText(this, "فاصله بین C و A بیشتر از 10 کیلومتر است!", Toast.LENGTH_LONG).show();
                } else {

                    markerC = mMap.addMarker(new MarkerOptions().position(pointC).title("Point C").icon(BitmapDescriptorFactory.fromResource(R.drawable.target_icon)));


                    //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pointC, 15));
                }


            }
            distances();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void distances() {
        double distanceAB = SphericalUtil.computeDistanceBetween(pointA, pointB);
        double distanceCA = SphericalUtil.computeDistanceBetween(pointC, pointA);
        double distanceCB = SphericalUtil.computeDistanceBetween(pointC, pointB);
        double distanceCD = SphericalUtil.computeDistanceBetween(pointC, pointD);

        tv3.setText("فاصله: AB:" + String.format("%.2f", distanceAB) + " AC:" + String.format("%.2f", distanceCA) + "\n" + " BC:" + String.format("%.2f", distanceCB) + " CD:" + String.format("%.2f", distanceCD));
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
                    public void onResponse(Object response) {
                        Log.d(TAG, response.toString());
//                        pDialog.hide();
                    }
                },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.d(TAG, "Error: "
                                + error.getMessage());
//                        pDialog.hide();
                    }
                }) {

            @Override
            protected Map getParams() {
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

    @Override
    protected void onPause() {

        is_run = false;
        super.onPause();
    }

    @Override
    protected void onResume() {

        is_run = true;
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        is_run = false;
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
                HttpPost httppost = new HttpPost("http://192.168.4.1/location");
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
                    public void run() {
                        try {
                            if (response.contains("{") && response.contains("}")) {
                                String respose2 = response.substring(response.indexOf("{"), response.indexOf("}") + 1);
                                JSONObject jsonObject = null;

                                jsonObject = new JSONObject(respose2);
                                if (jsonObject.getString("isA").equals("A")) {
                                    is_a_select_device = true;
                                } else {
                                    is_a_select_device = false;
                                }


                                sydney = new LatLng(jsonObject.getDouble("lat"), jsonObject.getDouble("lon"));
                                double temp_angle = 0;
//                                if (jsonObject.getDouble("angle")>0) {
                                sensor_angle = jsonObject.getDouble("angle");
//                                }else
//                                    temp_angle= jsonObject.getDouble("angle")+360;
//                                temp_angle=-360+temp_angle-anglemen;

                                if (is_a_select_device) {
                                    pointA = sydney;
                                    angleA = getCalibratedAngle(sensor_angle);
                                    //  angleA=temp_angle;
                                    distance = jsonObject.getDouble("distance");
                                    drawPointA();
                                } else {
                                    pointB = sydney;
                                    angleB = temp_angle;
                                    distance = jsonObject.getDouble("distance");
                                    drawPointB();
                                }


                                // mMap.clear();
                                //  mMap.addMarker(new MarkerOptions().position(sydney).title("موقعیت توپ"));
                                //tv1.setText("طول جغرافیایی: " + jsonObject.getString("lat") + "\n" + "عرض جغرافیایی: " + jsonObject.getString("long"));
                                // mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
//        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(new LatLngBounds(sydney), 17));
                                if (is_lcok) {
                                    if (pointC != null) {
                                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(pointC, 18f);
                                        mMap.animateCamera(cameraUpdate);
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Handler h = new Handler(getMainLooper());
                        if (is_run) {
                            h.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    new send_info_user().execute();
                                }
                            }, 1000);
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getBaseContext(), "خطا در دریافت اطلاعات", Toast.LENGTH_SHORT).show();
                        Handler h = new Handler(getMainLooper());

                        if (is_run) {
                            h.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    new send_info_user().execute();
                                }
                            }, 1000);
                        }
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
}