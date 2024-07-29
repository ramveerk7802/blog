package com.example.blogapp.Adaptor;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.blogapp.Activity.ReadMoreActivity;
import com.example.blogapp.Activity.SavedArticleActivity;
import com.example.blogapp.Model.BlogItemModel;
import com.example.blogapp.R;
import com.example.blogapp.databinding.BlogLayoutBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class SavedArticleAdaptor extends RecyclerView.Adapter<SavedArticleAdaptor.ViewHolder> {
    private Context context;
    private List<BlogItemModel> list;
    private FirebaseDatabase database;
    private FirebaseUser currentUser;

    public SavedArticleAdaptor(Context context,List<BlogItemModel> list){
        this.context=context;
        this.list=list;
        database = FirebaseDatabase.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(this.context);
        BlogLayoutBinding binding = BlogLayoutBinding.inflate(inflater,parent,false);
        return new ViewHolder(binding);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BlogItemModel model = this.list.get(position);
        if(model!=null) {
            String originalBloggerName= model.getBloggerName();
            String originalBlogTitle = model.getBlogTitle();
            String modifyBlogTitle = originalBlogTitle.substring(0,1).toUpperCase()+originalBlogTitle.substring(1).toLowerCase();
            String modifyBloggerName = originalBloggerName.substring(0,1).toUpperCase()+originalBloggerName.substring(1).toLowerCase();
            holder.binding.title.setText(modifyBlogTitle);
            holder.binding.bloggerName.setText(modifyBloggerName);
            holder.binding.bloggerPost.setText(model.getPost());
            holder.binding.likeCountText.setText(Integer.toString(model.getLikeCount()));
            holder.binding.bloggingDate.setText(model.getDate());
            Picasso.get().load(model.getBlogPic()).into(holder.binding.bloggerImg);
            Picasso.get().load(model.getImageView()).into(holder.binding.bloggerImageView);
            holder.binding.saveBlogBtn.setImageResource(R.drawable.save_fill);


            // handle read more button

            holder.binding.readMoreBtn.setOnClickListener(v -> {

                Intent intent = new Intent(this.context, ReadMoreActivity.class);
                intent.putExtra("blogDesc",model.getPost());
                intent.putExtra("blogTitle",model.getBlogTitle());
                intent.putExtra("bloggerImage",model.getBlogPic());
                intent.putExtra("bloggingDate",model.getDate());
                intent.putExtra("bloggerName",model.getBloggerName());
                context.startActivity(intent);
            });

            // handle save button

            holder.binding.saveBlogBtn.setOnClickListener(v -> {
                if(currentUser!=null){
                    handleSaveButton(holder.binding,model);
                    list.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position,list.size());
                }
            });
        }


    }

    private void handleSaveButton(BlogLayoutBinding binding, BlogItemModel model) {
        if(currentUser==null){
            Toast.makeText(this.context,"No internet connection !!",Toast.LENGTH_SHORT).show();
            return;
        }else {
            DatabaseReference saveReference = database.getReference().child("User").child(currentUser.getUid()).child("saveArticle");
            DatabaseReference blogReference = database.getReference().child("blog");
            saveReference.child(model.getPostId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    saveReference.child(model.getPostId()).removeValue()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    blogReference.child(model.getPostId())
                                            .child("saveArticle")
                                            .child(currentUser.getUid())
                                            .removeValue()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                }
                                            });
                                }
                            });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        BlogLayoutBinding binding;

        public ViewHolder(@NonNull BlogLayoutBinding binding) {
            super(binding.getRoot());
            this.binding=binding;
        }
    }
}
