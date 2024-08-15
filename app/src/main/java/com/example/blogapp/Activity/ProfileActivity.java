package com.example.blogapp.Activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.blogapp.MainActivity;
import com.example.blogapp.Model.UserModel;
import com.example.blogapp.R;
import com.example.blogapp.databinding.ActivityProfileBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

public class ProfileActivity extends AppCompatActivity {
    private static final int GALLERY_REQ_CODE=100;
    private ActivityProfileBinding binding;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private Uri imagePath;
    private Dialog dialog;



    private  final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),result->{
        if(result!=null){
            Picasso.get().load(result).into(binding.profilePic);
            imagePath=result;
        }
    });



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding =ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        

        //handle dataBase
        auth= FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        currentUser = auth.getCurrentUser();

        // handle the spinner
        ArrayList<String> genderList=new ArrayList<>();
        genderList.add("Male");
        genderList.add("Female");
        genderList.add("Other");

        ArrayAdapter<String> spinnerAdaptor = new ArrayAdapter<>(ProfileActivity.this,com.google.android.material.R.layout.support_simple_spinner_dropdown_item,genderList);
        binding.spinner.setAdapter(spinnerAdaptor);

        // dialog
        dialog = new Dialog(ProfileActivity.this);
        dialog.setContentView(R.layout.dialogboxlayout);
        dialog.setCancelable(false);


        String email = getIntent().getStringExtra("sendEmail");
        binding.edtEmail.setText(email);


        // handle profile pic
        binding.profilePic.setOnClickListener(v -> {
            galleryLauncher.launch("image/*");
        });

        // handle the next button
        binding.next.setOnClickListener(v -> {
            dialog.show();
            if(TextUtils.isEmpty(binding.edtName.getText().toString().trim()) || TextUtils.isEmpty(binding.edtEmail.getText().toString().trim()) ||
            TextUtils.isEmpty(binding.edtAge.getText().toString().trim())){
                Toast.makeText(ProfileActivity.this,"All fields are Mandatory !!",Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else if (imagePath==null || imagePath.toString().isEmpty()) {
                Toast.makeText(ProfileActivity.this,"Please select the image !!",Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                uploadPhoto(currentUser);
            }



        });



    }

    private void uploadPhoto(FirebaseUser currentUser) {
        if(currentUser!=null){
            Long timeStamp = new Date().getTime();
            StorageReference storageReference = storage.getReference().child("profile").child(timeStamp.toString());
            storageReference.putFile(imagePath)
                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful()){
                                storageReference.getDownloadUrl()
                                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                uploadUserInfo(uri);
                                            }


                                        });

                            }
                        }
                    });
        }
    }

    private void uploadUserInfo(Uri uri) {

        String email = getIntent().getStringExtra("sendEmail");
        String password = getIntent().getStringExtra("sendPassword");
        if(currentUser!=null){
            UserModel model = new UserModel();

            // setData
            model.setPassword(password);
            model.setProfilePic(uri.toString());
            model.setUserUid(currentUser.getUid());
            model.setEmail(email);
            model.setDisplayName(binding.edtName.getText().toString().trim());
            model.setAge(Integer.parseInt(binding.edtAge.getText().toString().trim()));
            model.setGender(binding.spinner.getSelectedItem().toString());
            DatabaseReference userReference = database.getReference().child("User");
            userReference.child(currentUser.getUid()).setValue(model)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            dialog.dismiss();
                            startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                            finish();

                        }
                    });
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GALLERY_REQ_CODE && resultCode==RESULT_OK && data!=null){
            Picasso.get().load(data.getData()).into(binding.profilePic);
        }
    }
}