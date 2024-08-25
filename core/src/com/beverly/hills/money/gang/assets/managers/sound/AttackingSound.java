package com.beverly.hills.money.gang.assets.managers.sound;

import static com.beverly.hills.money.gang.Constants.SHOOTING_SOUND_FREQ_MLS;

import com.badlogic.gdx.audio.Sound;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

public class AttackingSound {

  private final UserSettingSound sound;

  private static final Map<SoundVolumeTypeEntry, Long> LAST_PLAYED = new HashMap<>();


  public AttackingSound(UserSettingSound sound) {
    this.sound = sound;
  }

  public void play(SoundVolumeType volumeType, float pan) {
    play(volumeType, pan, null, SHOOTING_SOUND_FREQ_MLS);
  }

  public void play(SoundVolumeType volumeType, float pan, final int frequencyMls) {
    play(volumeType, pan, null, frequencyMls);
  }

  public void play(SoundVolumeType volumeType, float pan, UserSettingSound extraSound,
      final int frequencyMls) {
    var soundEntry
        = SoundVolumeTypeEntry.builder()
        .sound(sound.getSound())
        .soundVolumeType(volumeType).build();
    if (System.currentTimeMillis() - LAST_PLAYED.getOrDefault(soundEntry, 0L)
        > frequencyMls) {
      sound.play(volumeType, pan);
      Optional.ofNullable(extraSound)
          .ifPresent(userSettingSound -> userSettingSound.play(volumeType, pan));
      LAST_PLAYED.put(soundEntry, System.currentTimeMillis());
    }
  }

  public void play(SoundVolumeType volumeType, float pan, UserSettingSound extraSound) {
    play(volumeType, pan, extraSound, SHOOTING_SOUND_FREQ_MLS);
  }

  @Builder
  @Getter
  @EqualsAndHashCode
  private static class SoundVolumeTypeEntry {

    private final Sound sound;
    private final SoundVolumeType soundVolumeType;
  }

}
