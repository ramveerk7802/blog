package com.example.blogapp.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.example.blogapp.MainActivity;
import com.example.blogapp.Model.UserModel;
import com.example.blogapp.R;
import com.example.blogapp.databinding.ActivitySignBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ktx.Firebase;

public class SignInActivity extends AppCompatActivity {

    private ActivitySignBinding binding;
    private FirebaseAuth auth;
    private Dialog dialog;
    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivitySignBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // handle the database
        auth =FirebaseAuth.getInstance();
        database =FirebaseDatabase.getInstance();

        // handle the  dialog box
        dialog = new Dialog(SignInActivity.this);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialogboxlayout);



        // handle the intent from logIn activity
        Intent intent = getIntent();

        if(intent!=null){
            if(intent.getBooleanExtra("logIn",false)){
                binding.logInBtn.setOnClickListener(v -> {
                    dialog.show();
                    if(TextUtils.isEmpty(binding.edtEmail.getText().toString().trim()) || TextUtils.isEmpty(binding.edtPassword.getText().toString().trim())){
                        Toast.makeText(SignInActivity.this,"All fields are required !!",Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else if (binding.edtPassword.getText().toString().trim().toString().length()<8) {
                        Toast.makeText(SignInActivity.this,"Password must be 8 character any Combination .!!",Toast.LENGTH_SHORT).show();
                        dialog.dismiss();

                    }
                    else{
                        String email = binding.edtEmail.getText().toString().trim();
                        String password = binding.edtPassword.getText().toString().trim();
                        auth.signInWithEmailAndPassword(email,password)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if(task.isSuccessful()){
                                            FirebaseUser user = auth.getCurrentUser();
                                            if(user!=null && user.isEmailVerified()){
                                                checkProfileCompletion(user.getUid(),email,password);
                                            }
                                            else {
                                                Toast.makeText(SignInActivity.this,"Verify your email !!",Toast.LENGTH_SHORT).show();
                                                dialog.dismiss();
                                            }

                                        }else {
                                            Toast.makeText(SignInActivity.this,"Register first then log in !!",Toast.LENGTH_SHORT).show();
                                            dialog.dismiss();
                                        }

                                    }
                                });
                    }

                });

            }
            else{
                binding.logInBtn.setText(R.string.register);
                binding.logInBtn.setOnClickListener(v -> {
                    dialog.show();
                    if(TextUtils.isEmpty(binding.edtEmail.getText().toString().trim()) || TextUtils.isEmpty(binding.edtPassword.getText().toString().trim())){
                        Toast.makeText(SignInActivity.this,"All fields are required !!",Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else if (binding.edtPassword.getText().toString().trim().toString().length()<8) {
                        Toast.makeText(SignInActivity.this,"Password must be 8 character any Combination .!!",Toast.LENGTH_SHORT).show();
                        dialog.dismiss();

                    }
                    else {
                        String email = binding.edtEmail.getText().toString().trim();
                        String password = binding.edtPassword.getText().toString().trim();
                        auth.createUserWithEmailAndPassword(email,password)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if(task.isSuccessful()){
                                            FirebaseUser user = auth.getCurrentUser();
                                            if(user!=null){
                                                user.sendEmailVerification()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    Toast.makeText(SignInActivity.this,"Please verify the email and check your email !!",Toast.LENGTH_LONG).show();
                                                                    startActivity(new Intent(SignInActivity.this, LoginActivity.class));
                                                                    finish();

                                                                }
                                                                else {
                                                                    Toast.makeText(SignInActivity.this,"Failed to sending the verification link  Please re-register !!",Toast.LENGTH_LONG).show();
                                                                }
                                                                dialog.dismiss();
                                                            }
                                                        });
                                            }
                                        }
                                        else{
                                            Toast.makeText(SignInActivity.this,task.getException().getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                });


                    }

                });

            }
        }

//        binding.logInBtn.setOnClickListener(v -> {
//            dialog.show();
//            if(TextUtils.isEmpty(binding.edtEmail.getText().toString().trim()) || TextUtils.isEmpty(binding.edtPassword.getText().toString().trim())){
//                Toast.makeText(SignInActivity.this,"All fields are mandatory !!",Toast.LENGTH_SHORT).show();
//                dialog.dismiss();
//            }
//            else {
//                    auth.signInWithEmailAndPassword(binding.edtEmail.getText().toString().trim(),binding.edtPassword.getText().toString().trim())
//                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//                                @Override
//                                public void onComplete(@NonNull Task<AuthResult> task) {
//                                    if(task.isSuccessful()){
//                                        dialog.dismiss();
//                                        startActivity(new Intent(SignInActivity.this, MainActivity.class));
//                                        finish();
//                                    }
//                                    else {
//                                        dialog.dismiss();
//                                        Toast.makeText(SignInActivity.this, "{ " + task.getException().toString() + " }", Toast.LENGTH_SHORT).show();
//                                    }
//                                }
//                            });
//            }
//        });
    }

    private void checkProfileCompletion(String uid, String email, String password) {

        DatabaseReference userReference  = database.getReference().child("User");

        userReference.child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            startActivity(new Intent(SignInActivity.this, MainActivity.class));
                        }
                        else{
                            Intent intent = new Intent(SignInActivity.this, ProfileActivity.class);
                            intent.putExtra("sendEmail", email);
                            intent.putExtra("sendPassword", password);
                            startActivity(intent);
                        }
                        finish();
                        dialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
}