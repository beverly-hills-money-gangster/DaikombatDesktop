package com.beverly.hills.money.gang.screens.loading;

import com.beverly.hills.money.gang.Constants;
import com.beverly.hills.money.gang.DaiKombatGame;
import com.beverly.hills.money.gang.config.ClientConfig;
import com.beverly.hills.money.gang.network.GlobalGameConnection;
import com.beverly.hills.money.gang.proto.JoinGameCommand;
import com.beverly.hills.money.gang.proto.MergeConnectionCommand;
import com.beverly.hills.money.gang.proto.PlayerClass;
import com.beverly.hills.money.gang.proto.PlayerSkinColor;
import com.beverly.hills.money.gang.proto.ServerResponse;
import com.beverly.hills.money.gang.screens.data.CompleteJoinGameData;
import com.beverly.hills.money.gang.screens.data.GameBootstrapData;
import com.beverly.hills.money.gang.screens.game.PlayScreen;
import com.beverly.hills.money.gang.screens.ui.selection.GamePlayerClass;
import com.beverly.hills.money.gang.screens.ui.selection.SkinUISelection;
import com.beverly.hills.money.gang.utils.Converter;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JoinGameScreen extends ReconnectableScreen {

  private static final Logger LOG = LoggerFactory.getLogger(JoinGameScreen.class);
  private final GlobalGameConnection gameConnection;
  private final GameBootstrapData.GameBootstrapDataBuilder playerContextDataBuilder;
  private final CompleteJoinGameData completeJoinGameData;

  private final AtomicReference<String> errorMessage = new AtomicReference<>();

  public JoinGameScreen(final DaiKombatGame game,
      final GameBootstrapData.GameBootstrapDataBuilder playerContextDataBuilder,
      final CompleteJoinGameData completeJoinGameData,
      final GlobalGameConnection gameConnection,
      final int connectionTrial) {
    super(game, connectionTrial);
    this.gameConnection = gameConnection;
    this.completeJoinGameData = completeJoinGameData;
    this.playerContextDataBuilder = playerContextDataBuilder;
  }


  @Override
  public void show() {
    if (gameConnection.isAnyDisconnected()) {
      errorMessage.set("Connection lost");
      return;
    }
    var joinGameRequestBuilder = JoinGameCommand.newBuilder()
        .setVersion(ClientConfig.VERSION)
        .setGameId(completeJoinGameData.getGameRoomId())
        .setSkin(
            createSkinColorSelection(completeJoinGameData.getJoinGameData().getSkinUISelection()))
        .setPlayerClass(
            createPlayerClass(completeJoinGameData.getJoinGameData().getGamePlayerClass()))
        .setPlayerName(completeJoinGameData.getJoinGameData().getPlayerName());
    Optional.ofNullable(completeJoinGameData.getJoinGameData().getPlayerIdToRecover())
        .ifPresent(joinGameRequestBuilder::setRecoveryPlayerId);
    gameConnection.write(joinGameRequestBuilder
        .build());
  }

  private PlayerSkinColor createSkinColorSelection(SkinUISelection skinUISelection) {
    return switch (skinUISelection) {
      case BLUE -> PlayerSkinColor.BLUE;
      case YELLOW -> PlayerSkinColor.YELLOW;
      case ORANGE -> PlayerSkinColor.ORANGE;
      case GREEN -> PlayerSkinColor.GREEN;
      case PINK -> PlayerSkinColor.PINK;
      case PURPLE -> PlayerSkinColor.PURPLE;
    };
  }

  public static PlayerClass createPlayerClass(GamePlayerClass gamePlayerClass) {
    return switch (gamePlayerClass) {
      case WARRIOR -> PlayerClass.WARRIOR;
      case ANGRY_SKELETON -> PlayerClass.ANGRY_SKELETON;
      case DEMON_TANK -> PlayerClass.DEMON_TANK;
    };
  }


  @Override
  protected void onEscape() {
    gameConnection.disconnect();
  }


  @Override
  protected void onLoadingRenderInternal(final float delta) {
    if (StringUtils.isNotBlank(errorMessage.get())) {
      reconnect(errorMessage.get(), gameConnection, completeJoinGameData);
      return;
    }
    gameConnection.pollPrimaryConnectionResponse().ifPresent(response -> {
      if (response.hasErrorEvent()) {
        LOG.error("Error {}", errorMessage);
        errorMessage.set(response.getErrorEvent().getMessage());
      } else if (response.hasGameOver()) {
        LOG.info("Game is over. Try to reconnect");
        errorMessage.set("Can't connect. Game is over.");
      } else if (response.hasGameEvents()) {
        removeAllEntities();
        stopBgMusic();
        LOG.info("Joined the game. Go play");
        var playerContextData = createPlayerContextData(response);
        gameConnection.initVoiceChat(playerContextData.getPlayerId(),
            completeJoinGameData.getGameRoomId());
        getGame().setScreen(
            new PlayScreen(getGame(), gameConnection, playerContextData));
      }
    });

    gameConnection.pollErrors().stream().findAny().ifPresent(throwable -> {
      LOG.error("Error while loading", throwable);
      gameConnection.disconnect();
      errorMessage.set(ExceptionUtils.getMessage(throwable));
    });
  }

  private GameBootstrapData createPlayerContextData(ServerResponse response) {
    var mySpawnEvent = response.getGameEvents().getEvents(0);
    int playerId = mySpawnEvent.getPlayer().getPlayerId();
    gameConnection.getSecondaryGameConnections().forEach(
        secondaryGameConnection -> secondaryGameConnection.write(
            MergeConnectionCommand.newBuilder().setGameId(completeJoinGameData.getGameRoomId())
                .setPlayerId(playerId).build()));
    return playerContextDataBuilder
        .playerId(playerId)
        .playersOnline(response.getGameEvents().getPlayersOnline())
        .completeJoinGameData(completeJoinGameData)
        .spawn(Converter.convertToVector2(mySpawnEvent.getPlayer().getPosition()))
        .direction(Converter.convertToVector2(mySpawnEvent.getPlayer().getDirection()))
        .leaderBoardItemList(mySpawnEvent.getLeaderBoard().getItemsList())
        .build();
  }

  @Override
  protected void onTimeout() {
    gameConnection.disconnect();
  }

  @Override
  protected String getBaseLoadingMessage() {
    return Constants.GAME_JOIN;
  }

}
