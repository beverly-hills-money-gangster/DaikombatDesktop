package com.beverly.hills.money.gang.assets.managers.sound;

import lombok.Getter;

public enum SoundVolumeType {

    VERY_LOUD(0.9f), LOUD(0.7f), MEDIUM(0.3f), QUITE(0.1f);

    @Getter
    private final float volume;

    SoundVolumeType(float volume) {
        this.volume = volume;
    }


}
