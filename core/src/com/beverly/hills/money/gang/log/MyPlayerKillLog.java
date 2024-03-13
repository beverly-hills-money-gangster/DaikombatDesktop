package com.beverly.hills.money.gang.log;

import org.apache.commons.lang3.StringUtils;

public class MyPlayerKillLog {

    static final int MAX_MSG_DURATION_MLS = 3_000;
    private String killerMessage;
    private long expireTime;


    public void myPlayerKill(String victim, int vampireHpBuff) {
        killerMessage = StringUtils.upperCase("YOU KILLED " + victim);
        if (vampireHpBuff != 0) {
            killerMessage += " +" + vampireHpBuff + " HP";
        }
        expireTime = System.currentTimeMillis() + MAX_MSG_DURATION_MLS;
    }

    public boolean hasKillerMessage() {
        return System.currentTimeMillis() <= expireTime;
    }

    public String getKillerMessage() {
        return StringUtils.defaultIfBlank(killerMessage, "");
    }

}
