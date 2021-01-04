package com.ayuget.redface.image.superhost;

import com.google.gson.annotations.SerializedName;

public class SuperHostResult {

    int statusCode;
    String statusText;
    SuperHostImage image;

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
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
