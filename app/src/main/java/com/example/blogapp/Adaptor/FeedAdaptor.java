package com.example.blogapp.Adaptor;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.blogapp.Activity.ReadMoreActivity;
import com.example.blogapp.MainActivity;
import com.example.blogapp.Model.BlogItemModel;
import com.example.blogapp.R;
import com.example.blogapp.databinding.BlogLayoutBinding;
import com.google.android.gms.tasks.OnFailureListener;
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


public class FeedAdaptor extends RecyclerView.Adapter<FeedAdaptor.ViewHolder> {
    private  List<BlogItemModel> list;
    private final Context context;
    private final FirebaseDatabase database;
    private final FirebaseUser currentUser;

    public List<BlogItemModel> getList() {
        return list;
    }

    public FeedAdaptor(Context context, List<BlogItemModel> list){
        this.list=list;
        this.context=context;
        this.database= FirebaseDatabase.getInstance();
        this.currentUser= FirebaseAuth.getInstance().getCurrentUser();
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
        BlogItemModel model = list.get(position);
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

        // handle the blogger image view

        holder.binding.bloggerImageView.setOnClickListener( v->{

        });

        // handle the saveBlog Button

        holder.binding.saveBlogBtn.setOnClickListener(v -> {
            if(currentUser!=null) {
               handleSaveButtonClick(holder.binding, model, position);
            }
            else
                Toast.makeText(this.context,"No internet connection !!",Toast.LENGTH_SHORT).show();
        });

        // handle the like blog button

        holder.binding.likeBlogBtn.setOnClickListener( v -> {
            if(currentUser!=null)
                handleLikeButtonClick(holder.binding,model);
            else
                Toast.makeText(this.context,"No internet connection !!",Toast.LENGTH_SHORT).show();
        });
        


        // handle the read more Button

        holder.binding.readMoreBtn.setOnClickListener(v -> {
            Intent intent = new Intent(context, ReadMoreActivity.class);
            intent.putExtra("blogDesc",model.getPost());
            intent.putExtra("blogTitle",model.getBlogTitle());
            intent.putExtra("bloggerImage",model.getBlogPic());
            intent.putExtra("bloggingDate",model.getDate());
            intent.putExtra("bloggerName",model.getBloggerName());
            context.startActivity(intent);
        });

        // update the like button
        updateLikeButton(holder.binding,model);
        // update the save button
        updateSaveButton(holder.binding,model);
        

    }

    private void updateSaveButton(BlogLayoutBinding binding, BlogItemModel model) {
        DatabaseReference saveArticleReference = database.getReference().child("blog").child(model.getPostId());
        saveArticleReference.child("saveArticle")
                .child(currentUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists())
                            binding.saveBlogBtn.setImageResource(R.drawable.save_fill);
                        else
                            binding.saveBlogBtn.setImageResource(R.drawable.save);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void handleSaveButtonClick(BlogLayoutBinding binding, BlogItemModel model,int position) {
       DatabaseReference userReference = database.getReference().child("User").child(currentUser.getUid());
       DatabaseReference saveBlogReference = database.getReference().child("blog").child(model.getPostId()).child("saveArticle");
       saveBlogReference.child(currentUser.getUid())
               .addListenerForSingleValueEvent(new ValueEventListener() {
                   @Override
                   public void onDataChange(@NonNull DataSnapshot snapshot) {
                       if(snapshot.exists()){
                           saveBlogReference.child(currentUser.getUid()).removeValue()
                                   .addOnSuccessListener(new OnSuccessListener<Void>() {
                                       @Override
                                       public void onSuccess(Void unused) {
                                           userReference.child("saveArticle")
                                                   .child(model.getPostId()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                       @Override
                                                       public void onSuccess(Void unused) {
                                                           binding.saveBlogBtn.setImageResource(R.drawable.save);
                                                           notifyDataSetChanged();
                                                       }
                                                   });
                                       }
                                   });
                       }
                       else {
                           saveBlogReference.child(currentUser.getUid()).setValue(true)
                                   .addOnSuccessListener(new OnSuccessListener<Void>() {
                                       @Override
                                       public void onSuccess(Void unused) {
                                           userReference.child("saveArticle")
                                                   .child(model.getPostId()).setValue(true)
                                                   .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                       @Override
                                                       public void onSuccess(Void unused) {
                                                           binding.saveBlogBtn.setImageResource(R.drawable.save_fill);
                                                           notifyDataSetChanged();
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


    private void updateLikeButton(BlogLayoutBinding binding, BlogItemModel model) {
        DatabaseReference likeReference = database.getReference().child("blog").child(model.getPostId()).child("likes");
       likeReference.child(currentUser.getUid())
               .addListenerForSingleValueEvent(new ValueEventListener() {
                   @Override
                   public void onDataChange(@NonNull DataSnapshot snapshot) {
                       if(snapshot.exists())
                           binding.likeBlogBtn.setImageResource(R.drawable.like_fill);
                       else
                           binding.likeBlogBtn.setImageResource(R.drawable.like);
                   }

                   @Override
                   public void onCancelled(@NonNull DatabaseError error) {

                   }
               });

    }

    private void handleLikeButtonClick(BlogLayoutBinding binding, BlogItemModel model) {

        DatabaseReference userReference = database.getReference().child("User").child(currentUser.getUid());
        DatabaseReference likeReference = database.getReference().child("blog").child(model.getPostId());
        likeReference.child("likes")
                .child(currentUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            likeReference.child("likes")
                                    .child(currentUser.getUid())
                                    .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            likeReference.child("likeCount").setValue(model.getLikeCount()-1);
                                            userReference.child("likes")
                                                    .child(currentUser.getUid())
                                                    .removeValue()
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            binding.likeBlogBtn.setImageResource(R.drawable.like);
//                                                            notifyItemChanged(0);
                                                        }
                                                    });
                                        }
                                    });


                        }
                        else{
                            likeReference.child("likes")
                                    .child(currentUser.getUid())
                                    .setValue(true)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            likeReference.child("likeCount").setValue(model.getLikeCount()+1);
                                            userReference.child("likes")
                                                    .child(model.getPostId())
                                                    .setValue(true)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            binding.likeBlogBtn.setImageResource(R.drawable.like_fill);

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
