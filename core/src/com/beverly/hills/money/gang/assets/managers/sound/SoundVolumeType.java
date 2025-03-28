package com.beverly.hills.money.gang.assets.managers.sound;

import lombok.Getter;

public enum SoundVolumeType {

  HIGH_LOUD(0.9f/5), LOUD(0.8f/5), LOW_LOUD(0.7f/5), HIGH_NORMAL(0.6f/5), NORMAL(0.5f/5), LOW_NORMAL(
      0.4f/5), HIGH_QUIET(0.3f/5), QUIET(0.2f/5), LOW_QUIET(0.1f/5), MUTE(0.0f);

  @Getter
  private final float volume;

  SoundVolumeType(float volume) {
    this.volume = volume;
  }


}
