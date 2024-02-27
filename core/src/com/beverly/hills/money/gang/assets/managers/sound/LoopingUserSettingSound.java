package com.beverly.hills.money.gang.assets.managers.sound;

import com.badlogic.gdx.audio.Sound;
import com.beverly.hills.money.gang.screens.ui.selection.UserSettingsUISelection;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LoopingUserSettingSound {
    private final Sound sound;
    private float lastVolume;
    private long lastSoundId = -1;

    public void loop(float volume) {
        lastSoundId = sound.loop(volume * UserSettingsUISelection.SOUND.getState().getNormalized());
        lastVolume = volume;
    }

    public void refreshVolume() {
        sound.setVolume(lastSoundId, lastVolume * UserSettingsUISelection.SOUND.getState().getNormalized());
    }

    public void stop() {
        sound.stop();
    }
}
