package com.example.blogapp.Activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.TextUtilsCompat;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.example.blogapp.MainActivity;
import com.example.blogapp.Model.UserModel;
import com.example.blogapp.R;
import com.example.blogapp.databinding.ActivityRegisterBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.Date;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private static final int GALLERY_REQ_CODE=100;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private Uri imagePath;
    private Dialog dialog;
    private final ActivityResultLauncher galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),result ->{
        if(result !=null){
            Picasso.get().load(result).into(binding.profilePic);
            imagePath = result;

        }
    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        // handle the dialog
        dialog = new Dialog(RegisterActivity.this);
        dialog.setContentView(R.layout.dialogboxlayout);
        dialog.setCancelable(false);




        binding.profilePic.setOnClickListener(v -> {
            openGallery();

        });
        binding.registerBtn.setOnClickListener(v -> {
            dialog.show();
            if(TextUtils.isEmpty(binding.edtName.getText().toString().trim()) || TextUtils.isEmpty(binding.edtEmail.getText().toString()
                    .trim()) || TextUtils.isEmpty(binding.edtPassword.getText().toString().trim())){
                Toast.makeText(RegisterActivity.this,"All field are required !!",Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
            else if(imagePath.toString().isEmpty()){
                Toast.makeText(RegisterActivity.this,"Select the Profile Image",Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
            else{
                auth.createUserWithEmailAndPassword(binding.edtEmail.getText().toString().trim(),binding.edtPassword.getText().toString().trim())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()) {
                                    FirebaseUser user = auth.getCurrentUser();
                                    updateUI(user);
                                }
                                else
                                    Toast.makeText(RegisterActivity.this,task.getException().toString(),Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }
    public void openGallery(){
        galleryLauncher.launch("image/*");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GALLERY_REQ_CODE && resultCode==RESULT_OK && data.getData()!=null){
                Picasso.get().load(data.getData()).into(binding.profilePic);
        }
    }
    public void updateUI(FirebaseUser user){
        if(user!=null){
            Long time = new Date().getTime();
            StorageReference reference = storage.getReference().child("profile").child(time.toString());
            reference.putFile(imagePath)
                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful()){
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        uploadInfo(uri);
                                    }
                                });
                            }

                        }
                    });
        }
        else {
            Toast.makeText(RegisterActivity.this,"Not Registered",Toast.LENGTH_SHORT).show();
            return;
        }

    }
    public void uploadInfo(Uri uri){
        FirebaseUser currUser = auth.getCurrentUser();
        if(currUser!=null) {
            UserModel model = new UserModel();
            model.setEmail(binding.edtEmail.getText().toString().trim());
            model.setDisplayName(binding.edtName.getText().toString().trim());
            model.setProfilePic(uri.toString());
            model.setPassword(binding.edtPassword.getText().toString().trim());
            model.setUserUid(currUser.getUid());
            database.getReference().child("User").child(currUser.getUid()).setValue(model)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            dialog.dismiss();
                            Intent main= new Intent(RegisterActivity.this,MainActivity.class);
                            startActivity(main);
                            finish();

                        }
                    });
        }
    }
}