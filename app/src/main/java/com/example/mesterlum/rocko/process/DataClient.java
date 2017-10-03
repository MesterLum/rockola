package com.example.mesterlum.rocko.process;

import java.io.Serializable;

/**
 * Created by mesterlum on 02/10/17.
 */

public class DataClient implements Serializable {

    private String user;
    private String music;
    private String musicId;

    public String getMusicId() {
        return musicId;
    }

    public void setMusicId(String musicId) {
        this.musicId = musicId;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getMusic() {
        return music;
    }

    public void setMusic(String music) {
        this.music = music;
    }
}
