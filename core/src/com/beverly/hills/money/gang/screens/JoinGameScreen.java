package com.beverly.hills.money.gang.screens;

import com.beverly.hills.money.gang.Configs;
import com.beverly.hills.money.gang.DaiKombatGame;
import com.beverly.hills.money.gang.config.ClientConfig;
import com.beverly.hills.money.gang.network.LoadBalancedGameConnection;
import com.beverly.hills.money.gang.proto.JoinGameCommand;
import com.beverly.hills.money.gang.proto.MergeConnectionCommand;
import com.beverly.hills.money.gang.proto.ServerResponse;
import com.beverly.hills.money.gang.proto.SkinColorSelection;
import com.beverly.hills.money.gang.screens.data.ConnectGameData;
import com.beverly.hills.money.gang.screens.data.PlayerConnectionContextData;
import com.beverly.hills.money.gang.screens.ui.selection.SkinUISelection;
import com.beverly.hills.money.gang.utils.Converter;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO test it
public class JoinGameScreen extends ReconnectableScreen {

  private static final Logger LOG = LoggerFactory.getLogger(JoinGameScreen.class);
  private String errorMessage;
  private final LoadBalancedGameConnection gameConnection;
  private final PlayerConnectionContextData.PlayerConnectionContextDataBuilder playerContextDataBuilder;
  private final ConnectGameData connectGameData;

  private final int connectionTrial;

  public JoinGameScreen(final DaiKombatGame game,
      final PlayerConnectionContextData.PlayerConnectionContextDataBuilder playerContextDataBuilder,
      final ConnectGameData connectGameData,
      final LoadBalancedGameConnection gameConnection,
      final int connectionTrial) {
    super(game);
    this.gameConnection = gameConnection;
    this.connectGameData = connectGameData;
    this.playerContextDataBuilder = playerContextDataBuilder;
    this.connectionTrial = connectionTrial;
  }

  @Override
  public void show() {
    if (gameConnection.isAnyDisconnected()) {
      errorMessage = "Connection lost";
      return;
    }
    var joinGameRequestBuilder = JoinGameCommand.newBuilder()
        .setVersion(ClientConfig.VERSION)
        .setGameId(Configs.GAME_ID)
        .setSkin(creatSkinColorSelection(connectGameData.getSkinUISelection()))
        .setPlayerName(connectGameData.getPlayerName());
    Optional.ofNullable(connectGameData.getPlayerIdToRecover())
        .ifPresent(joinGameRequestBuilder::setRecoveryPlayerId);
    gameConnection.write(joinGameRequestBuilder
        .build());

  }

  private SkinColorSelection creatSkinColorSelection(SkinUISelection skinUISelection) {
    return switch (skinUISelection) {
      case BLUE -> SkinColorSelection.BLUE;
      case YELLOW -> SkinColorSelection.YELLOW;
      case ORANGE -> SkinColorSelection.ORANGE;
      case GREEN -> SkinColorSelection.GREEN;
      case PINK -> SkinColorSelection.PINK;
      case PURPLE -> SkinColorSelection.PURPLE;
    };
  }


  @Override
  protected void onEscape() {
    gameConnection.disconnect();
  }


  @Override
  protected void onLoadingRender(final float delta) {
    if (!isTimeToConnect(connectionTrial)) {
      return;
    }
    if (errorMessage != null || gameConnection.isAnyDisconnected()) {
      reconnectOnError(Objects.toString(errorMessage, "can't connect"),
          connectionTrial, gameConnection, connectGameData);
      return;
    }
    gameConnection.pollPrimaryConnectionResponse().ifPresent(response -> {
      if (response.hasErrorEvent()) {
        LOG.error("Error {}", errorMessage);
        errorMessage = response.getErrorEvent().getMessage();
      } else if (response.hasGameOver()) {
        LOG.info("Game is over. Try to reconnect");
        reconnectOnError("Game over", connectionTrial, gameConnection, connectGameData);
      } else if (response.hasGameEvents()) {
        removeAllEntities();
        stopBgMusic();
        LOG.info("Joined the game. Go play");
        getGame().setScreen(
            new PlayScreen(getGame(), gameConnection, createPlayerContextData(response)));
      }
    });

    gameConnection.pollErrors().stream().findAny().ifPresent(throwable -> {
      LOG.error("Error while loading", throwable);
      gameConnection.disconnect();
      errorMessage = ExceptionUtils.getMessage(throwable);
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
