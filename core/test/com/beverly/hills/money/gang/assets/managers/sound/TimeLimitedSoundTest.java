package com.beverly.hills.money.gang.assets.managers.sound;

import static com.beverly.hills.money.gang.Constants.TIME_LIMITED_SOUND_FREQ_MLS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.badlogic.gdx.audio.Sound;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TimeLimitedSoundTest {

  private TimeLimitedSound timeLimitedSound;

  private UserSettingSound userSettingSound;

  @BeforeEach
  public void setUp() {
    userSettingSound = mock(UserSettingSound.class);
    doReturn(mock(Sound.class)).when(userSettingSound).getSound();
    timeLimitedSound = new TimeLimitedSound(userSettingSound);
  }

  @Test
  public void testPlayDifferentSoundsSameVolume() {
    var userSettingSound1 = mock(UserSettingSound.class);
    doReturn(mock(Sound.class)).when(userSettingSound1).getSound();
    var attackingSound1 = new TimeLimitedSound(userSettingSound1);

    var userSettingSound2 = mock(UserSettingSound.class);
    doReturn(mock(Sound.class)).when(userSettingSound2).getSound();
    var attackingSound2 = new TimeLimitedSound(userSettingSound2);

    // it's same volume but different sound. both must be played
    attackingSound1.play(SoundVolumeType.LOUD, 1);
    verify(userSettingSound1).play(SoundVolumeType.LOUD, 1);

    attackingSound2.play(SoundVolumeType.LOUD, 1);
    verify(userSettingSound2).play(SoundVolumeType.LOUD, 1);
  }

  @Test
  public void testPlayFirstTime() {
    timeLimitedSound.play(SoundVolumeType.LOUD, 1);
    verify(userSettingSound).play(SoundVolumeType.LOUD, 1);
  }

  @Test
  public void testPlayTooOften() {
    // played recently
    timeLimitedSound.play(SoundVolumeType.LOUD, 1);

    // play again
    timeLimitedSound.play(SoundVolumeType.LOUD, 1);

    verify(userSettingSound, times(1)).play(SoundVolumeType.LOUD, 1); // play only once
  }

  @Test
  public void testPlayWait() throws InterruptedException {
    // played recently
    timeLimitedSound.play(SoundVolumeType.LOUD, -1);

    Thread.sleep(TIME_LIMITED_SOUND_FREQ_MLS + 10);

    // play again
    timeLimitedSound.play(SoundVolumeType.LOUD, -1);

    verify(userSettingSound, times(2)).play(SoundVolumeType.LOUD, -1); // play twice
  }

  @Test
  public void testPlayDifferentSoundsVolumeTypes() {
    Arrays.stream(SoundVolumeType.values())
        .forEach(soundVolumeType -> timeLimitedSound.play(soundVolumeType, 0));

    Arrays.stream(SoundVolumeType.values())
        .forEach(soundVolumeType -> verify(userSettingSound).play(soundVolumeType, 0));

  }

  @Test
  public void testPlayDifferentSoundVolumeTypesTwice() {
    // first time
    Arrays.stream(SoundVolumeType.values())
        .forEach(soundVolumeType -> timeLimitedSound.play(soundVolumeType, 1));

    // second time
    Arrays.stream(SoundVolumeType.values())
        .forEach(soundVolumeType -> timeLimitedSound.play(soundVolumeType, 1));

    // played once only
    Arrays.stream(SoundVolumeType.values())
        .forEach(soundVolumeType -> verify(userSettingSound, times(1))
            .play(soundVolumeType, 1));

  }

  @Test
  public void testPlayDifferentSoundVolumeTypesWait() throws InterruptedException {
    // first time
    Arrays.stream(SoundVolumeType.values())
        .forEach(soundVolumeType -> timeLimitedSound.play(soundVolumeType, 1));

    Thread.sleep(TIME_LIMITED_SOUND_FREQ_MLS + 10);

    // second time
    Arrays.stream(SoundVolumeType.values())
        .forEach(soundVolumeType -> timeLimitedSound.play(soundVolumeType, 1));

    // played once only
    Arrays.stream(SoundVolumeType.values())
        .forEach(soundVolumeType -> verify(userSettingSound, times(2))
            .play(soundVolumeType, 1));

  }
}
