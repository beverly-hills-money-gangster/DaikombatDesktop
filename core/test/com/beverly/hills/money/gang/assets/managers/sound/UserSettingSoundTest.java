package com.beverly.hills.money.gang.assets.managers.sound;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.badlogic.gdx.audio.Sound;
import com.beverly.hills.money.gang.screens.ui.selection.UserSettingsUISelection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UserSettingSoundTest {

  private UserSettingSound userSettingSound;

  private Sound sound;

  @BeforeEach
  public void setUp() {
    sound = mock(Sound.class);
    userSettingSound = new UserSettingSound(sound);
    UserSettingsUISelection.SOUND.getState().defaultSetting();
  }

  @Test
  public void testPlayDefaultSettings() {
    userSettingSound.play(10);
    verify(sound).play(10, 1, 0);
  }

  @Test
  public void testPlayWithPan() {
    userSettingSound.play(10, -1);
    verify(sound).play(10, 1, -1);
  }

  @Test
  public void testPlayCustomSettingsIncrease() {
    UserSettingsUISelection.SOUND.getState().increase();
    userSettingSound.play(10);
    verify(sound).play(13f, 1, 0);
  }

  @Test
  public void testPlayCustomSettingsDecrease() {
    UserSettingsUISelection.SOUND.getState().decrease();
    userSettingSound.play(10);
    verify(sound).play(7f, 1, 0);
  }

  @Test
  public void testLoopDecrease() {
    UserSettingsUISelection.SOUND.getState().decrease();
    userSettingSound.loop(10);
    verify(sound).loop(7f);
  }

  @Test
  public void testLoopIncrease() {
    UserSettingsUISelection.SOUND.getState().increase();
    userSettingSound.loop(10);
    verify(sound).loop(13f);
  }
}
