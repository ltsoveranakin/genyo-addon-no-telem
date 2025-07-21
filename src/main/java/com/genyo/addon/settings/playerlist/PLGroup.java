package com.genyo.addon.settings.playerlist;

import com.genyo.addon.modules.GenyoWelcome;

import java.util.List;

public class PLGroup {

    private volatile String message;
    private volatile List<GenyoWelcome.ListPlayer> players;
    private volatile String name;

    public PLGroup(String name, String message, List<GenyoWelcome.ListPlayer> players) {
        this.name = name;
        this.message = message;
        this.players = players;
    }

    public boolean containsPlayer(GenyoWelcome.ListPlayer player) {
        for (GenyoWelcome.ListPlayer listPlayer : players) {
            if (listPlayer.getName().equals(player.getName())) {
                return true;
            }
        }

        return false;
    }

    public boolean containsPlayer(String name) {
        for (GenyoWelcome.ListPlayer listPlayer : players) {
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

    public List<GenyoWelcome.ListPlayer> getPlayers() {
        return players;
    }

    public void setGroupName(String newName) {
        this.name = newName;
    }

    public void setPlayers(List<GenyoWelcome.ListPlayer> players) {
        this.players = players;
    }

    public void addPlayer(GenyoWelcome.ListPlayer player) {
        players.add(player);
    }

    public void removePlayer(GenyoWelcome.ListPlayer player) {
        players.remove(player);
    }

}
