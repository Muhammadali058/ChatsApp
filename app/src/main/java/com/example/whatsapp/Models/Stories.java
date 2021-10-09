package com.example.whatsapp.Models;

import java.util.List;

public class Stories {
    String uId, name, profileImage;
    List<Status> statuses;

    public Stories() {
    }

    public Stories(String uId, String name, String profileImage, List<Status> statuses) {
        this.uId = uId;
        this.name = name;
        this.profileImage = profileImage;
        this.statuses = statuses;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public List<Status> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<Status> statuses) {
        this.statuses = statuses;
    }
}
