package com.beverly.hills.money.gang.assets.managers.sound;

import static com.beverly.hills.money.gang.configs.Constants.SFX_VOLUME_COEFFICIENT;

import lombok.Getter;

public enum SoundVolumeType {

  HIGH_LOUD(1f*SFX_VOLUME_COEFFICIENT), LOUD(0.85f*SFX_VOLUME_COEFFICIENT), LOW_LOUD(0.7f*SFX_VOLUME_COEFFICIENT), HIGH_NORMAL(0.6f*SFX_VOLUME_COEFFICIENT), NORMAL(0.5f*SFX_VOLUME_COEFFICIENT), LOW_NORMAL(
      0.4f*SFX_VOLUME_COEFFICIENT), HIGH_QUIET(0.3f*SFX_VOLUME_COEFFICIENT), QUIET(0.2f*SFX_VOLUME_COEFFICIENT), LOW_QUIET(0.1f*SFX_VOLUME_COEFFICIENT), MUTE(0.0f*SFX_VOLUME_COEFFICIENT);

  @Getter
  private final float volume;

  SoundVolumeType(float volume) {
    this.volume = volume;
  }


}
