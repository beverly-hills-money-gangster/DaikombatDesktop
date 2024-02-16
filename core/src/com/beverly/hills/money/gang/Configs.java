package com.beverly.hills.money.gang;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

public interface Configs {

    String HOST = StringUtils.defaultIfBlank(System.getenv("DESKTOP_GAME_HOST"), "localhost");

    int PORT = NumberUtils.toInt(System.getenv("DESKTOP_GAME_PORT"), 7777);

    int GAME_ID = NumberUtils.toInt(System.getenv("DESKTOP_GAME_ID"), 0);

    boolean DEV_MODE = Boolean.parseBoolean(System.getenv("DESKTOP_DEV_MODE"));

    int PLAYER_MOVE_SPEED = NumberUtils.toInt(System.getenv("DESKTOP_PLAYER_MOVE_SPEED"), 5);
    float SHOOTING_DISTANCE = NumberUtils.toFloat(System.getenv("DESKTOP_SHOOTING_DISTANCE"), 7.5f);
}
