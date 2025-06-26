package com.beverly.hills.money.gang.screens.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.beverly.hills.money.gang.Constants;
import com.beverly.hills.money.gang.DaiKombatGame;
import com.beverly.hills.money.gang.assets.managers.registry.FontRegistry;
import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import com.beverly.hills.money.gang.assets.managers.sound.UserSettingSound;
import com.beverly.hills.money.gang.pref.UserPreference;
import com.beverly.hills.money.gang.screens.menu.AbstractMainMenuScreen;
import com.beverly.hills.money.gang.screens.menu.MainMenuScreen;
import com.beverly.hills.money.gang.screens.ui.selection.UISelection;
import com.beverly.hills.money.gang.screens.ui.selection.UserSettingsUISelection;

public class SettingsScreen extends AbstractMainMenuScreen {

  private final UserPreference userPreference = new UserPreference();
  private final UserSettingSound dingSound1;

  private final UISelection<UserSettingsUISelection> settingsSelection
      = new UISelection<>(UserSettingsUISelection.values());

  private final BitmapFont guiFont64;

  public SettingsScreen(final DaiKombatGame game) {
    super(game);
    guiFont64 = game.getAssMan().getFont(FontRegistry.FONT_64);
    dingSound1 = game.getAssMan().getUserSettingSound(SoundRegistry.DING_1);
    settingsSelection.setMenuItemSize(64);
  }

  @Override
  public void handleInput(final float delta) {
    if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
      userPreference.setMouseSensitivity(
          UserSettingsUISelection.MOUSE_SENS.getState().getSetting());
      userPreference.setSoundVolume(UserSettingsUISelection.SOUND.getState().getSetting());
      userPreference.flush();
      removeAllEntities();
      getGame().setScreen(new MainMenuScreen(getGame()));
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
      settingsSelection.next();
      dingSound1.play(Constants.DEFAULT_SFX_VOLUME);
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
      settingsSelection.prev();
      dingSound1.play(Constants.DEFAULT_SFX_VOLUME);
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
      var setting = settingsSelection.getSelectedOption().getState();
      setting.decrease();
      this.refreshBgMusicVolume();
      dingSound1.play(Constants.DEFAULT_SFX_VOLUME);
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
      var setting = settingsSelection.getSelectedOption().getState();
      setting.increase();
      this.refreshBgMusicVolume();
      dingSound1.play(Constants.DEFAULT_SFX_VOLUME);
    }
  }


  @Override
  public void render(final float delta) {
    super.render(delta);
    getGame().getBatch().begin();

    settingsSelection.render(guiFont64, this, Constants.LOGO_INDENT);
    getGame().getBatch().end();
  }


}
