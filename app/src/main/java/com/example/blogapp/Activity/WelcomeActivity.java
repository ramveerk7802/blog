package com.example.blogapp.Activity;

import static android.os.Build.VERSION_CODES.R;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.example.blogapp.MainActivity;
import com.example.blogapp.R;
import com.example.blogapp.databinding.ActivityWelcomeBinding;

public class WelcomeActivity extends AppCompatActivity {
    private ActivityWelcomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWelcomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                finish();
            }
        },3000);
    }
}