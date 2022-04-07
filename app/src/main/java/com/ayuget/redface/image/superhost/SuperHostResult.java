package com.ayuget.redface.image.superhost;

import com.google.gson.annotations.SerializedName;

public class SuperHostResult {

    int statusCode;
    String statusTxt;
    SuperHostImage image;

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusTxt() {
        return statusTxt;
    }

    public void setStatusTxt(String statusTxt) {
        this.statusTxt = statusTxt;
    }

    public SuperHostImage getImage() {
        return image;
    }

    public void setImage(SuperHostImage image) {
        this.image = image;
    }

    class SuperHostImage {
        String url;
        SuperHostUrl medium;
        SuperHostUrl thumb;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public SuperHostUrl getMedium() {
            return medium;
        }

        public void setMedium(SuperHostUrl medium) {
            this.medium = medium;
        }

        public SuperHostUrl getThumb() {
            return thumb;
        }

        public void setThumb(SuperHostUrl thumb) {
            this.thumb = thumb;
        }
    }

    class SuperHostUrl {
        String url;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

}
