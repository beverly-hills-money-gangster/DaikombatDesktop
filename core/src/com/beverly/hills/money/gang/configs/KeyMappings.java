package com.beverly.hills.money.gang.configs;

import com.badlogic.gdx.Input.Keys;
import java.util.Locale;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum KeyMappings {

  TALK(Keys.V),
  CHAT(Keys.valueOf("`")),
  LEADERBOARD(Keys.TAB),
  REFRESH(Keys.R),
  PREV_WEAPONS(Keys.Q),
  NEXT_WEAPONS(Keys.E),
  TAUNT(Keys.X);


  @Getter
  private final int key;

  public String getKeyName() {
    return Keys.toString(key).toUpperCase(Locale.ENGLISH);
  }

}
