package com.example.blogapp.Adaptor;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.blogapp.Activity.ReadMoreActivity;
import com.example.blogapp.Activity.SavedArticleActivity;
import com.example.blogapp.Model.BlogItemModel;
import com.example.blogapp.R;
import com.example.blogapp.databinding.BlogLayoutBinding;
import com.squareup.picasso.Picasso;

import java.util.List;

public class SavedArticleAdaptor extends RecyclerView.Adapter<SavedArticleAdaptor.ViewHolder> {
    private Context context;
    private List<BlogItemModel> list;

    public SavedArticleAdaptor(Context context,List<BlogItemModel> list){
        this.context=context;
        this.list=list;

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
            Picasso.get().load(model.getBlogPic()).into(holder.binding.bloggerImg);
            Picasso.get().load(model.getImageView()).into(holder.binding.bloggerImageView);
            if (model.getSaved())
                holder.binding.saveBlogBtn.setImageResource(R.drawable.save_fill);
            else
                holder.binding.saveBlogBtn.setImageResource(R.drawable.save);

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
