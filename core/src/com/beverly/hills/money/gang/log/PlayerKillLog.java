package com.beverly.hills.money.gang.log;

import org.apache.commons.lang3.StringUtils;

public class PlayerKillLog {

    private static final int MAX_MSG_DURATION_MLS = 3_000;
    private final String playerName;
    private String killerMessage;
    private long expireTime;

    public PlayerKillLog(String playerName) {
        this.playerName = playerName;
    }

    // TODO refactor
    public void myPlayerKill(String victim) {
        killerMessage = StringUtils.upperCase(playerName + " KILLS " + victim);
        expireTime = System.currentTimeMillis() + MAX_MSG_DURATION_MLS;
    }

    public void otherPlayerKill(String otherPlayer, String victim) {
        killerMessage = StringUtils.upperCase(otherPlayer + " KILLS " + victim);
        expireTime = System.currentTimeMillis() + MAX_MSG_DURATION_MLS;
    }

    public boolean hasKillerMessage() {
        return System.currentTimeMillis() <= expireTime;
    }

    public String getKillerMessage() {
        return StringUtils.defaultIfBlank(killerMessage, "");
    }

}
