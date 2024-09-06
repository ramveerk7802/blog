package com.example.blogapp;



import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
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

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


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

        if(currentUser==null){
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

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

                 if(id==R.id.nav_new_article) {
                    startActivity(new Intent(MainActivity.this, AddBlogActivity.class));
                }
                else if(id==R.id.nav_your_article){
                        startActivity(new Intent(MainActivity.this, YourArticleActivity.class));
                }
                else if(id==R.id.nav_bookmark) {
                    startActivity(new Intent(MainActivity.this, SavedArticleActivity.class));
                }
                else if(id==R.id.nav_about) {
                    Toast.makeText(MainActivity.this, "Clicked on About", Toast.LENGTH_SHORT).show();
                }
                else {
                    auth.signOut();
                    startActivity(new Intent(MainActivity.this,LoginActivity.class));
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
                assert model != null;
                if(model.getProfilePic()!=null) {
//                    Picasso.get().load(model.getProfilePic()).into(binding.appbarMainLayout.profileImg);
                    Picasso.get().load(model.getProfilePic()).into(headerLayoutBinding.headerProfileImg);
                }
                String originalName = model.getDisplayName();
                String modifyName = originalName.substring(0,1).toUpperCase()+originalName.substring(1).toLowerCase();
                headerLayoutBinding.headerUserName.setText(modifyName);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);
        MenuItem searchItem = menu.findItem(R.id.main_menu_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        assert searchView != null;
        searchView.setQueryHint("Search here...");

        // change the edit text color

        EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(Color.WHITE);
        searchEditText.setHintTextColor(Color.WHITE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return true;
            }
        });


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id =item.getItemId();
        if(id==R.id.main_menu_logout){
            auth.signOut();
            startActivity(new Intent(MainActivity.this,LoginActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void filterList(String newText) {
        List<BlogItemModel> filteredList= new LinkedList<>();
        for(BlogItemModel item : list){
            if(item.getBlogTitle().toLowerCase().contains(newText))
                filteredList.add(item);
        }
        adaptor.setFilteredList(filteredList);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(currentUser==null){
            startActivity(new Intent(MainActivity.this,LoginActivity.class));
            finish();
        }

    }
}