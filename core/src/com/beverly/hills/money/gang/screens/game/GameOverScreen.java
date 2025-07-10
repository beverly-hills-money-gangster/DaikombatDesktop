package com.beverly.hills.money.gang.screens.game;

import static com.beverly.hills.money.gang.Constants.DEFAULT_MUSIC_VOLUME;
import static com.beverly.hills.money.gang.Constants.PRESS_TAB_TO_SEE_LEADERBOARD;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.beverly.hills.money.gang.Constants;
import com.beverly.hills.money.gang.DaiKombatGame;
import com.beverly.hills.money.gang.assets.managers.registry.FontRegistry;
import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import com.beverly.hills.money.gang.assets.managers.sound.UserSettingSound;
import com.beverly.hills.money.gang.entities.enemies.EnemyTextures;
import com.beverly.hills.money.gang.entities.ui.LeaderBoardDataLayer;
import com.beverly.hills.money.gang.screens.loading.ConnectServerScreen;
import com.beverly.hills.money.gang.screens.menu.MainMenuScreen;
import com.beverly.hills.money.gang.screens.data.CompleteJoinGameData;
import com.beverly.hills.money.gang.screens.menu.AbstractMainMenuScreen;
import com.beverly.hills.money.gang.screens.ui.selection.GameOverUISelection;
import com.beverly.hills.money.gang.screens.ui.selection.UISelection;
import com.beverly.hills.money.gang.screens.ui.skin.SkinSelectAnimation;

// TODO make shooting sounds change pitch while shooting
public class GameOverScreen extends AbstractMainMenuScreen {

  private final BitmapFont guiFont64;
  private final BitmapFont guiFont32;
  private final UserSettingSound boomSound1;
  private final UserSettingSound dingSound1;
  private final UserSettingSound youWinMusic;
  private final GlyphLayout glyphLayoutWinner;
  private final LeaderBoardDataLayer uiLeaderBoard;
  private boolean showLeaderBoard;
  private final String leaderMessage;
  private final CompleteJoinGameData completeJoinGameData;

  private final SkinSelectAnimation winnerSkinSelectAnimation;
  private final UISelection<GameOverUISelection> menuSelection = new UISelection<>(
      GameOverUISelection.values());

  public GameOverScreen(final DaiKombatGame game,
      final LeaderBoardDataLayer uiLeaderBoard,
      final CompleteJoinGameData completeJoinGameData) {

    super(game);
    guiFont64 = game.getAssMan().getFont(FontRegistry.FONT_64);
    guiFont32 = game.getAssMan().getFont(FontRegistry.FONT_32);
    boomSound1 = game.getAssMan().getUserSettingSound(SoundRegistry.BOOM_1);
    dingSound1 = game.getAssMan().getUserSettingSound(SoundRegistry.DING_1);
    youWinMusic = game.getAssMan().getUserSettingSound(SoundRegistry.WIN_MUSIC);
    this.completeJoinGameData = completeJoinGameData;
    this.uiLeaderBoard = uiLeaderBoard;
    this.showLogo = false;
    if (uiLeaderBoard.getMyPlace() == 1) {
      stopBgMusic();
      youWinMusic.play(DEFAULT_MUSIC_VOLUME * 1.2f);
      leaderMessage = "YOU WIN | " + uiLeaderBoard.getFirstPlaceStats();
    } else {
      leaderMessage = "WINNER IS " + uiLeaderBoard.getFirstPlaceString();
    }
    glyphLayoutWinner = new GlyphLayout(guiFont64, leaderMessage);
    var winner = uiLeaderBoard.getTopPlayer().get();
    winnerSkinSelectAnimation = new SkinSelectAnimation(new EnemyTextures(game.getAssMan(),
        winner.getPlayerClass(), winner.getSkinUISelection()), this);
  }

  @Override
  public void handleInput(final float delta) {
    showLeaderBoard = false;
    if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
      boomSound1.play(Constants.DEFAULT_SFX_VOLUME);
      switch (menuSelection.getSelectedOption()) {
        case PLAY -> {
          removeAllEntities();
          getGame().setScreen(new ConnectServerScreen(getGame(), completeJoinGameData));
        }
        case QUIT -> {
          removeAllEntities();
          getGame().setScreen(new MainMenuScreen(getGame()));
        }
        default -> Gdx.app.exit();

      }
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
      removeAllEntities();
      getGame().setScreen(new MainMenuScreen(getGame()));
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
      menuSelection.next();
      dingSound1.play(Constants.DEFAULT_SFX_VOLUME);
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
      menuSelection.prev();
      dingSound1.play(Constants.DEFAULT_SFX_VOLUME);
    } else if (Gdx.input.isKeyPressed(Keys.TAB)) {
      showLeaderBoard = true;
    }
  }


  @Override
  public void render(final float delta) {
    super.render(delta);
    getGame().getBatch().begin();

    if (showLeaderBoard) {
      String leaderBoard = uiLeaderBoard.toString();
      var glyphLayoutRecSentMessages = new GlyphLayout(guiFont64, leaderBoard);
      guiFont64.draw(getGame().getBatch(),
          leaderBoard, getViewport().getWorldWidth() / 2f - glyphLayoutRecSentMessages.width / 2f,
          getViewport().getWorldHeight() - 128);
    } else {
      winnerSkinSelectAnimation.render();
      drawBlinking(guiFont32, bitmapFont -> {
        var glyphLayoutLeaderBoardHint = new GlyphLayout(bitmapFont, PRESS_TAB_TO_SEE_LEADERBOARD);
        bitmapFont.draw(getGame().getBatch(), PRESS_TAB_TO_SEE_LEADERBOARD,
            getViewport().getWorldWidth() / 2f - glyphLayoutLeaderBoardHint.width / 2f,
            getViewport().getWorldHeight() / 2f - glyphLayoutLeaderBoardHint.height / 2f
                - Constants.LOGO_INDENT + 64);
      });
      guiFont64.draw(getGame().getBatch(), leaderMessage,
          getViewport().getWorldWidth() / 2f - glyphLayoutWinner.width / 2f,
          getViewport().getWorldHeight() / 2f - glyphLayoutWinner.height / 2f
              - Constants.LOGO_INDENT + 64 - 32);
      menuSelection.render(guiFont64, this, Constants.LOGO_INDENT + 64);
    }
    getGame().getBatch().end();
  }

  @Override
  public void onExitScreen() {
    super.onExitScreen();
    youWinMusic.stop();
  }

}
