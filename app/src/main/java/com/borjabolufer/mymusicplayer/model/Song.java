package com.borjabolufer.mymusicplayer.model;

import android.media.MediaMetadataRetriever;

public class Song {
    private String title;
    private String artist;
    private String album;
    private String year;
    private int resourceId;


    public Song(String title, String artist, String album, String year, int resourceId) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.year = year;
        this.resourceId = resourceId;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public String getYear() {
        return year;
    }

    public int getResourceId() {
        return resourceId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }

    @Override
    public String toString() {
        return "Song{" +
                "title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", album='" + album + '\'' +
                ", year='" + year + '\'' +
                '}';
    }
}
