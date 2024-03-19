package com.beverly.hills.money.gang.screens.ui.selection;

import lombok.Getter;
import lombok.Setter;

public enum UserSettingsUISelection {

  SOUND("SOUND VOLUME", new SettingState()),
  MOUSE_SENS("MOUSE SENSITIVITY", new SettingState());

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

    protected static final int DEFAULT = 10;
    protected static final int MAX_SETTING = 30;
    protected static final int MIN_SETTING = 0;
    protected static final int SETTING_DELTA = 3;

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
      return setting / 10f;
    }

    @Override
    public String toString() {
      return "|".repeat(setting) + ".".repeat(MAX_SETTING - setting);
    }


  }
}
