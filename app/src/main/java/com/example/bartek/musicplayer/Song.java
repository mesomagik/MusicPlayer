package com.example.bartek.musicplayer;

import android.content.SharedPreferences;

import java.io.Serializable;

/**
 * Created by Bartek on 2016-12-23.
 */

public class Song implements Serializable{

    public static final String SHARED_FAVOURITES = "MyFavourites1";
    private long id;
    private String title;
    private String artist;

    public Song(long id, String title, String artist){
        this.id = id;
        this.artist = artist;
        this.title = title;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public Song getObj(){
        return this;
    }


}
