package com.beverly.hills.money.gang.pref;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class UserPreference {

  private final Preferences prefs = Gdx.app.getPreferences("Daikombat Preferences");

  private static final String SOUND_VOLUME_PREF_KEY = "sound_volume";
  private static final String MOUSE_SENS_PREF_KEY = "mouse_sens";

  public int getSoundVolume() {
    return prefs.getInteger(SOUND_VOLUME_PREF_KEY, 10);
  }

  public int getMouseSensitivity() {
    return prefs.getInteger(MOUSE_SENS_PREF_KEY, 10);
  }

  public void setSoundVolume(int volume) {
    prefs.putInteger(SOUND_VOLUME_PREF_KEY, volume);
  }

  public void setMouseSensitivity(int sensitivity) {
    prefs.putInteger(MOUSE_SENS_PREF_KEY, sensitivity);
  }

  public void flush() {
    prefs.flush();
  }
}
