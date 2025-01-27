package com.beverly.hills.money.gang.assets.managers.sound;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.badlogic.gdx.audio.Sound;
import com.beverly.hills.money.gang.assets.managers.sound.UserSettingSound.SoundConf;
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
    userSettingSound.play(0.5f);
    verify(sound).play(0.25f, 1, 0);
  }

  @Test
  public void testPlayWithPan() {
    userSettingSound.play(SoundConf.builder().volume(0.5f).pan(-1).build());
    verify(sound).play(0.25f, 1, -1);
  }

  @Test
  public void testPlayWithPitch() {
    userSettingSound.play(SoundConf.builder().volume(0.5f).pitch(1.5f).build());
    verify(sound).play(0.25f, 1.5f, 0);
  }

  @Test
  public void testPlayDefaults() {
    userSettingSound.play(SoundConf.builder().volume(0.5f).build());
    verify(sound).play(0.25f, 1, 0);
  }

  @Test
  public void testPlayCustomSettingsIncrease() {
    UserSettingsUISelection.SOUND.getState().increase();
    userSettingSound.play(0.5f);
    verify(sound).play(0.3f, 1, 0);
  }

  @Test
  public void testPlayCustomSettingsDecrease() {
    UserSettingsUISelection.SOUND.getState().decrease();
    userSettingSound.play(0.5f);
    verify(sound).play(0.2f, 1, 0);
  }

  @Test
  public void testLoopDecrease() {
    UserSettingsUISelection.SOUND.getState().decrease();
    userSettingSound.loop(0.5f);
    verify(sound).loop(0.2f);
  }

  @Test
  public void testLoopIncrease() {
    UserSettingsUISelection.SOUND.getState().increase();
    userSettingSound.loop(0.5f);
    verify(sound).loop(0.3f);
  }
}
