package com.beverly.hills.money.gang.screens.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.beverly.hills.money.gang.Constants;
import com.beverly.hills.money.gang.DaiKombatGame;
import com.beverly.hills.money.gang.assets.managers.registry.FontRegistry;
import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import com.beverly.hills.money.gang.assets.managers.sound.UserSettingSound;
import com.beverly.hills.money.gang.entities.enemies.EnemyTextures;
import com.beverly.hills.money.gang.screens.data.ConnectServerData;
import com.beverly.hills.money.gang.screens.data.JoinGameData;
import com.beverly.hills.money.gang.screens.ui.selection.GamePlayerClass;
import com.beverly.hills.money.gang.screens.ui.selection.SkinUISelection;
import com.beverly.hills.money.gang.screens.ui.selection.UISelection;
import com.beverly.hills.money.gang.screens.ui.skin.SkinSelectAnimation;

public class ChooseSkinColorScreen extends AbstractMainMenuScreen {

  private final BitmapFont guiFont64;
  private final UserSettingSound boomSound1;
  private final UserSettingSound dingSound1;

  private static final String SELECT_COLOR = "SELECT YOUR COLOR";


  private final SkinSelectAnimation skinSelectAnimation;

  private final GamePlayerClass gamePlayerClass;
  private final UISelection<SkinUISelection> skinSelection = new UISelection<>(
      SkinUISelection.values());

  private final JoinGameData.JoinGameDataBuilder joinGameDataBuilder;

  private final ConnectServerData connectServerData;

  public ChooseSkinColorScreen(final DaiKombatGame game,
      final JoinGameData.JoinGameDataBuilder joinGameDataBuilder,
      final GamePlayerClass gamePlayerClass,
      final ConnectServerData connectServerData) {
    super(game);
    this.gamePlayerClass = gamePlayerClass;
    this.connectServerData = connectServerData;
    guiFont64 = game.getAssMan().getFont(FontRegistry.FONT_64);
    this.joinGameDataBuilder = joinGameDataBuilder;
    boomSound1 = game.getAssMan().getUserSettingSound(SoundRegistry.BOOM_1);
    dingSound1 = game.getAssMan().getUserSettingSound(SoundRegistry.DING_1);
    showLogo = false;
    skinSelectAnimation = new SkinSelectAnimation(new EnemyTextures(game.getAssMan(),
        gamePlayerClass,
        SkinUISelection.GREEN), this);
  }

  @Override
  public void handleInput(final float delta) {

    if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
      boomSound1.play(Constants.DEFAULT_SFX_VOLUME);
      joinGameDataBuilder.skinUISelection(skinSelection.getSelectedOption());
      removeAllEntities();
      getGame().setScreen(
          new GetGameRoomsScreen(getGame(), joinGameDataBuilder.build(), connectServerData));
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
            gamePlayerClass, skinSelection.getSelectedOption()));
  }

  @Override
  public void render(final float delta) {
    super.render(delta);
    getGame().getBatch().begin();
    GlyphLayout glyphSelectColor = new GlyphLayout(guiFont64, SELECT_COLOR);
    guiFont64.draw(getGame().getBatch(), SELECT_COLOR,
        getViewport().getWorldWidth() / 2f - glyphSelectColor.width / 2f,
        skinSelectAnimation.getAnimationYOffset() - glyphSelectColor.height / 2f);

    skinSelection.render(guiFont64, this,
        skinSelectAnimation.getAnimationYOffset() - 64 - 16);
    skinSelectAnimation.render();
    getGame().getBatch().end();
  }

}
