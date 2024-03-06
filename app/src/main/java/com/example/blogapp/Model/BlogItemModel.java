package com.example.blogapp.Model;

public class BlogItemModel {
    private String blogTitle,blogPic,bloggerName,post;
    private long timeStamp;
    private int likeCount;
    public BlogItemModel(){}
    public BlogItemModel(String blogTitle,String blogPic,String bloggerName,long timeStamp,String post,int likeCount){
        this.blogTitle=blogTitle;
        this.blogPic=blogPic;
        this.bloggerName=bloggerName;
        this.timeStamp=timeStamp;
        this.post =post;
        this.likeCount=likeCount;
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

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }
}
