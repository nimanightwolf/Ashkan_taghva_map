package com.app.dadepardazan.find_c_point_map;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.app.dadepardazan.find_c_point_map.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {
    ActivityLoginBinding binding;
    SharedPreferences setting;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setting = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        //  setContentView(R.layout.activity_login);
        binding = ActivityLoginBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        binding.loginButton.setOnClickListener(v -> {
            if (binding.usernameEditText.getText().toString().equals(setting.getString("user_name", "psg")) && (binding.passwordEditText.getText().toString().equals(setting.getString("password", "123456789")))) {
                startActivity(new Intent(this, MapsActivity.class));
                finish();

            } else {
                Toast.makeText(this, "نام کاربری یا رمز ورود اشتباه است", Toast.LENGTH_SHORT).show();
            }
        });


    }
}
