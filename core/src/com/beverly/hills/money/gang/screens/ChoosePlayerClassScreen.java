package com.beverly.hills.money.gang.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.beverly.hills.money.gang.Constants;
import com.beverly.hills.money.gang.DaiKombatGame;
import com.beverly.hills.money.gang.assets.managers.registry.FontRegistry;
import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import com.beverly.hills.money.gang.assets.managers.sound.UserSettingSound;
import com.beverly.hills.money.gang.screens.data.ConnectGameData;
import com.beverly.hills.money.gang.screens.ui.selection.PlayerClassUISelection;
import com.beverly.hills.money.gang.screens.ui.selection.UISelection;

public class ChoosePlayerClassScreen extends AbstractMainMenuScreen {

  private final BitmapFont guiFont64;
  private final UserSettingSound boomSound1;
  private final UserSettingSound dingSound1;

  private static final String SELECT_CLASS = "SELECT YOUR VAMPIRE CLASS";

  private final UISelection<PlayerClassUISelection> playerClassSelection = new UISelection<>(
      PlayerClassUISelection.values());

  private final ConnectGameData.ConnectGameDataBuilder joinGameDataBuilder;

  public ChoosePlayerClassScreen(final DaiKombatGame game,
      final ConnectGameData.ConnectGameDataBuilder joinGameDataBuilder) {
    super(game);
    guiFont64 = game.getAssMan().getFont(FontRegistry.FONT_64);
    this.joinGameDataBuilder = joinGameDataBuilder;
    boomSound1 = game.getAssMan().getUserSettingSound(SoundRegistry.BOOM_1);
    dingSound1 = game.getAssMan().getUserSettingSound(SoundRegistry.DING_1);
  }

  @Override
  public void handleInput(final float delta) {

    if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
      boomSound1.play(Constants.DEFAULT_SFX_VOLUME);
      joinGameDataBuilder.playerClassUISelection(playerClassSelection.getSelectedOption());
      removeAllEntities();
      getGame().setScreen(new ConnectServerScreen(getGame(), joinGameDataBuilder.build()));
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
      removeAllEntities();
      getGame().setScreen(new MainMenuScreen(getGame()));
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
      playerClassSelection.next();
      dingSound1.play(Constants.DEFAULT_SFX_VOLUME);
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
      playerClassSelection.prev();
      dingSound1.play(Constants.DEFAULT_SFX_VOLUME);
    }
  }

  @Override
  public void render(final float delta) {
    super.render(delta);
    getGame().getBatch().begin();

    GlyphLayout glyphLayoutEnterYourClass = new GlyphLayout(guiFont64, SELECT_CLASS);
    guiFont64.draw(getGame().getBatch(), SELECT_CLASS,
        getViewport().getWorldWidth() / 2f - glyphLayoutEnterYourClass.width / 2f,
        getViewport().getWorldHeight() / 2f - glyphLayoutEnterYourClass.height / 2f
            - Constants.LOGO_INDENT + 64);

    playerClassSelection.render(guiFont64, this, Constants.LOGO_INDENT);
    getGame().getBatch().end();
  }

}
