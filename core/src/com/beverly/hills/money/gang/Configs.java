package com.beverly.hills.money.gang;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

public interface Configs {

    String HOST = StringUtils.defaultIfBlank(System.getenv("DESKTOP_GAME_HOST"), "localhost");

    int PORT = NumberUtils.toInt(System.getenv("DESKTOP_GAME_PORT"), 7777);

    int GAME_ID = NumberUtils.toInt(System.getenv("DESKTOP_GAME_ID"), 0);

    boolean DEV_MODE = Boolean.parseBoolean(System.getenv("DESKTOP_DEV_MODE"));

    boolean MIMIC_CONSTANT_NETWORK_ACTIVITY = Boolean.parseBoolean(System.getenv("DESKTOP_MIMIC_CONSTANT_NETWORK_ACTIVITY"));

    int PLAYER_MOVE_SPEED = NumberUtils.toInt(System.getenv("DESKTOP_PLAYER_MOVE_SPEED"), 5);
    float SHOOTING_DISTANCE = NumberUtils.toFloat(System.getenv("DESKTOP_SHOOTING_DISTANCE"), 7.5f);

    float MELEE_DISTANCE = NumberUtils.toFloat(System.getenv("DESKTOP_MELEE_DISTANCE"), 0.3f);

    int FLUSH_ACTIONS_FREQ_MLS
            = NumberUtils.toInt(System.getenv("DESKTOP_FLUSH_ACTIONS_FREQ_MLS"), 50);
}
