package com.example.blogapp.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.blogapp.Adaptor.SavedArticleAdaptor;
import com.example.blogapp.Model.BlogItemModel;
import com.example.blogapp.R;
import com.example.blogapp.databinding.ActivitySavedArticleBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.LinkedList;
import java.util.List;

public class SavedArticleActivity extends AppCompatActivity {
    private ActivitySavedArticleBinding binding;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private FirebaseUser currUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySavedArticleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // database
        auth = FirebaseAuth.getInstance();
        database= FirebaseDatabase.getInstance();
        currUser=auth.getCurrentUser();

        if(currUser==null){
            Toast.makeText(this, "No internet Connection !!", Toast.LENGTH_SHORT).show();
            return;
        }

        // handle toolbar
        setSupportActionBar(binding.toolbarLayout.generalToolbar);
        if(getSupportActionBar()!=null){
            getSupportActionBar().setTitle("Bookmark");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


        // Adaptor
        List<BlogItemModel> savedArticleList = new LinkedList<>();
        SavedArticleAdaptor adaptor = new SavedArticleAdaptor(SavedArticleActivity.this,savedArticleList);
        binding.recycleView.setAdapter(adaptor);
        binding.recycleView.setLayoutManager(new LinearLayoutManager(SavedArticleActivity.this));

        DatabaseReference userReference = database.getReference().child("User").child(currUser.getUid()).child("saveArticle");
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                savedArticleList.clear();
                    for(DataSnapshot postSnapshot :snapshot.getChildren()){
                        String postId = postSnapshot.getKey();
                        DatabaseReference blogReference = database.getReference().child("blog").child(postId);
                        blogReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                BlogItemModel item= snapshot.getValue(BlogItemModel.class);
                                if(item!=null){
                                    savedArticleList.add(item);
                                    adaptor.notifyDataSetChanged();
                                }
                            }


                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(SavedArticleActivity.this,"No internet connection !!",Toast.LENGTH_SHORT).show();

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
        MenuInflater inflater= getMenuInflater();
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