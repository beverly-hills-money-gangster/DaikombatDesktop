package com.beverly.hills.money.gang.assets.managers.sound;

import lombok.Getter;

public enum SoundVolumeType {

  HIGH_LOUD(1f), LOUD(0.9f), LOW_LOUD(0.8f), HIGH_NORMAL(0.7f), NORMAL(0.6f), LOW_NORMAL(
      0.5f), HIGH_QUIET(0.4f), QUIET(0.3f), LOW_QUIET(0.2f), MUTE(0.0f);

  @Getter
  private final float volume;

  SoundVolumeType(float volume) {
    this.volume = volume;
  }


}
