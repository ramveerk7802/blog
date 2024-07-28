package com.example.blogapp.Activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintSet;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.blogapp.MainActivity;
import com.example.blogapp.Model.BlogItemModel;
import com.example.blogapp.Model.UserModel;

import com.example.blogapp.R;
import com.example.blogapp.databinding.ActivityAddBlogBinding;
import com.example.blogapp.databinding.DialogboxlayoutBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AddBlogActivity extends AppCompatActivity {
    private ActivityAddBlogBinding binding;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private FirebaseAuth auth;
    private final static int GALLERY_REQ_CODE = 1000;
    private Uri imageViewPath =null;
    private Dialog dialog;
    private BlogItemModel currArticle;

    // for image select from gallery

    private final ActivityResultLauncher galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),result->{
        if(result!=null){
            Picasso.get().load(result).into(binding.blogPostImageView);
            imageViewPath = result;
        }


    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivityAddBlogBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // handle the database
        database=FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();


        if(currentUser==null) {
            Toast.makeText(this,"Log in first !!",Toast.LENGTH_SHORT).show();
            return;
        }


        binding.blogPostImageView.setOnClickListener( v->{
            openGallery();
        });

        // dialog box handle

        dialog= new Dialog(AddBlogActivity.this);
        dialog.setContentView(R.layout.dialogboxlayout);
        dialog.setCancelable(false);

        // handle toolbar
        setSupportActionBar(binding.addBlogToolbarLayout.generalToolbar);
        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle("Add new article");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.updateBtn.setVisibility(View.GONE);

//        binding.blogDescText.setFocusable(true);
//        binding.blogDescText.setFocusableInTouchMode(true);

        // handle from edit
        if(getIntent().getBooleanExtra("fromEdit",false)){
            binding.addBlogBtn.setVisibility(View.GONE);
            binding.updateBtn.setVisibility(View.VISIBLE);
            if(getSupportActionBar()!=null)
                getSupportActionBar().setTitle("Update article ");
            binding.blogPostImageView.setVisibility(View.GONE);
            Intent intent= getIntent();
            String postId = intent.getStringExtra("postId");
            DatabaseReference blogReference = database.getReference().child("blog");
            if(postId!=null) {
                blogReference.child(postId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            BlogItemModel item = snapshot.getValue(BlogItemModel.class);
                            if(item!=null) {
                                binding.blogDescText.setText(item.getPost());
                                binding.blogTitleText.setText(item.getBlogTitle());
                                currArticle = item;
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            // handle the update button
            binding.updateBtn.setOnClickListener(v -> {
                dialog.show();
                if(binding.blogTitleText.getText().toString().trim().isEmpty() ||TextUtils.isEmpty(binding.blogTitleText.getText().toString().trim())|| TextUtils.isEmpty(binding.blogDescText.getText().toString().trim())){
                    Toast.makeText(this,"All fields are required !!",Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
                else {
                    BlogItemModel editArticle = new BlogItemModel();
                    editArticle.setSaved(currArticle.getSaved());
                    editArticle.setBlogPic(currArticle.getBlogPic());
                    editArticle.setImageView(currArticle.getImageView());
                    editArticle.setPostId(currArticle.getPostId());
                    editArticle.setPost(binding.blogDescText.getText().toString().trim());
                    editArticle.setBlogTitle(binding.blogTitleText.getText().toString().trim());
                    editArticle.setBloggerName(currArticle.getBloggerName());
                    editArticle.setLikeCount(currArticle.getLikeCount());
                    String currDate = new SimpleDateFormat("dd/MM/yy").format(new Date());
                    editArticle.setDate(currDate);
                    editArticle.setLiked(currArticle.getLiked());
                    if(postId!=null) {
                        blogReference.child(postId).removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        blogReference.child(postId).setValue(editArticle)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        Intent intent = new Intent(AddBlogActivity.this, YourArticleActivity.class);
                                                        intent.putExtra("fromEdit", true);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                });
                                    }
                                });
                    }

                }});





        }

        // handle the addBlog Button
        binding.addBlogBtn.setOnClickListener(v -> {
            dialog.show();
            DatabaseReference userReference = database.getReference().child("User").child(currentUser.getUid());
            DatabaseReference blogReference = database.getReference().child("blog");
            if(TextUtils.isEmpty(binding.blogTitleText.getText().toString().trim()) || TextUtils.isEmpty(binding.blogDescText.getText().toString().trim())){
                Toast.makeText(this,"All field are required !!",Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
            else if(imageViewPath.toString().isEmpty()){
                Toast.makeText(this,"Select the image !!",Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
            else{
                if(currentUser!=null){
                    Long timeStamp = new Date().getTime();
                    StorageReference storageReference = storage.getReference()
                            .child("blogPostImage")
                            .child(timeStamp.toString());
                    storageReference.putFile(imageViewPath)
                            .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                    if(task.isSuccessful()){
                                        storageReference.getDownloadUrl()
                                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                UserModel item = snapshot.getValue(UserModel.class);
                                                                String key = blogReference.push().getKey();
                                                                if(item!=null && key!=null){

                                                                    userReference.child("MyBlog").child(key).setValue(true)
                                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void unused) {
                                                                                    BlogItemModel blogData = new BlogItemModel();

                                                                                    blogData.setBlogPic(item.getProfilePic());
                                                                                    blogData.setBlogTitle(binding.blogTitleText.getText().toString().trim());
                                                                                    blogData.setPost(binding.blogDescText.getText().toString().trim());
                                                                                    blogData.setLikeCount(0);
                                                                                    blogData.setBloggerName(item.getDisplayName());

                                                                                    // current date
                                                                                    String currDate= new SimpleDateFormat("dd/MM/yy").format(new Date());
                                                                                    blogData.setDate(currDate);
                                                                                    blogData.setPostId(key);
                                                                                    blogData.setImageView(uri.toString());
                                                                                    blogData.setSaved(false);
                                                                                    blogData.setLiked(false);

                                                                                    blogReference.child(key).setValue(blogData)
                                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                                    if(task.isSuccessful()){
                                                                                                        dialog.dismiss();
                                                                                                        Toast.makeText(AddBlogActivity.this,"Blog upload successfully !!",Toast.LENGTH_SHORT).show();
                                                                                                        finish();
                                                                                                    }
                                                                                                }
                                                                                            });

                                                                                }
                                                                            });
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError error) {

                                                            }
                                                        });
                                                    }
                                                });
                                    }
                                }
                            });
                }
            }
        });


    }

    public void openGallery(){
        galleryLauncher.launch("image/*");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GALLERY_REQ_CODE && resultCode==RESULT_OK && data.getData()!=null)
            Picasso.get().load(data.getData().toString()).into(binding.blogPostImageView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.saved_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }
}