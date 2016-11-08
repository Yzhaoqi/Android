package com.yzq.android.musicplayer;

import java.io.Serializable;

/**
 * Created by YZQ on 2016/11/8.
 */

public class MusicItem implements Serializable {
    private String name;
    private String path;

    public MusicItem(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }
}
