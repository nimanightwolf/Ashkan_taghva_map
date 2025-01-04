package com.app.dadepardazan.find_c_point_map;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class SettingActivity extends AppCompatActivity {
    SharedPreferences setting;
    Switch switch_unit;
    TextView tv_pass;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        setting = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        findviewid();
        if (setting.getString("unit", "").equals("DMS")) {
            switch_unit.setChecked(true);
        } else {
            switch_unit.setChecked(false);
        }
        switch_unit.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                setResult(RESULT_OK);
                SharedPreferences.Editor editor = setting.edit();
                editor.putString("unit", "DMS");
                editor.apply();
                editor.commit();
            } else {
                setResult(RESULT_OK);
                SharedPreferences.Editor editor = setting.edit();
                editor.putString("unit", "");
                editor.apply();
                editor.commit();
            }
        });

        tv_pass.setOnClickListener(v -> {
            dialogPass();
        });


    }

    private void findviewid() {
        switch_unit = findViewById(R.id.switch_unit);
        tv_pass = findViewById(R.id.tv_pass);
    }

    private void dialogPass() {
        // Create an EditText to input the angle
        final EditText input = new EditText(this);
        final EditText input2 = new EditText(this);
        final LinearLayout linear = new LinearLayout(this);
        linear.setOrientation(LinearLayout.VERTICAL);

        input.setHint("نام کاربری جدید را وارد کنید");
        input2.setHint("رمز ورود جدید را وارد  کنید");


        // Create an AlertDialog for angle input
        AlertDialog.Builder angleDialog = new AlertDialog.Builder(this);
        angleDialog.setTitle("تغییر نام کاربری و رمز ورود");
        linear.addView(input);
        linear.addView(input2);
        angleDialog.setView(linear);

        // Set positive button to submit the angle
        angleDialog.setPositiveButton("تایید", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if ((!input2.getText().toString().isEmpty()) & (!input.getText().toString().isEmpty())) {

                    savepref(input.getText().toString(), input2.getText().toString());

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

    private void savepref(String username, String pass) {

        SharedPreferences.Editor editor = setting.edit();
        editor.putString("user_name", username);    // Saving float
        editor.putString("password", pass);    // Saving float
        editor.apply();
        editor.commit();
    }

}
