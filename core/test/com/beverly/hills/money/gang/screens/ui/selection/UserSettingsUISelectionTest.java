package com.beverly.hills.money.gang.screens.ui.selection;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UserSettingsUISelectionTest {


  @BeforeEach
  public void setUp() {
    Arrays.stream(UserSettingsUISelection.values())
        .forEach(userSettingsUISelection -> userSettingsUISelection.getState().defaultSetting());
  }

  @Test
  public void testNormalizeDefault() {
    assertEquals(
        UserSettingsUISelection.SettingState.DEFAULT / 10D,
        UserSettingsUISelection.SOUND.getState().getNormalized(), 0.00001);
  }

  @Test
  public void testNormalizeAfterIncrease() {
    UserSettingsUISelection.SOUND.getState().increase();
    assertEquals(
        (UserSettingsUISelection.SettingState.DEFAULT
            + UserSettingsUISelection.SettingState.SETTING_DELTA) / 10D,
        UserSettingsUISelection.SOUND.getState().getNormalized(), 0.00001);
  }


  @Test
  public void testIncrease() {
    UserSettingsUISelection.SOUND.getState().increase();
    assertEquals(
        UserSettingsUISelection.SettingState.DEFAULT
            + UserSettingsUISelection.SettingState.SETTING_DELTA,
        UserSettingsUISelection.SOUND.getState().getSetting());
  }

  @Test
  public void testIncreaseMax() {
    for (int i = 0; i < 1000; i++) {
      UserSettingsUISelection.SOUND.getState().increase();
    }
    assertEquals(
        UserSettingsUISelection.SettingState.MAX_SETTING,
        UserSettingsUISelection.SOUND.getState().getSetting(),
        "We shouldn't be able to go over maximum value even if we increase over the top");
  }

  @Test
  public void testDecrease() {
    UserSettingsUISelection.SOUND.getState().decrease();
    assertEquals(
        UserSettingsUISelection.SettingState.DEFAULT
            - UserSettingsUISelection.SettingState.SETTING_DELTA,
        UserSettingsUISelection.SOUND.getState().getSetting());
  }

  @Test
  public void testDecreaseMin() {
    for (int i = 0; i < 1000; i++) {
      UserSettingsUISelection.SOUND.getState().decrease();
    }
    assertEquals(
        UserSettingsUISelection.SettingState.MIN_SETTING,
        UserSettingsUISelection.SOUND.getState().getSetting(),
        "We shouldn't be able to go over minimum value even if we decrease over the top");
  }
}
