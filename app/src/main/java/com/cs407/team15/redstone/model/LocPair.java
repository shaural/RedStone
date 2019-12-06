package com.cs407.team15.redstone.model;

public class LocPair {
    private String locId;
    private long total;

    public LocPair(String locId, long total) {
        this.locId=locId;
        this.total=total;
    }

    public String getLocId() {
        return locId;
    }

    public void setLocId(String locId) {
        this.locId = locId;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
