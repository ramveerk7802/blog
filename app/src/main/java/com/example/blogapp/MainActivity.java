package com.example.blogapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.blogapp.Activity.LoginActivity;
import com.example.blogapp.Adaptor.FeedAdaptor;
import com.example.blogapp.Model.BlogItemModel;
import com.example.blogapp.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;

import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        auth = FirebaseAuth.getInstance();
        if(auth.getCurrentUser()==null){
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
        else{
            Toast.makeText(MainActivity.this,"Null",Toast.LENGTH_SHORT).show();
        }
        List<BlogItemModel> list = new LinkedList<>();
        String post = "Text messaging, or texting, is the act of ";
        list.add(new BlogItemModel("My new Blog","R.drawable.profile_avtar","Ramveer",4525556L,post,10));
        list.add(new BlogItemModel("My new Blog 1","R.drawable.profile_avtar","Ramveer kaithwar",4525556L,post,220));
        list.add(new BlogItemModel("My new Blog 2","R.drawable.profile_avtar","Amit kumar",4525556L,post,10));
        list.add(new BlogItemModel("My new Blog 3","R.drawable.profile_avtar","Vishal rao",4525556L,post,8));
        list.add(new BlogItemModel("My new Blog 5","R.drawable.profile_avtar","Ramveer verma",4525556L,post,20));
        final FeedAdaptor adaptor = new FeedAdaptor(MainActivity.this,list);
        binding.recycleViewBlog.setAdapter(adaptor);
        binding.recycleViewBlog.setLayoutManager(new LinearLayoutManager(MainActivity.this));
    }

}