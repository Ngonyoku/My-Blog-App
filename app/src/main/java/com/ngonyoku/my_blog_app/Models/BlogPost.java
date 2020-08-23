package com.ngonyoku.my_blog_app.Models;

import java.sql.Timestamp;

public class BlogPost {
    private String description;
    private String image_url;
    private String thumbnail_url;
    private Timestamp timeStamp;
    private String user_id;

    public BlogPost() {
    }

    public BlogPost(String description, String image_url, String thumbnail_url, Timestamp timeStamp, String user_id) {
        this.description = description;
        this.image_url = image_url;
        this.thumbnail_url = thumbnail_url;
        this.timeStamp = timeStamp;
        this.user_id = user_id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getThumbnail_url() {
        return thumbnail_url;
    }

    public void setThumbnail_url(String thumbnail_url) {
        this.thumbnail_url = thumbnail_url;
    }

    public Timestamp getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Timestamp timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
}
