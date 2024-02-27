package com.beverly.hills.money.gang.assets.managers.sound;

import com.badlogic.gdx.audio.Sound;
import com.beverly.hills.money.gang.screens.ui.selection.UserSettingsUISelection;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserSettingSound {

    @Getter
    private final Sound sound;



    public void play(SoundVolumeType volumeType) {
        play(volumeType.getVolume());
    }

    public void play(float volume) {
        sound.play(volume * UserSettingsUISelection.SOUND.getState().getNormalized());
    }

    public void loop(float volume) {
        sound.loop(volume * UserSettingsUISelection.SOUND.getState().getNormalized());

    }


    public void stop() {
        sound.stop();
    }

}
