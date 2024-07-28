package com.example.blogapp;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.blogapp.Activity.AddBlogActivity;
import com.example.blogapp.Activity.LoginActivity;
import com.example.blogapp.Activity.SavedArticleActivity;
import com.example.blogapp.Activity.YourArticleActivity;
import com.example.blogapp.Adaptor.FeedAdaptor;
import com.example.blogapp.Model.BlogItemModel;
import com.example.blogapp.Model.UserModel;
import com.example.blogapp.databinding.ActivityMainBinding;

import com.example.blogapp.databinding.HeaderLayoutBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.crypto.interfaces.PBEKey;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private HeaderLayoutBinding headerLayoutBinding;

    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private boolean isFabExtended=true;
    FeedAdaptor adaptor;
    private List<BlogItemModel> list;
    DrawerLayout drawerLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // database
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        currentUser = auth.getCurrentUser();

        // find the id of view

        NavigationView navigationView = findViewById(R.id.navigationView);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawerLayout);

        // toolbar setup
        setSupportActionBar(toolbar);
        if(getSupportActionBar()!=null){
            getSupportActionBar().setTitle("New Feed");
        }
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.OpenDrawer,R.string.CloseDrawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();


        //inflate and bind header layout of drawer (navigation)
        View view = binding.navigationView.getHeaderView(0);
        headerLayoutBinding = HeaderLayoutBinding.bind(view);

        // handle the navigation view
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id =item.getItemId();
                if(id==R.id.nav_home){
                }
                else if(id==R.id.nav_new_article) {
                    startActivity(new Intent(MainActivity.this, AddBlogActivity.class));
                }
                else if(id==R.id.nav_your_article){
                        startActivity(new Intent(MainActivity.this, YourArticleActivity.class));
                }
                else if(id==R.id.nav_bookmark) {
                    startActivity(new Intent(MainActivity.this, SavedArticleActivity.class));
                }
                else if(id==R.id.nav_privacy_and_policy){
                        Toast.makeText(MainActivity.this,"Clicked on privacy and policy",Toast.LENGTH_SHORT).show();
                }
                else if(id==R.id.nav_about) {
                    Toast.makeText(MainActivity.this, "Clicked on About", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(MainActivity.this,"Clicked on logout",Toast.LENGTH_SHORT).show();
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return false;
            }
        });

        //handle the back button if drawer open
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if(drawerLayout.isDrawerOpen(GravityCompat.START))
                    drawerLayout.closeDrawer(GravityCompat.START);
                else {
                    setEnabled(false);
                    MainActivity.super.finish();
                }
            }
        });



        if(currentUser==null){
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        binding.appbarMainLayout.mainContentLayout.recycleViewBlog.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(dy>0 && isFabExtended){
                    binding.appbarMainLayout.mainContentLayout.floatingBtn.shrink();
                    isFabExtended =false;
                }

                else if(dy<0 && !isFabExtended){
                    binding.appbarMainLayout.mainContentLayout.floatingBtn.extend();
                    isFabExtended=true;
                }
            }
        });

        // fetch the user data
        database.getReference().child("User").child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserModel model = snapshot.getValue(UserModel.class);
                if(model.getProfilePic()!=null) {
                    Picasso.get().load(model.getProfilePic()).into(binding.appbarMainLayout.profileImg);
                    Picasso.get().load(model.getProfilePic()).into(headerLayoutBinding.headerProfileImg);
                }
                headerLayoutBinding.headerUserName.setText(model.getDisplayName());
                headerLayoutBinding.headerEmail.setText(model.getEmail());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.appbarMainLayout.mainContentLayout.floatingBtn.setOnClickListener( v -> {
            startActivity(new Intent(this, AddBlogActivity.class));
        });

        // set the data in recycle view
        list = new LinkedList<>();
        adaptor = new FeedAdaptor(MainActivity.this,list);
        binding.appbarMainLayout.mainContentLayout.recycleViewBlog.setAdapter(adaptor);
        binding.appbarMainLayout.mainContentLayout.recycleViewBlog.setLayoutManager(new LinearLayoutManager(MainActivity.this));


        // load the data in recycle view
        loadData();


    }
    private void loadData(){
        database.getReference().child("blog").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    BlogItemModel obj = dataSnapshot.getValue(BlogItemModel.class);
                    if(obj!=null) {
                        list.add(obj);
                    }
                }
                // shuffle the list
                Collections.reverse(list);
                adaptor.notifyDataSetChanged();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("MainActivity recycle",error.getMessage());
                Toast.makeText(MainActivity.this,"recycler view data does not set",Toast.LENGTH_SHORT).show();

            }
        });

    }





}