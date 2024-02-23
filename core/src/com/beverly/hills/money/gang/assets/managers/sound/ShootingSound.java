package com.beverly.hills.money.gang.assets.managers.sound;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.beverly.hills.money.gang.Constants.SHOOTING_SOUND_FREQ_MLS;

public class ShootingSound {

    private final UserSettingSound sound;

    private static final Map<SoundVolumeType, Long> LAST_PLAYED = new HashMap<>();

    static {
        init();
    }

    public ShootingSound(UserSettingSound sound) {
        this.sound = sound;
    }

    public void play(SoundVolumeType volumeType) {
        if (System.currentTimeMillis() - LAST_PLAYED.get(volumeType) > SHOOTING_SOUND_FREQ_MLS) {
            sound.play(volumeType);
            LAST_PLAYED.put(volumeType, System.currentTimeMillis());
        }
    }

    protected static void init() {
        Arrays.stream(SoundVolumeType.values()).forEach(soundVolumeType -> LAST_PLAYED.put(soundVolumeType, 0L));

    }

}
