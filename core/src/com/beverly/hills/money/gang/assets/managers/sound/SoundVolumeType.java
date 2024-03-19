package com.beverly.hills.money.gang.assets.managers.sound;

import lombok.Getter;

public enum SoundVolumeType {

  VERY_LOUD(1f), LOUD(0.8f), NORMAL(0.7f), MEDIUM(0.5f), QUITE(0.2f), MUTE(0.0f);

  @Getter
  private final float volume;

  SoundVolumeType(float volume) {
    this.volume = volume;
  }


}
