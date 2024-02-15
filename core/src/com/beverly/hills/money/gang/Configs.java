package com.beverly.hills.money.gang;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

public interface Configs {

    String HOST = StringUtils.defaultIfBlank(System.getenv("GAME_HOST"), "localhost");

    int PORT = NumberUtils.toInt(System.getenv("GAME_PORT"), 7777);

    int GAME_ID = NumberUtils.toInt(System.getenv("GAME_ID"), 0);

    boolean DEV_MODE = Boolean.parseBoolean(System.getenv("DEV_MODE"));
}
