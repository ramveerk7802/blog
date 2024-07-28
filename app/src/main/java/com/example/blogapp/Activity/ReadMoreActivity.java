package com.example.blogapp.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.example.blogapp.R;
import com.example.blogapp.databinding.ActivityReadMoreBinding;
import com.squareup.picasso.Picasso;

public class ReadMoreActivity extends AppCompatActivity {
    ActivityReadMoreBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReadMoreBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        //handle toolbar
        setSupportActionBar(binding.readMoreToolbarLayout.generalToolbar);
        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
        // handle intent pass from your article adaptor
        if(getIntent().getBooleanExtra("from Your Article",false)){
            binding.likeBtn.setVisibility(View.GONE);
            binding.saveBtn.setVisibility(View.GONE);

        }
        String originalBloggerName = getIntent().getStringExtra("bloggerName");
        String title = getIntent().getStringExtra("blogTitle");
        String modifyName = originalBloggerName.substring(0,1).toUpperCase()+originalBloggerName.substring(1).toLowerCase();
        String modifyTitle = title.substring(0,1).toUpperCase()+title.substring(1).toLowerCase();
        binding.bloggerName.setText(modifyName);
        binding.blogTitleText.setText(modifyTitle);
        binding.dateText.setText(getIntent().getStringExtra("bloggingDate"));
        Picasso.get().load(getIntent().getStringExtra("bloggerImage")).into(binding.bloggerImage);
        binding.blogDesc.setText(getIntent().getStringExtra("blogDesc"));
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