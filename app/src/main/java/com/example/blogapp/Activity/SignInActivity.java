package com.example.blogapp.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.example.blogapp.MainActivity;
import com.example.blogapp.databinding.ActivitySignBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.ktx.Firebase;

public class SignInActivity extends AppCompatActivity {

    private ActivitySignBinding binding;
    private FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivitySignBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        auth =FirebaseAuth.getInstance();
        binding.logInBtn.setOnClickListener(v -> {
            if(TextUtils.isEmpty(binding.edtEmail.getText().toString().trim()) || TextUtils.isEmpty(binding.edtPassword.getText().toString().trim())){
                Toast.makeText(SignInActivity.this,"All fields are mandatory !!",Toast.LENGTH_SHORT).show();
            }
            else {
                    auth.signInWithEmailAndPassword(binding.edtEmail.getText().toString().trim(),binding.edtPassword.getText().toString().trim())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        startActivity(new Intent(SignInActivity.this, MainActivity.class));
                                        finish();
                                    }
                                    else
                                        Toast.makeText(SignInActivity.this,"{ "+task.getException().toString()+" }",Toast.LENGTH_SHORT).show();
                                }
                            });
            }
        });
    }
}