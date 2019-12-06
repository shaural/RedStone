package com.cs407.team15.redstone.model;

import java.util.List;

public class Scavanger {
    private String name;
    private String uid;
    private String type;
    private Boolean hammer;
    private List<String> locations;
    private List<String> hints;
    private List<String> tags;
    private List<Integer> leaderboardTime;
    private List<String> leaderboardUsername;
    private Integer votes;
    public Scavanger(String name, String uid, String type, Boolean hammer, List<String> locations, List<String> hints, List<String> tags, List<Integer> leaderboardTime, List<String> leaderboardUsername, Integer votes){
        this.name=name;
        this.uid=uid;
        this.type=type;
        this.hammer=hammer;
        this.locations=locations;
        this.hints=hints;
        this.tags=tags;
        this.leaderboardTime=leaderboardTime;
        this.leaderboardUsername=leaderboardUsername;
        this.votes=votes;
    }

    public String getName() {
        return name;
    }

    public String getUid() {
        return uid;
    }

    public String getType() {
        return type;
    }

    public Boolean getHammer() {
        return hammer;
    }

    public List<String> getLocations() {
        return locations;
    }

    public List<String> getHints() {
        return hints;
    }

    public void setHints(List<String> hints) {
        this.hints = hints;
    }

    public List<String> getTags() {
        return tags;
    }

    public Integer getVotes() {
        return votes;
    }

    public List<Integer> getLeaderboardTime() {
        return leaderboardTime;
    }

    public List<String> getLeaderboardUsername() {
        return leaderboardUsername;
    }
}
