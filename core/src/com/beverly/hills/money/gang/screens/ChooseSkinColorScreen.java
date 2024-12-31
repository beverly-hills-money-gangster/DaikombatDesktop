package com.beverly.hills.money.gang.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.beverly.hills.money.gang.Constants;
import com.beverly.hills.money.gang.DaiKombatGame;
import com.beverly.hills.money.gang.assets.managers.registry.FontRegistry;
import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import com.beverly.hills.money.gang.assets.managers.sound.UserSettingSound;
import com.beverly.hills.money.gang.entities.enemies.EnemyTextures;
import com.beverly.hills.money.gang.screens.data.ConnectGameData;
import com.beverly.hills.money.gang.screens.ui.selection.PlayerClassUISelection;
import com.beverly.hills.money.gang.screens.ui.selection.SkinUISelection;
import com.beverly.hills.money.gang.screens.ui.selection.UISelection;
import com.beverly.hills.money.gang.screens.ui.skin.SkinSelectAnimation;

public class ChooseSkinColorScreen extends AbstractMainMenuScreen {

  private final BitmapFont guiFont64;
  private final UserSettingSound boomSound1;
  private final UserSettingSound dingSound1;

  private final SkinSelectAnimation skinSelectAnimation;

  private final PlayerClassUISelection playerClassUISelection;
  private final UISelection<SkinUISelection> skinSelection = new UISelection<>(
      SkinUISelection.values());

  private final ConnectGameData.ConnectGameDataBuilder joinGameDataBuilder;

  public ChooseSkinColorScreen(final DaiKombatGame game,
      final ConnectGameData.ConnectGameDataBuilder joinGameDataBuilder,
      final PlayerClassUISelection playerClassUISelection) {
    super(game);
    this.playerClassUISelection = playerClassUISelection;
    guiFont64 = game.getAssMan().getFont(FontRegistry.FONT_64);
    this.joinGameDataBuilder = joinGameDataBuilder;
    boomSound1 = game.getAssMan().getUserSettingSound(SoundRegistry.BOOM_1);
    dingSound1 = game.getAssMan().getUserSettingSound(SoundRegistry.DING_1);
    showLogo = false;
    skinSelectAnimation = new SkinSelectAnimation(new EnemyTextures(game.getAssMan(), playerClassUISelection,
        SkinUISelection.GREEN), this);
  }

  @Override
  public void handleInput(final float delta) {

    if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
      boomSound1.play(Constants.DEFAULT_SFX_VOLUME);
      joinGameDataBuilder.skinUISelection(skinSelection.getSelectedOption());
      removeAllEntities();
      getGame().setScreen(new ConnectServerScreen(getGame(), joinGameDataBuilder.build()));
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
      removeAllEntities();
      getGame().setScreen(new MainMenuScreen(getGame()));
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
      skinSelection.next();
      dingSound1.play(Constants.DEFAULT_SFX_VOLUME);
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
      skinSelection.prev();
      dingSound1.play(Constants.DEFAULT_SFX_VOLUME);
    }
    skinSelectAnimation.setEnemyTextures(
        new EnemyTextures(getGame().getAssMan(),
            playerClassUISelection, skinSelection.getSelectedOption()));
  }

  @Override
  public void render(final float delta) {
    super.render(delta);
    getGame().getBatch().begin();
    skinSelection.render(guiFont64, this, Constants.LOGO_INDENT - 64);
    skinSelectAnimation.render();
    getGame().getBatch().end();
  }

}
