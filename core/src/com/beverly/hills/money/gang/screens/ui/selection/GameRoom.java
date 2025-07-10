package com.beverly.hills.money.gang.screens.ui.selection;

import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Builder
@Getter
public class GameRoom {

  private String cachedToString;
  private final int roomId;
  private final String title;
  private final String description;
  private final int playersOnline;
  private final String mapName;
  private final String mapHash;


  @Override
  public String toString() {
    if (cachedToString == null) {
      cachedToString =
          StringUtils.rightPad(
              StringUtils.defaultIfBlank(title, " Game room " + roomId).toUpperCase(), 20, ".")
              + playersOnline + " ONLINE";
    }
    return cachedToString;
  }
}
