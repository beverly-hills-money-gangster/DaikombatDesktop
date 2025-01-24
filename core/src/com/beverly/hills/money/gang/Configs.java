package com.beverly.hills.money.gang;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

public interface Configs {

  String HOST = StringUtils.defaultIfBlank(System.getenv("DESKTOP_GAME_HOST"), "64.226.66.192");

  int PORT = NumberUtils.toInt(System.getenv("DESKTOP_GAME_PORT"), 7777);

  int GAME_ID = NumberUtils.toInt(System.getenv("DESKTOP_GAME_ID"), 0);

  boolean DEV_MODE = Boolean.parseBoolean(System.getenv("DESKTOP_DEV_MODE"));

  int SECONDARY_CONNECTIONS_TO_OPEN = NumberUtils.toInt(
      System.getenv("DESKTOP_SECONDARY_CONNECTIONS_TO_OPEN"), 3);

  int MAX_RECONNECTIONS = NumberUtils.toInt(
      System.getenv("DESKTOP_MAX_RECONNECTIONS"), 5);

  int SPEED_BOOST = NumberUtils.toInt(System.getenv("DESKTOP_SPEED_BOOST"), 1);

}
