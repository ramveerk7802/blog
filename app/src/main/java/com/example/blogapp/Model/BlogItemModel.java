package com.example.blogapp.Model;

import java.util.LinkedList;
import java.util.List;

public class BlogItemModel {
    private String blogTitle,blogPic,bloggerName,post,postId;
    private String date;
    private int likeCount;
    private String imageView;
    private Boolean saved,liked;



    public BlogItemModel(){
        this.postId =null;
    }
//    public BlogItemModel(String blogTitle,String blogPic,String bloggerName,String date,String post,int likeCount,String imageView){
//        this.blogTitle=blogTitle;
//        this.blogPic=blogPic;
//        this.bloggerName=bloggerName;
//        this.date=date;
//        this.post =post;
//        this.likeCount=likeCount;
//        this.imageView=imageView;
//        this.postId= postId;
//    }
    public BlogItemModel(String blogTitle,String blogPic,String bloggerName,String date,String post,int likeCount,String imageView,boolean saved,boolean liked){
        this.blogTitle=blogTitle;
        this.blogPic=blogPic;
        this.bloggerName=bloggerName;
        this.date=date;
        this.post =post;
        this.likeCount=likeCount;
        this.imageView=imageView;
        this.postId=null;
        this.saved=saved;
        this.liked=liked;

    }



    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getImageView() {
        return imageView;
    }

    public void setImageView(String imageView) {
        this.imageView = imageView;
    }

    public String getBlogTitle() {
        return blogTitle;
    }

    public void setBlogTitle(String blogTitle) {
        this.blogTitle = blogTitle;
    }

    public String getBlogPic() {
        return blogPic;
    }

    public void setBlogPic(String blogPic) {
        this.blogPic = blogPic;
    }

    public String getBloggerName() {
        return bloggerName;
    }

    public void setBloggerName(String bloggerName) {
        this.bloggerName = bloggerName;
    }

    public String getPost() {
        return post;
    }

    public void setPost(String post) {
        this.post = post;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public Boolean getSaved() {
        return saved;
    }

    public void setSaved(Boolean saved) {
        this.saved = saved;
    }

    public Boolean getLiked() {
        return liked;
    }

    public void setLiked(Boolean liked) {
        this.liked = liked;
    }
}
