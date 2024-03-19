package com.beverly.hills.money.gang.assets.managers.sound;

import static com.beverly.hills.money.gang.Constants.SHOOTING_SOUND_FREQ_MLS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AttackingSoundTest {

  private AttackingSound attackingSound;

  private UserSettingSound userSettingSound;

  @BeforeEach
  public void setUp() {
    userSettingSound = mock(UserSettingSound.class);
    attackingSound = new AttackingSound(userSettingSound);
    AttackingSound.init();
  }

  @Test
  public void testPlayFirstTime() {
    attackingSound.play(SoundVolumeType.LOUD);
    verify(userSettingSound).play(SoundVolumeType.LOUD);
  }

  @Test
  public void testPlayTooOften() {
    // played recently
    attackingSound.play(SoundVolumeType.LOUD);

    // play again
    attackingSound.play(SoundVolumeType.LOUD);

    verify(userSettingSound, times(1)).play(SoundVolumeType.LOUD); // play only once
  }

  @Test
  public void testPlayWait() throws InterruptedException {
    // played recently
    attackingSound.play(SoundVolumeType.LOUD);

    Thread.sleep(SHOOTING_SOUND_FREQ_MLS + 10);

    // play again
    attackingSound.play(SoundVolumeType.LOUD);

    verify(userSettingSound, times(2)).play(SoundVolumeType.LOUD); // play twice
  }

  @Test
  public void testPlayDifferentSounds() {
    Arrays.stream(SoundVolumeType.values())
        .forEach(soundVolumeType -> attackingSound.play(soundVolumeType));

    Arrays.stream(SoundVolumeType.values())
        .forEach(soundVolumeType -> verify(userSettingSound).play(soundVolumeType));

  }

  @Test
  public void testPlayDifferentSoundsTwice() {
    // first time
    Arrays.stream(SoundVolumeType.values())
        .forEach(soundVolumeType -> attackingSound.play(soundVolumeType));

    // second time
    Arrays.stream(SoundVolumeType.values())
        .forEach(soundVolumeType -> attackingSound.play(soundVolumeType));

    // played once only
    Arrays.stream(SoundVolumeType.values())
        .forEach(soundVolumeType -> verify(userSettingSound, times(1))
            .play(soundVolumeType));

  }

  @Test
  public void testPlayDifferentSoundsWait() throws InterruptedException {
    // first time
    Arrays.stream(SoundVolumeType.values())
        .forEach(soundVolumeType -> attackingSound.play(soundVolumeType));

    Thread.sleep(SHOOTING_SOUND_FREQ_MLS + 10);

    // second time
    Arrays.stream(SoundVolumeType.values())
        .forEach(soundVolumeType -> attackingSound.play(soundVolumeType));

    // played once only
    Arrays.stream(SoundVolumeType.values())
        .forEach(soundVolumeType -> verify(userSettingSound, times(2))
            .play(soundVolumeType));

  }
}
