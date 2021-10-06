package com.example.whatsapp.Models;

import java.io.Serializable;

public class Countries implements Serializable {
    int image;
    String name;

    String code;

    public Countries(int image, String name, String code) {
        this.image = image;
        this.name = name;
        this.code = code;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

}
