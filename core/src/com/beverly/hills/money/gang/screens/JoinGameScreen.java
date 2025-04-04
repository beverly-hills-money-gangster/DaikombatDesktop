package com.beverly.hills.money.gang.screens;

import com.beverly.hills.money.gang.Configs;
import com.beverly.hills.money.gang.DaiKombatGame;
import com.beverly.hills.money.gang.config.ClientConfig;
import com.beverly.hills.money.gang.network.GlobalGameConnection;
import com.beverly.hills.money.gang.proto.JoinGameCommand;
import com.beverly.hills.money.gang.proto.MergeConnectionCommand;
import com.beverly.hills.money.gang.proto.PlayerClass;
import com.beverly.hills.money.gang.proto.PlayerSkinColor;
import com.beverly.hills.money.gang.proto.ServerResponse;
import com.beverly.hills.money.gang.screens.data.ConnectGameData;
import com.beverly.hills.money.gang.screens.data.PlayerConnectionContextData;
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
  private final PlayerConnectionContextData.PlayerConnectionContextDataBuilder playerContextDataBuilder;
  private final ConnectGameData connectGameData;

  private final AtomicReference<String> errorMessage = new AtomicReference<>();

  public JoinGameScreen(final DaiKombatGame game,
      final PlayerConnectionContextData.PlayerConnectionContextDataBuilder playerContextDataBuilder,
      final ConnectGameData connectGameData,
      final GlobalGameConnection gameConnection,
      final int connectionTrial) {
    super(game, connectionTrial);
    this.gameConnection = gameConnection;
    this.connectGameData = connectGameData;
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
        .setGameId(Configs.GAME_ID)
        .setSkin(createSkinColorSelection(connectGameData.getSkinUISelection()))
        .setPlayerClass(createPlayerClass(connectGameData.getGamePlayerClass()))
        .setPlayerName(connectGameData.getPlayerName());
    Optional.ofNullable(connectGameData.getPlayerIdToRecover())
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
      reconnect(errorMessage.get(), gameConnection, connectGameData);
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
        gameConnection.initVoiceChat(playerContextData.getPlayerId(), Configs.GAME_ID);
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

  private PlayerConnectionContextData createPlayerContextData(ServerResponse response) {
    var mySpawnEvent = response.getGameEvents().getEvents(0);
    int playerId = mySpawnEvent.getPlayer().getPlayerId();
    gameConnection.getSecondaryGameConnections().forEach(
        secondaryGameConnection -> secondaryGameConnection.write(
            MergeConnectionCommand.newBuilder().setGameId(Configs.GAME_ID).setPlayerId(playerId)
                .build()));
    return playerContextDataBuilder
        .playerId(playerId)
        .playersOnline(response.getGameEvents().getPlayersOnline())
        .connectGameData(connectGameData)
        .spawn(Converter.convertToVector2(mySpawnEvent.getPlayer().getPosition()))
        .direction(Converter.convertToVector2(mySpawnEvent.getPlayer().getDirection()))
        .leaderBoardItemList(mySpawnEvent.getLeaderBoard().getItemsList())
        .build();
  }

  @Override
  protected void onTimeout() {
    gameConnection.disconnect();
  }

}
