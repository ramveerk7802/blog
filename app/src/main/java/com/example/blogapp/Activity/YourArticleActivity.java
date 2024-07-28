package com.example.blogapp.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.blogapp.Adaptor.YourArticleAdaptor;
import com.example.blogapp.Model.BlogItemModel;
import com.example.blogapp.R;
import com.example.blogapp.databinding.ActivityYourArticleBinding;
import com.example.blogapp.databinding.BlogLayoutBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.LinkedList;
import java.util.List;

public class YourArticleActivity extends AppCompatActivity {

    private ActivityYourArticleBinding binding;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private FirebaseUser currentUser;
    private FirebaseStorage storage;
    private List<BlogItemModel> list;
    private YourArticleAdaptor adaptor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivityYourArticleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //handle database
        auth=FirebaseAuth.getInstance();
        database=FirebaseDatabase.getInstance();
        currentUser=auth.getCurrentUser();
        storage = FirebaseStorage.getInstance();
        if(currentUser==null)
            return;

        //handle the toolbar
        setSupportActionBar(binding.YourArticleToolbarLayout.generalToolbar);
        if(getSupportActionBar()!=null){
            getSupportActionBar().setTitle("Your Article");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //handle the recycle View
        list= new LinkedList<>();
        adaptor = new YourArticleAdaptor(this,list);
        binding.YourArticleRecycleView.setAdapter(adaptor);
        binding.YourArticleRecycleView.setLayoutManager(new LinearLayoutManager(this));
        loadData();

    }


    private void loadData() {
        if(currentUser!=null){
            DatabaseReference userReference = database.getReference().child("User").child(currentUser.getUid()).child("MyBlog");
            DatabaseReference blogReference = database.getReference().child("blog");
            userReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    list.clear();
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        String postId = dataSnapshot.getKey();
                        blogReference.child(postId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                BlogItemModel item = snapshot.getValue(BlogItemModel.class);
                                if(item!=null) {
                                    list.add(item);
                                    adaptor.notifyDataSetChanged();

                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

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

    @Override
    protected void onResume() {
        super.onResume();
        if(getIntent().getBooleanExtra("fromEdit",false) || getIntent().getBooleanExtra("fromDelete",false)){
            loadData();
        }
    }


}