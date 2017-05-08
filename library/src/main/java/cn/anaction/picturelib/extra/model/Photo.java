/*
 * Copyright (c) 2017. danlu.com Co.Ltd. All rights reserved.
 */

package cn.anaction.picturelib.extra.model;

/**
 * author: wuhaiyang(<a href="mailto:wuhaiyang@danlu.com">wuhaiyang@danlu.com</a>)<br/>
 * version: 1.0.0<br/>
 * since: 2017-05-07 下午9:51<br/>
 */
public class Photo {
    private int id;
    private String path;

    public Photo(int id, String path) {
        this.id = id;
        this.path = path;
    }

    public Photo() {
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Photo)) return false;

        Photo photo = (Photo) o;

        return id == photo.id;
    }

    @Override public int hashCode() {
        return id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
