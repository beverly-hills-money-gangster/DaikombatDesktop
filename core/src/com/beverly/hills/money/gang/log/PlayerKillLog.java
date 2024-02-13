package com.beverly.hills.money.gang.log;

import org.apache.commons.lang3.StringUtils;

public class PlayerKillLog {

    private static final int MAX_MSG_DURATION_MLS = 3_000;
    private int kills;
    private final String playerName;
    private String killerMessage;
    private long expireTime;

    public PlayerKillLog(String playerName) {
        this.playerName = playerName;
    }

    public void myPlayerKill(String victim) {
        kills++;
        killerMessage = StringUtils.upperCase(playerName + " KILLS " + victim);
        expireTime = System.currentTimeMillis() + MAX_MSG_DURATION_MLS;
    }

    public void otherPlayerKill(String otherPlayer, String victim) {
        killerMessage = StringUtils.upperCase(otherPlayer + " KILLS " + victim);
        expireTime = System.currentTimeMillis() + MAX_MSG_DURATION_MLS;
    }

    public String getKillCount() {
        if (kills == 0) {
            return "";
        }
        if (kills == 1) {
            return kills + " KILL";
        }
        return kills + " KILLS";
    }

    public boolean hasKillerMessage() {
        return System.currentTimeMillis() <= expireTime;
    }

    public String getKillerMessage() {
        return StringUtils.defaultIfBlank(killerMessage, "");
    }

    public int getKills() {
        return kills;
    }
}
