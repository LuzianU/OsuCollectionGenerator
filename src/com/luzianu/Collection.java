package com.luzianu;

import java.util.ArrayList;
import java.util.List;

public class Collection {
    private String name;
    private List<String> md5s;

    public Collection(String name) {
        this.name = name;
        this.md5s = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addBeatmap(String md5) {
        if (!md5s.contains(md5))
            md5s.add(md5);
    }

    public void removeBeatmap(String md5) {
        md5s.remove(md5);
    }

    public int getSize() {
        return md5s.size();
    }

    public List<String> getMd5s() {
        return md5s;
    }

}
