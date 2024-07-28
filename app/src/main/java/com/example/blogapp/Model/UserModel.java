package com.example.blogapp.Model;

public class UserModel {
    private String displayName,email,password,profilePic,userUid;
    public UserModel(){}

    public UserModel(String displayName, String email, String password, String profilePic, String userUid) {
        this.displayName = displayName;
        this.email=email;
        this.password = password;
        this.profilePic = profilePic;
        this.userUid = userUid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
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
