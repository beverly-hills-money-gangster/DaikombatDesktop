package com.beverly.hills.money.gang.assets.managers.sound;

import static com.beverly.hills.money.gang.configs.Constants.TIME_LIMITED_SOUND_FREQ_MLS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.badlogic.gdx.audio.Sound;
import com.beverly.hills.money.gang.assets.managers.sound.TimeLimitedSound.TimeLimitSoundConf;
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
    attackingSound1.play(
        TimeLimitSoundConf.builder().soundVolumeType(SoundVolumeType.LOUD).pan(1).build());

    verify(userSettingSound1).play(argThat(argument -> {
      assertEquals(1, argument.getPan());
      assertEquals(1, argument.getPitch());
      assertEquals(SoundVolumeType.LOUD.getVolume(), argument.getVolume());
      return true;
    }));

    attackingSound2.play(
        TimeLimitSoundConf.builder().soundVolumeType(SoundVolumeType.LOUD).pan(1).build());
    verify(userSettingSound2).play(argThat(argument -> {
      assertEquals(1, argument.getPan());
      assertEquals(1, argument.getPitch());
      assertEquals(SoundVolumeType.LOUD.getVolume(), argument.getVolume());
      return true;
    }));
  }

  @Test
  public void testPlayFirstTime() {
    timeLimitedSound.play(
        TimeLimitSoundConf.builder().soundVolumeType(SoundVolumeType.LOUD).pan(1).build());

    verify(userSettingSound).play(argThat(argument -> {
      assertEquals(1, argument.getPan());
      assertEquals(1, argument.getPitch());
      assertEquals(SoundVolumeType.LOUD.getVolume(), argument.getVolume());
      return true;
    }));

  }

  @Test
  public void testPlayFirstTimeDefaults() {
    timeLimitedSound.play(
        TimeLimitSoundConf.builder().soundVolumeType(SoundVolumeType.LOUD).build());
    verify(userSettingSound).play(argThat(argument -> {
      assertEquals(0, argument.getPan());
      assertEquals(1, argument.getPitch());
      assertEquals(SoundVolumeType.LOUD.getVolume(), argument.getVolume());
      return true;
    }));
  }

  @Test
  public void testPlayFirstWithPanAndPitch() {
    timeLimitedSound.play(
        TimeLimitSoundConf.builder().soundVolumeType(SoundVolumeType.LOUD).pan(0.5f).pitch(1.5f)
            .build());
    verify(userSettingSound).play(argThat(argument -> {
      assertEquals(0.5f, argument.getPan());
      assertEquals(1.5f, argument.getPitch());
      assertEquals(SoundVolumeType.LOUD.getVolume(), argument.getVolume());
      return true;
    }));
  }

  @Test
  public void testPlayTooOften() {
    // played recently
    timeLimitedSound.play(
        TimeLimitSoundConf.builder().soundVolumeType(SoundVolumeType.LOUD).pan(1).build());

    // play again
    timeLimitedSound.play(
        TimeLimitSoundConf.builder().soundVolumeType(SoundVolumeType.LOUD).pan(1).build());

    verify(userSettingSound).play(argThat(argument -> {
      assertEquals(1, argument.getPan());
      assertEquals(1, argument.getPitch());
      assertEquals(SoundVolumeType.LOUD.getVolume(), argument.getVolume());
      return true;
    }));
  }

  @Test
  public void testPlayWait() throws InterruptedException {
    // played recently
    timeLimitedSound.play(
        TimeLimitSoundConf.builder().soundVolumeType(SoundVolumeType.LOUD).pan(-1).build());

    Thread.sleep(TIME_LIMITED_SOUND_FREQ_MLS + 10);

    // play again
    timeLimitedSound.play(
        TimeLimitSoundConf.builder().soundVolumeType(SoundVolumeType.LOUD).pan(-1).build());

    verify(userSettingSound, times(2)).play(argThat(argument -> {
      assertEquals(-1, argument.getPan());
      assertEquals(1, argument.getPitch());
      assertEquals(SoundVolumeType.LOUD.getVolume(), argument.getVolume());
      return true;
    }));
  }

  @Test
  public void testPlayDifferentSoundsVolumeTypes() {
    Arrays.stream(SoundVolumeType.values())
        .forEach(soundVolumeType -> timeLimitedSound.play(
            TimeLimitSoundConf.builder().soundVolumeType(soundVolumeType).pan(0).build()));
    Arrays.stream(SoundVolumeType.values())
        .forEach(soundVolumeType -> verify(userSettingSound).play(argThat(
            argument -> 0 == argument.getPan() && 1 == argument.getPitch()
                && soundVolumeType.getVolume() == argument.getVolume())));

  }

  @Test
  public void testPlayDifferentSoundVolumeTypesTwice() {
    // first time
    Arrays.stream(SoundVolumeType.values())
        .forEach(soundVolumeType -> timeLimitedSound.play(
            TimeLimitSoundConf.builder().soundVolumeType(soundVolumeType).pan(1).build()));

    // second time
    Arrays.stream(SoundVolumeType.values())
        .forEach(soundVolumeType -> timeLimitedSound.play(
            TimeLimitSoundConf.builder().soundVolumeType(soundVolumeType).pan(1).build()));

    // played once only
    Arrays.stream(SoundVolumeType.values())
        .forEach(soundVolumeType -> verify(userSettingSound, times(1))
            .play(argThat(argument -> 1 == argument.getPan() && 1 == argument.getPitch()
                && soundVolumeType.getVolume() == argument.getVolume())));

  }

  @Test
  public void testPlayDifferentSoundVolumeTypesWait() throws InterruptedException {
    // first time
    Arrays.stream(SoundVolumeType.values())
        .forEach(soundVolumeType -> timeLimitedSound.play(
            TimeLimitSoundConf.builder().soundVolumeType(soundVolumeType).pan(1).build()));

    Thread.sleep(TIME_LIMITED_SOUND_FREQ_MLS + 10);

    // second time
    Arrays.stream(SoundVolumeType.values())
        .forEach(soundVolumeType -> timeLimitedSound.play(
            TimeLimitSoundConf.builder().soundVolumeType(soundVolumeType).pan(1).build()));

    // played once only
    Arrays.stream(SoundVolumeType.values())
        .forEach(soundVolumeType -> verify(userSettingSound, times(2))
            .play(argThat(argument -> 1 == argument.getPan() && 1 == argument.getPitch()
                && soundVolumeType.getVolume() == argument.getVolume())));

  }
}
