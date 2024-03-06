package com.example.blogapp.Adaptor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.blogapp.Model.BlogItemModel;
import com.example.blogapp.R;
import com.example.blogapp.databinding.BlogLayoutBinding;
import com.squareup.picasso.Picasso;

import java.util.List;

public class FeedAdaptor extends RecyclerView.Adapter<FeedAdaptor.ViewHolder> {
    private BlogLayoutBinding binding;
    private List<BlogItemModel> list;
    private Context context;
    public FeedAdaptor(Context context,List<BlogItemModel> list){
        this.list=list;
        this.context=context;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        binding = BlogLayoutBinding.inflate(inflater);
        return new ViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BlogItemModel model = list.get(position);

        binding.title.setText(model.getBlogTitle());
        binding.bloggerName.setText(model.getBloggerName());
        binding.bloggerPost.setText(model.getPost());
        binding.likeCountText.setText(Integer.toString(model.getLikeCount()));
        Picasso.get().load(model.getBlogPic()).placeholder(R.drawable.profile_avtar).into(binding.bloggerImg);

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
