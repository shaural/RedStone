package com.cs407.team15.redstone.model;

public class Tag {
    private String name;

    public Tag() {
        name = "";
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public Tag(String name) {
        this.name=name;
    }
}
