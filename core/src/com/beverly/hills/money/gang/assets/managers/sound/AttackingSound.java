package com.beverly.hills.money.gang.assets.managers.sound;

import static com.beverly.hills.money.gang.Constants.SHOOTING_SOUND_FREQ_MLS;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

public class AttackingSound {

  private final UserSettingSound sound;

  private static final Map<SoundVolumeType, Long> LAST_PLAYED = new EnumMap<>(
      SoundVolumeType.class);

  static {
    init();
  }

  public AttackingSound(UserSettingSound sound) {
    this.sound = sound;
  }

  public void play(SoundVolumeType volumeType) {
    if (System.currentTimeMillis() - LAST_PLAYED.get(volumeType) > SHOOTING_SOUND_FREQ_MLS) {
      sound.play(volumeType);
      LAST_PLAYED.put(volumeType, System.currentTimeMillis());
    }
  }

  protected static void init() {
    Arrays.stream(SoundVolumeType.values())
        .forEach(soundVolumeType -> LAST_PLAYED.put(soundVolumeType, 0L));

  }

}
