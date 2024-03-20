package com.beverly.hills.money.gang.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.beverly.hills.money.gang.Configs;
import com.beverly.hills.money.gang.Constants;
import com.beverly.hills.money.gang.DaiKombatGame;
import com.beverly.hills.money.gang.assets.managers.registry.FontRegistry;
import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import com.beverly.hills.money.gang.assets.managers.sound.UserSettingSound;
import com.beverly.hills.money.gang.screens.data.JoinGameData;
import com.beverly.hills.money.gang.screens.ui.selection.ServerUISelection;
import com.beverly.hills.money.gang.screens.ui.selection.UISelection;

public class ChooseServerScreen extends AbstractMainMenuScreen {

  private final BitmapFont guiFont64;
  private final UserSettingSound boomSound1;
  private final UserSettingSound dingSound1;
  private final UISelection<ServerUISelection> serverSelection = new UISelection<>(
      ServerUISelection.values());


  public ChooseServerScreen(final DaiKombatGame game) {
    super(game);
    guiFont64 = game.getAssMan().getFont(FontRegistry.FONT_64);

    boomSound1 = game.getAssMan().getUserSettingSound(SoundRegistry.BOOM_1);
    dingSound1 = game.getAssMan().getUserSettingSound(SoundRegistry.DING_1);
  }

  @Override
  public void handleInput(final float delta) {

    if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
      boomSound1.play(Constants.DEFAULT_SFX_VOLUME);
      switch (serverSelection.getSelectedOption()) {
        case OFFICIAL -> {
          removeAllEntities();
          getGame().setScreen(new EnterYourNameScreen(getGame(),
              JoinGameData.builder()
                  .serverHost(Configs.HOST)
                  .serverPort(Configs.PORT)));
        }
        case CUSTOM -> {
          removeAllEntities();
          getGame().setScreen(new EnterServerAddressScreen(getGame()));
        }

        default -> Gdx.app.exit();

      }
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
      removeAllEntities();
      getGame().setScreen(new MainMenuScreen(getGame()));
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
      serverSelection.up();
      dingSound1.play(Constants.DEFAULT_SFX_VOLUME);
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
      serverSelection.down();
      dingSound1.play(Constants.DEFAULT_SFX_VOLUME);
    }
  }


  @Override
  public void render(final float delta) {
    super.render(delta);
    getGame().getBatch().begin();
    serverSelection.render(guiFont64, this, Constants.LOGO_INDENT);
    getGame().getBatch().end();
  }

}
