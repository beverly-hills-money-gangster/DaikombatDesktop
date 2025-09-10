package com.beverly.hills.money.gang.screens.game;

import static com.beverly.hills.money.gang.configs.Constants.DEFAULT_MUSIC_VOLUME;
import static com.beverly.hills.money.gang.configs.Constants.PRESS_TO_SEE_LEADERBOARD;
import static com.beverly.hills.money.gang.configs.Constants.PRESS_TO_CHAT;
import static com.beverly.hills.money.gang.configs.Constants.PRESS_TO_TALK;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.beverly.hills.money.gang.configs.Constants;
import com.beverly.hills.money.gang.DaiKombatGame;
import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import com.beverly.hills.money.gang.assets.managers.sound.UserSettingSound;
import com.beverly.hills.money.gang.configs.KeyMappings;
import com.beverly.hills.money.gang.entities.enemies.EnemyTextures;
import com.beverly.hills.money.gang.entities.ui.LeaderBoardDataLayer;
import com.beverly.hills.money.gang.network.GlobalGameConnection;
import com.beverly.hills.money.gang.proto.ServerResponse;
import com.beverly.hills.money.gang.proto.ServerResponse.GameEvent.GameEventType;
import com.beverly.hills.money.gang.proto.ServerResponse.GameEvents;
import com.beverly.hills.money.gang.screens.ChatBox;
import com.beverly.hills.money.gang.screens.GameScreen;
import com.beverly.hills.money.gang.screens.data.CompleteJoinGameData;
import com.beverly.hills.money.gang.screens.data.GameBootstrapData;
import com.beverly.hills.money.gang.screens.loading.ConnectServerScreen;
import com.beverly.hills.money.gang.screens.menu.AbstractMainMenuScreen;
import com.beverly.hills.money.gang.screens.menu.ErrorScreen;
import com.beverly.hills.money.gang.screens.menu.MainMenuScreen;
import com.beverly.hills.money.gang.screens.ui.audio.VoiceChatPlayer;
import com.beverly.hills.money.gang.screens.ui.selection.GameOverUISelection;
import com.beverly.hills.money.gang.screens.ui.selection.UISelection;
import com.beverly.hills.money.gang.screens.ui.skin.SkinSelectAnimation;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO make shooting sounds change pitch while shooting
public class GameOverScreen extends AbstractMainMenuScreen {


  private static final Logger LOG = LoggerFactory.getLogger(GameOverScreen.class);

  private final UserSettingSound boomSound1;
  private final UserSettingSound dingSound1;
  private final UserSettingSound youWinMusic;
  private final GlyphLayout glyphLayoutWinner;
  private final LeaderBoardDataLayer uiLeaderBoard;
  private boolean showLeaderBoard;
  private final String leaderMessage;
  private final CompleteJoinGameData completeJoinGameData;

  private final GlobalGameConnection gameConnection;

  private final ChatBox chatBox;

  private final SkinSelectAnimation winnerSkinSelectAnimation;
  private final UISelection<GameOverUISelection> menuSelection = new UISelection<>(
      GameOverUISelection.values());

  private final GameBootstrapData gameBootstrapData;

  private final VoiceChatPlayer voiceChatPlayer;

  private int playersOnline;


  public GameOverScreen(final DaiKombatGame game,
      final LeaderBoardDataLayer uiLeaderBoard,
      final GameBootstrapData gameBootstrapData,
      final GlobalGameConnection gameConnection) {
    super(game);
    this.playersOnline = uiLeaderBoard.size();
    this.gameConnection = gameConnection;
    this.gameBootstrapData = gameBootstrapData;
    boomSound1 = game.getAssMan().getUserSettingSound(SoundRegistry.BOOM_1);
    dingSound1 = game.getAssMan().getUserSettingSound(SoundRegistry.DING_1);
    youWinMusic = game.getAssMan().getUserSettingSound(SoundRegistry.WIN_MUSIC);
    this.completeJoinGameData = gameBootstrapData.getCompleteJoinGameData();
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
    this.voiceChatPlayer = new VoiceChatPlayer(gameConnection,
        gameBootstrapData.getPlayerId(),
        gameBootstrapData.getCompleteJoinGameData().getGameRoomId(), this,
        payload -> {
          // do nothing
        });
    this.chatBox = new ChatBox(gameBootstrapData, gameConnection, this);
    this.chatBox.greetPlayers(uiLeaderBoard.size());
  }

  @Override
  public void show() {
    voiceChatPlayer.init();
  }

  @Override
  public void handleInput(final float delta) {
    showLeaderBoard = false;
    if (Gdx.input.isKeyPressed(KeyMappings.LEADERBOARD.getKey())) {
      showLeaderBoard = true;
      return;
    } else if (chatBox.handleChatInput()) {
      return;
    }
    voiceChatPlayer.handleInput();
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
    }
  }


  @Override
  public void render(final float delta) {
    super.render(delta);
    List<String> hints = new ArrayList<>();
    getGame().getBatch().begin();
    handleConnection();
    renderGameTechStats(playersOnline, gameConnection);
    if (showLeaderBoard) {
      String leaderBoard = uiLeaderBoard.toString();
      var glyphLayoutRecSentMessages = new GlyphLayout(guiFont64, leaderBoard);
      guiFont64.draw(getGame().getBatch(),
          leaderBoard, getViewport().getWorldWidth() / 2f - glyphLayoutRecSentMessages.width / 2f,
          getViewport().getWorldHeight() - 128);
    } else {
      hints.add(PRESS_TO_SEE_LEADERBOARD);
      chatBox.renderChat();
      winnerSkinSelectAnimation.render();
      if (!chatBox.isChatMode()) {
        hints.add(PRESS_TO_CHAT);
        if (voiceChatPlayer.isVoiceChatMode()) {
          voiceChatPlayer.renderGui();
        } else {
          hints.add(PRESS_TO_TALK);
        }
      }

      renderHints(hints);

      guiFont64.draw(getGame().getBatch(), leaderMessage,
          getViewport().getWorldWidth() / 2f - glyphLayoutWinner.width / 2f,
          winnerSkinSelectAnimation.getAnimationYOffset() - 32);

      menuSelection.render(guiFont64, this, winnerSkinSelectAnimation.getAnimationYOffset() - 64);
    }
    getGame().getBatch().end();
  }

  private void handleConnection() {
    gameConnection.pollResponses().forEach(serverResponse -> {
      LOG.info("Got response {}", serverResponse);
      if (serverResponse.hasChatEvents()) {
        var chatEvent = serverResponse.getChatEvents();
        chatBox.getChatLog().addMessage(chatEvent.getName(), chatEvent.getMessage());
      } else if (serverResponse.hasErrorEvent()) {
        handleException(new RuntimeException(serverResponse.getChatEvents().getMessage()));
      } else if (serverResponse.hasGameEvents()) {
        var gameEvents = serverResponse.getGameEvents();
        Optional.of(gameEvents).filter(GameEvents::hasPlayersOnline).ifPresent(
            events -> playersOnline = events.getPlayersOnline());
        gameEvents.getEventsList().stream().filter(
                gameEvent -> gameEvent.getEventType() == GameEventType.EXIT)
            .forEach(this::handleExit);
      }
    });
    gameConnection.pollErrors().forEach(this::handleException);
  }

  @Override
  public void onExitScreen(GameScreen gameScreen) {
    super.onExitScreen(gameScreen);
    youWinMusic.stop();
    closeGameConnectionResources();
    voiceChatPlayer.stop();
  }

  @Override
  public void hide() {
    super.hide();
    closeGameConnectionResources();
  }

  private void closeGameConnectionResources() {
    gameConnection.disconnect();
  }

  public void handleException(Throwable error) {
    LOG.error("Got error", error);
    gameConnection.disconnect();
    removeAllEntities();
    getGame().setScreen(
        new ErrorScreen(getGame(), ExceptionUtils.getRootCause(error).getMessage()));
  }

  @Override
  public void dispose() {
    gameConnection.disconnect();
    voiceChatPlayer.stop();
  }

  private void handleExit(ServerResponse.GameEvent gameEvent) {
    if (gameEvent.getPlayer().getPlayerId() == gameBootstrapData.getPlayerId()) {
      return;
    }
    var playerName =
        gameEvent.getPlayer().hasPlayerName() ? gameEvent.getPlayer().getPlayerName() : null;
    Optional.ofNullable(playerName).ifPresent(
        name -> chatBox.getChatLog().addChatLog(name + " has left the game"));
  }
}
