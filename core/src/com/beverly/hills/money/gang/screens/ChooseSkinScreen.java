package com.beverly.hills.money.gang.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.beverly.hills.money.gang.Constants;
import com.beverly.hills.money.gang.DaiKombatGame;
import com.beverly.hills.money.gang.assets.managers.registry.FontRegistry;
import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import com.beverly.hills.money.gang.assets.managers.registry.TexturesRegistry;
import com.beverly.hills.money.gang.assets.managers.sound.UserSettingSound;
import com.beverly.hills.money.gang.entities.enemies.EnemyTextures;
import com.beverly.hills.money.gang.screens.data.ConnectGameData;
import com.beverly.hills.money.gang.screens.ui.selection.SkinUISelection;
import com.beverly.hills.money.gang.screens.ui.selection.UISelection;

public class ChooseSkinScreen extends AbstractMainMenuScreen {

  private static final int STEP_ANIMATION_DELAY_MLS = 150;
  private final BitmapFont guiFont64;
  private final UserSettingSound boomSound1;
  private final UserSettingSound dingSound1;

  private int skinStep;
  private long stepUntilMls;

  private EnemyTextures enemyTextures;
  private final UISelection<SkinUISelection> skinSelection = new UISelection<>(
      SkinUISelection.values());

  private final ConnectGameData.ConnectGameDataBuilder joinGameDataBuilder;

  public ChooseSkinScreen(final DaiKombatGame game,
      final ConnectGameData.ConnectGameDataBuilder joinGameDataBuilder) {
    super(game);
    guiFont64 = game.getAssMan().getFont(FontRegistry.FONT_64);
    this.joinGameDataBuilder = joinGameDataBuilder;
    boomSound1 = game.getAssMan().getUserSettingSound(SoundRegistry.BOOM_1);
    dingSound1 = game.getAssMan().getUserSettingSound(SoundRegistry.DING_1);
    showLogo = false;
    enemyTextures = new EnemyTextures(game.getAssMan(),
        TexturesRegistry.ENEMY_PLAYER_SPRITES_GREEN);
  }

  @Override
  public void handleInput(final float delta) {

    if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
      boomSound1.play(Constants.DEFAULT_SFX_VOLUME);
      joinGameDataBuilder.skinUISelection(skinSelection.getSelectedOption());
      removeAllEntities();
      getGame().setScreen(new ChoosePlayerClassScreen(getGame(), joinGameDataBuilder));
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
    TexturesRegistry selectedTexture = switch (skinSelection.getSelectedOption()) {
      case GREEN -> TexturesRegistry.ENEMY_PLAYER_SPRITES_GREEN;
      case BLUE -> TexturesRegistry.ENEMY_PLAYER_SPRITES_BLUE;
      case YELLOW -> TexturesRegistry.ENEMY_PLAYER_SPRITES_YELLOW;
      case ORANGE -> TexturesRegistry.ENEMY_PLAYER_SPRITES_ORANGE;
      case PURPLE -> TexturesRegistry.ENEMY_PLAYER_SPRITES_PURPLE;
      case PINK -> TexturesRegistry.ENEMY_PLAYER_SPRITES_PINK;
    };
    enemyTextures = new EnemyTextures(getGame().getAssMan(), selectedTexture);
  }

  @Override
  public void render(final float delta) {
    super.render(delta);
    getGame().getBatch().begin();
    var texture = enemyTextures.getEnemyPlayerMoveFrontTextureRegion(skinStep);
    float scale = (getViewport().getWorldWidth() / 5f) / texture.getRegionWidth();
    getGame().getBatch().draw(texture,
        getViewport().getWorldWidth() / 2 - (texture.getRegionWidth() * scale) / 2f,
        getViewport().getWorldHeight() / 2.5f,
        texture.getRegionWidth() * scale, texture.getRegionHeight() * scale);

    skinSelection.render(guiFont64, this, Constants.LOGO_INDENT - 64);
    getGame().getBatch().end();
    if (System.currentTimeMillis() > stepUntilMls) {
      stepUntilMls = System.currentTimeMillis() + STEP_ANIMATION_DELAY_MLS;
      skinStep = (skinStep + 1) % 3;
    }
  }

}
