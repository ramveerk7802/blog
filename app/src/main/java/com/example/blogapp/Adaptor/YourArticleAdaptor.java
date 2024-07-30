package com.example.blogapp.Adaptor;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.blogapp.Activity.AddBlogActivity;
import com.example.blogapp.Activity.ReadMoreActivity;
import com.example.blogapp.Model.BlogItemModel;
import com.example.blogapp.R;
import com.example.blogapp.databinding.BlogLayoutWithEditBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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
import com.squareup.picasso.Picasso;

import java.util.List;

public class YourArticleAdaptor extends RecyclerView.Adapter<YourArticleAdaptor.ViewHolder>{
    private Context context;
    private List<BlogItemModel> list;
    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private FirebaseStorage storage;
    private AlertDialog.Builder builder;
    public YourArticleAdaptor(Context context, List<BlogItemModel> list) {
        this.context=context;
        this.list=list;
        database= FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        storage= FirebaseStorage.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater= LayoutInflater.from(this.context);
        BlogLayoutWithEditBinding binding= BlogLayoutWithEditBinding.inflate(inflater,parent,false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BlogItemModel item = list.get(position);
        int deletePosition = position;

        String originalBloggerName= item.getBloggerName();
        String originalBlogTitle = item.getBlogTitle();
        String modifyBlogTitle = originalBlogTitle.substring(0,1).toUpperCase()+originalBlogTitle.substring(1).toLowerCase();
        String modifyBloggerName = originalBloggerName.substring(0,1).toUpperCase()+originalBloggerName.substring(1).toLowerCase();
        Picasso.get().load(item.getBlogPic()).into(holder.binding.bloggerImg);
        Picasso.get().load(item.getImageView()).into(holder.binding.bloggerImageView);
        holder.binding.bloggerName.setText(modifyBloggerName);
        holder.binding.bloggerPostText.setText(item.getPost());
        holder.binding.bloggingDate.setText(item.getDate());
        holder.binding.title.setText(modifyBlogTitle);

        // handle the edit button
        holder.binding.edit.setOnClickListener(v -> {
            Intent intent = new Intent(this.context,AddBlogActivity.class);
            intent.putExtra("postId",item.getPostId());
            intent.putExtra("fromEdit",true);
            this.context.startActivity(intent);
        });

        // handle the read more button
        holder.binding.readMore.setOnClickListener(v -> {
            Intent intent = new Intent(this.context, ReadMoreActivity.class);
            intent.putExtra("blogDesc",item.getPost());
            intent.putExtra("blogTitle",item.getBlogTitle());
            intent.putExtra("bloggerImage",item.getBlogPic());
            intent.putExtra("bloggingDate",item.getDate());
            intent.putExtra("bloggerName",item.getBloggerName());
            intent.putExtra("fromYourArticle",true);
            this.context.startActivity(intent);

        });

        // handle delete button
        holder.binding.delete.setOnClickListener(v -> {
            Context context1 = this.context;
            if(currentUser!=null && item!=null){
                builder=new AlertDialog.Builder(this.context);
                builder.setTitle("Delete")
                .setIcon(R.drawable.delete)
                    .setMessage(R.string.delete_msg).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteArticle(item.getPostId(), context1,deletePosition);
                        dialog.dismiss();

                        Toast.makeText(holder.binding.getRoot().getContext(),"Article removed successfully",Toast.LENGTH_SHORT).show();
                        
                    }
                })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setCancelable(false)
                                .create();
                    builder.show();

            }
        });




    }

    private void deleteArticle(String postId,Context context,int deletePosition) {
        Context context2 =context;
        if(currentUser==null){
            Toast.makeText(this.context,"No internet connection",Toast.LENGTH_SHORT).show();
            return;
        }
        else {
            DatabaseReference userReference = database.getReference().child("User").child(currentUser.getUid());
            DatabaseReference blogReference = database.getReference().child("blog");

            // check the saved article for delete
            userReference.child("saveArticle").child(postId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                userReference.child("saveArticle")
                                        .child(postId)
                                        .removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                removeData(postId,context2,userReference,blogReference);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(context2,"Failed to delete",Toast.LENGTH_SHORT).show();
                                            }
                                        });

                            }
                            else {
                                    removeData(postId,context2,userReference,blogReference);

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
    }

    private void removeData(String postId, Context context2,DatabaseReference userReference, DatabaseReference blogReference) {
        blogReference.child(postId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            BlogItemModel item = snapshot.getValue(BlogItemModel.class);
                            String imageUrl = "";
                            if(item!=null)
                                imageUrl=item.getImageView();
                            if(imageUrl!=null && !imageUrl.isEmpty()){
                                StorageReference imageReference = storage.getReferenceFromUrl(imageUrl);
                                imageReference.delete()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    blogReference.child(postId)
                                                            .removeValue()
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused) {
                                                                    userReference.child("MyBlog")
                                                                            .child(postId)
                                                                            .removeValue()
                                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void unused) {
                                                                                    list.remove(item);
                                                                                    Toast.makeText(context2,"Article removed succesfully",Toast.LENGTH_SHORT).show();
                                                                                }
                                                                            });
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Toast.makeText(context2,"Failed to delete",Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                }
                                                else
                                                    Toast.makeText(context2,"Image does not delete !!",Toast.LENGTH_LONG).show();
                                            }
                                        });
                            }
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
        BlogLayoutWithEditBinding binding;
        public ViewHolder(@NonNull BlogLayoutWithEditBinding binding) {
            super(binding.getRoot());
            this.binding=binding;


        }
    }

}
