package com.beverly.hills.money.gang.screens.ui.selection;

import lombok.Getter;
import lombok.Setter;

public enum UserSettingsUISelection {

  SOUND("SOUND EFFECTS", new SettingState()),
  VOICE("VOICE CHAT", new SettingState()),
  MOUSE_SENS("SENSITIVITY", new SettingState());

  @Getter
  private final SettingState state;

  @Getter
  private final String title;

  UserSettingsUISelection(String title, SettingState state) {
    this.title = title;
    this.state = state;
  }

  @Override
  public String toString() {
    return this.title + " " + this.getState().toString();
  }

  public static class SettingState {

    public static final int DEFAULT = 5;
    protected static final int MAX_SETTING = 10;
    protected static final int MIN_SETTING = 0;
    protected static final int SETTING_DELTA = 1;

    @Getter
    @Setter
    private int setting = DEFAULT;

    public void defaultSetting() {
      setting = DEFAULT;
    }

    public void increase() {
      setting = Math.min(MAX_SETTING, setting + SETTING_DELTA);
    }

    public void decrease() {
      setting = Math.max(MIN_SETTING, setting - SETTING_DELTA);
    }

    public float getNormalized() {
      return (setting / (float) MAX_SETTING);
    }

    @Override
    public String toString() {
      return "|".repeat(setting) + ".".repeat(MAX_SETTING - setting);
    }


  }
}
