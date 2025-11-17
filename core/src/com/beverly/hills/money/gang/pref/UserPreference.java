package com.beverly.hills.money.gang.pref;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.beverly.hills.money.gang.screens.ui.selection.UserSettingsUISelection;
import com.beverly.hills.money.gang.screens.ui.selection.UserSettingsUISelection.SettingState;

public class UserPreference {

  private final Preferences prefs = Gdx.app.getPreferences("Daikombat Preferences");


  public int get(final UserSettingsUISelection selection) {
    return prefs.getInteger(selection.name(), SettingState.DEFAULT);
  }

  public void set(final UserSettingsUISelection selection, int value) {
    prefs.putInteger(selection.name(), value);
  }

  public void flush() {
    prefs.flush();
  }
}
