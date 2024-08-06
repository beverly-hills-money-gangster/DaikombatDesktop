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
    play(volumeType, pan, null);
  }

  public void play(SoundVolumeType volumeType, float pan, UserSettingSound extraSound) {
    var soundEntry
        = SoundVolumeTypeEntry.builder()
        .sound(sound.getSound())
        .soundVolumeType(volumeType).build();
    if (System.currentTimeMillis() - LAST_PLAYED.getOrDefault(soundEntry, 0L)
        > SHOOTING_SOUND_FREQ_MLS) {
      sound.play(volumeType, pan);
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

}
