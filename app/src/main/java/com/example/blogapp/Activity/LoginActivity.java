package com.example.blogapp.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.blogapp.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.logInBtn.setOnClickListener(v -> {
            Intent intent=new Intent(LoginActivity.this, SignInActivity.class);
            intent.putExtra("logIn",true);
            startActivity(intent);
            finish();
        });
        binding.registerBtn.setOnClickListener(v -> {
            Intent intent=new Intent(LoginActivity.this, SignInActivity.class);
            startActivity(intent);
            finish();
        });
    }
}