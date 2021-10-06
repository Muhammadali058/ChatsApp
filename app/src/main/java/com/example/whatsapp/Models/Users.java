package com.example.whatsapp.Models;

import androidx.annotation.Nullable;

import java.io.Serializable;

public class Users implements Serializable {
    String uId;
    String name;
    String phoneNumber;
    String imageUrl;
    String status;
    String token;

    public Users() {
    }

    public Users(String uId, String name, String phoneNumber, String imageUrl) {
        this.uId = uId;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.imageUrl = imageUrl;
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(this == obj)
            return true;
        else {
            Users user = (Users) obj;
            if(this.uId == user.getuId())
                return true;
            else
                return  false;
        }
    }
}
