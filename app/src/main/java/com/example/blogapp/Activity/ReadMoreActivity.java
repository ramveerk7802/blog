package com.example.blogapp.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.blogapp.Model.BlogItemModel;
import com.example.blogapp.R;
import com.example.blogapp.databinding.ActivityReadMoreBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ReadMoreActivity extends AppCompatActivity {
    ActivityReadMoreBinding binding;
    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReadMoreBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // handle database instance
        auth = FirebaseAuth.getInstance();
        database =FirebaseDatabase.getInstance();
        currentUser = auth.getCurrentUser();
        if(currentUser==null){
            Toast.makeText(ReadMoreActivity.this,"log in first !!",Toast.LENGTH_SHORT).show();
            return;
        }


        //handle toolbar
        setSupportActionBar(binding.readMoreToolbarLayout.generalToolbar);
        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.app_name);
        }

        // handle intent  from different activity
        Intent intent = getIntent();
        String postId = intent.getStringExtra("postId");

        if(intent.getBooleanExtra("fromYourArticle",false)){
            binding.likeBtn.setVisibility(View.GONE);
            binding.saveBtn.setVisibility(View.GONE);
            setData(intent);

        }
        else if(intent.getBooleanExtra("fromBookmark",false)){
            binding.saveBtn.setVisibility(View.GONE);
            binding.likeBtn.setVisibility(View.GONE);
            setData(intent);

        }
        // this intent from feed adaptor
        else{
            setData(intent);
            if(postId!=null && !postId.isEmpty()){
                updateLikeButton(postId);
                updateSaveButton(postId);
                binding.saveBtn.setOnClickListener(v -> {
                    handleSaveButton(postId);
                });
                binding.likeBtn.setOnClickListener(v -> {
                    handleLikeButton(postId);
                });
            }

        }









    }

    private void handleLikeButton(String postId) {
        DatabaseReference likeReference = database.getReference().child("User").child(currentUser.getUid()).child("likes");
        DatabaseReference blogReference = database.getReference().child("blog").child(postId);

        blogReference.child("likes")
                .child(currentUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //unlike
                        if(snapshot.exists()){
                            likeReference.child(postId)
                                    .removeValue()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            blogReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    BlogItemModel item = snapshot.getValue(BlogItemModel.class);
                                                    if(item!=null) {
                                                        blogReference.child("likeCount")
                                                                .setValue(item.getLikeCount() - 1)
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void unused) {
                                                                        blogReference.child("likes")
                                                                                .child(currentUser.getUid())
                                                                                .removeValue()
                                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                    @Override
                                                                                    public void onSuccess(Void unused) {
                                                                                        updateLikeButton(postId);
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
                        else{
                            likeReference.child(postId)
                                    .setValue(true)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            blogReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    BlogItemModel item = snapshot.getValue(BlogItemModel.class);
                                                    if(item!=null){
                                                        blogReference.child("likeCount")
                                                                .setValue(item.getLikeCount()+1)
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void unused) {
                                                                        blogReference.child("likes")
                                                                                .child(currentUser.getUid())
                                                                                .setValue(true)
                                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                    @Override
                                                                                    public void onSuccess(Void unused) {
                                                                                        updateLikeButton(postId);
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

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void updateLikeButton(String postId) {
        DatabaseReference likeReference = database.getReference().child("blog").child(postId).child("likes");
        likeReference.child(currentUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            binding.likeBtn.setImageResource(R.drawable.like_fill);
                            binding.likeBtn.setImageTintList(ContextCompat.getColorStateList(ReadMoreActivity.this,R.color.red));
                        }

                        else {
                            binding.likeBtn.setImageResource(R.drawable.like);
                            binding.likeBtn.setImageTintList(ContextCompat.getColorStateList(ReadMoreActivity.this,R.color.save_button_color));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void updateSaveButton(String postId) {
        DatabaseReference saveReference = database.getReference().child("blog").child(postId).child("saveArticle");
        saveReference.child(currentUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            binding.saveBtn.setIconTintResource(R.color.red);
                            binding.saveBtn.setTextColor(ContextCompat.getColor(ReadMoreActivity.this,R.color.red));
                        }
                        else
                        {
                            binding.saveBtn.setIconTintResource(R.color.save_button_color);
                            binding.saveBtn.setTextColor(ContextCompat.getColor(ReadMoreActivity.this,R.color.save_button_color));
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
    private void setData(Intent intent){
        String originalBloggerName = intent.getStringExtra("bloggerName");
        String title = intent.getStringExtra("blogTitle");
        String modifyName = originalBloggerName.substring(0,1).toUpperCase()+originalBloggerName.substring(1).toLowerCase();
        String modifyTitle = title.substring(0,1).toUpperCase()+title.substring(1).toLowerCase();
        binding.bloggerName.setText(modifyName);
        binding.blogTitleText.setText(modifyTitle);
        binding.dateText.setText(intent.getStringExtra("bloggingDate"));
        Picasso.get().load(intent.getStringExtra("bloggerImage")).into(binding.bloggerImage);
        binding.blogDesc.setText(intent.getStringExtra("blogDesc"));


    }

    private void handleSaveButton(String postId){
        DatabaseReference saveReference = database.getReference().child("User").child(currentUser.getUid()).child("saveArticle");
        DatabaseReference blogReference = database.getReference().child("blog").child(postId);
        saveReference.child(postId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            saveReference.child(postId)
                                    .removeValue()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            blogReference.child("saveArticle")
                                                    .child(currentUser.getUid())
                                                    .removeValue()
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            updateSaveButton(postId);
                                                        }
                                                    });
                                        }
                                    });
                        }else{
                            saveReference.child(postId)
                                    .setValue(true)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            blogReference.child("saveArticle")
                                                    .child(currentUser.getUid())
                                                    .setValue(true)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            updateSaveButton(postId);
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