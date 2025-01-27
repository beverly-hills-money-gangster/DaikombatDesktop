package com.beverly.hills.money.gang.assets.managers.sound;

import com.badlogic.gdx.audio.Sound;
import com.beverly.hills.money.gang.screens.ui.selection.UserSettingsUISelection;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserSettingSound {

  @Getter
  private final Sound sound;

  public void play(final SoundVolumeType volumeType, float pan) {
    play(SoundConf.builder().volume(volumeType.getVolume()).pan(pan).build());
  }

  public void play(float volume) {
    play(SoundConf.builder().volume(volume).build());
  }

  public void play(final SoundConf soundConf) {
    sound.play(soundConf.volume * UserSettingsUISelection.SOUND.getState().getNormalized(),
        soundConf.pitch, soundConf.pan);
  }

  public void loop(float volume) {
    sound.loop(volume * UserSettingsUISelection.SOUND.getState().getNormalized());
  }


  public void stop() {
    sound.stop();
  }

  @Builder
  @Getter
  public static class SoundConf {

    @NonNull
    private final Float volume;
    @Builder.Default
    private final float pan = 0;
    @Builder.Default
    private final float pitch = 1;

  }

}
