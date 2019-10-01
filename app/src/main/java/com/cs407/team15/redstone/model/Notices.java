package com.cs407.team15.redstone.model;

import com.cs407.team15.redstone.model.Notice;

public class Notices {
    private String description;
    private int n_id;
    private int time_created;

    public Notices() {

    }

    public Notices(String description, int n_id, int time_created) {
        this.description = description;
        this.n_id = n_id;
        this.time_created = time_created;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getN_id() {
        return n_id;
    }

    public void setN_id(int n_id) {
        this.n_id = n_id;
    }

    public int getTime_created() {
        return time_created;
    }

    public void setTime_created(int time_created) {
        this.time_created = time_created;
    }





}
