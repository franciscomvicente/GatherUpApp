package com.example.gatherup.Utils;

public class FindFriendsModel {
    private String Username, userID;

    private FindFriendsModel(){}

    private FindFriendsModel(String Username, String userID) {
        this.Username = Username;
        this.userID = userID;
    }

    public String getUsername(){
        return Username;
    }

    public void setUsername(String username) {
        this.Username = username;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
