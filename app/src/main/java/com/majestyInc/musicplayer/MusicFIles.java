package com.majestyInc.musicplayer;

public class MusicFIles {
    private String Path;
    private String Title;
    private String Artist;
    private String Album;
    private String Duration;
    private String Id;


    public MusicFIles(String path, String title, String artist, String album, String duration, String id) {
        Path = path;
        Title = title;
        Artist = artist;
        Album = album;
        Duration = duration;
        Id = id;
    }

    public String getPath() {
        return Path;
    }

    public void setPath(String path) {
        Path = path;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getArtist() {
        return Artist;
    }

    public void setArtist(String artist) {
        Artist = artist;
    }

    public String getAlbum() {
        return Album;
    }

    public void setAlbum(String album) {
        Album = album;
    }

    public String getDuration() {
        return Duration;
    }

    public void setDuration(String duration) {
        Duration = duration;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }
}
