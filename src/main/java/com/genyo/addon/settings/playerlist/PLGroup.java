package com.genyo.addon.settings.playerlist;

import java.util.List;

public class PLGroup {

    private volatile String message;
    private volatile List<ListPlayer> players;
    private volatile String name;
    private volatile boolean enabled;

    public PLGroup(String name, String message, List<ListPlayer> players) {
        this.name = name;
        this.message = message;
        this.players = players;
        this.enabled = true;
    }

    public boolean containsPlayer(ListPlayer player) {
        if (containsPlayer(player.getName())) return true;
        return false;
    }

    public boolean containsPlayer(String name) {
        for (ListPlayer listPlayer : players) {
            if (listPlayer.getName().equals(name)) {
                return true;
            }
        }

        return false;
    }

    public String getGroupName() {
        return name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ListPlayer> getPlayers() {
        return players;
    }

    public void setGroupName(String newName) {
        this.name = newName;
    }

    public void setPlayers(List<ListPlayer> players) {
        this.players = players;
    }

    public void addPlayer(ListPlayer player) {
        players.add(player);
    }

    public void removePlayer(ListPlayer player) {
        players.remove(player);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void toggle() {
        enabled = !enabled;
    }

}
