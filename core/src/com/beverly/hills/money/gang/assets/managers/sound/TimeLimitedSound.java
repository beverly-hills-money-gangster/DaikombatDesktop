package com.beverly.hills.money.gang.assets.managers.sound;

import static com.beverly.hills.money.gang.Constants.TIME_LIMITED_SOUND_FREQ_MLS;

import com.badlogic.gdx.audio.Sound;
import com.beverly.hills.money.gang.assets.managers.sound.UserSettingSound.SoundConf;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

public class TimeLimitedSound {

  private final UserSettingSound sound;

  private static final Map<SoundVolumeTypeEntry, Long> LAST_PLAYED = new HashMap<>();

  public TimeLimitedSound(UserSettingSound sound) {
    this.sound = sound;
  }


  public void play(final @NonNull TimeLimitSoundConf soundConf) {
    play(soundConf.soundVolumeType, soundConf.pitch, soundConf.pan, soundConf.extraSound,
        soundConf.frequencyMls);
  }

  private void play(
      final SoundVolumeType volumeType,
      float pitch,
      float pan,
      UserSettingSound extraSound,
      final int frequencyMls) {
    var soundEntry
        = SoundVolumeTypeEntry.builder()
        .sound(sound.getSound())
        .soundVolumeType(volumeType).build();
    if (System.currentTimeMillis() - LAST_PLAYED.getOrDefault(soundEntry, 0L)
        > frequencyMls) {
      sound.play(SoundConf.builder().volume(volumeType.getVolume()).pitch(pitch).pan(pan).build());
      Optional.ofNullable(extraSound)
          .ifPresent(userSettingSound -> userSettingSound.play(volumeType, pan));
      LAST_PLAYED.put(soundEntry, System.currentTimeMillis());
    }
  }

  @Builder
  @Getter
  @EqualsAndHashCode
  private static class SoundVolumeTypeEntry {

    private final Sound sound;
    private final SoundVolumeType soundVolumeType;
  }

  public static void clear() {
    LAST_PLAYED.clear();
  }


  @Builder
  public static class TimeLimitSoundConf {

    @NonNull
    private final SoundVolumeType soundVolumeType;
    @Builder.Default
    private final float pan = 0;
    @Builder.Default
    private final float pitch = 1;
    @Builder.Default
    private final int frequencyMls = TIME_LIMITED_SOUND_FREQ_MLS;
    private final UserSettingSound extraSound;

  }

}
