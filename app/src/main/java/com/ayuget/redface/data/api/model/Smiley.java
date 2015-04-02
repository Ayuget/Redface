package com.ayuget.redface.data.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class Smiley {
    private int id;

    private String code;

    @SerializedName("image_url")
    private String imageUrl;

    private String image;

    private Date sentAt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Date getSentAt() {
        return sentAt;
    }

    public void setSentAt(Date sentAt) {
        this.sentAt = sentAt;
    }

    public static Smiley make(String code, String imageUrl) {
        Smiley s = new Smiley();
        s.setCode(code);
        s.setImageUrl(imageUrl);
        return s;
    }
}
