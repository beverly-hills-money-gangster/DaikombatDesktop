package com.beverly.hills.money.gang.assets.managers.sound;

import com.badlogic.gdx.audio.Sound;
import com.beverly.hills.money.gang.screens.ui.selection.UserSettingsUISelection;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserSettingSound {

  @Getter
  private final Sound sound;

  public void play(SoundVolumeType volumeType, float pan) {
    if (volumeType == SoundVolumeType.MUTE) {
      return;
    }
    play(volumeType.getVolume(), pan);
  }


  public void play(SoundVolumeType volumeType) {
    play(volumeType.getVolume(), 0);
  }

  public void play(float volume) {
    play(volume, 0);
  }

  public void play(float volume, float pan) {
    sound.play(volume * UserSettingsUISelection.SOUND.getState().getNormalized(), 1, pan);
  }

  public void loop(float volume) {
    sound.loop(volume * UserSettingsUISelection.SOUND.getState().getNormalized());
  }

  public void stop() {
    sound.stop();
  }

}
