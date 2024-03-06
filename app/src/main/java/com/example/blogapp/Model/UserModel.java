package com.example.blogapp.Model;

public class UserModel {
    private String name,email,password,profilePic,userUid;
    public UserModel(){}

    public UserModel(String name, String email, String password, String profilePic, String userUid) {
        this.name = name;
        this.email=email;
        this.password = password;
        this.profilePic = profilePic;
        this.userUid = userUid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getUserUid() {
        return userUid;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }
}
