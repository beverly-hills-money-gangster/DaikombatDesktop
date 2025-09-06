package com.beverly.hills.money.gang.screens.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.beverly.hills.money.gang.DaiKombatGame;
import com.beverly.hills.money.gang.entities.ui.LeaderBoardDataLayer;
import com.beverly.hills.money.gang.entities.ui.LeaderBoardPlayer;
import com.beverly.hills.money.gang.network.GlobalGameConnection;
import com.beverly.hills.money.gang.proto.RespawnCommand;
import com.beverly.hills.money.gang.proto.ServerResponse;
import com.beverly.hills.money.gang.screens.data.GameBootstrapData;
import com.beverly.hills.money.gang.screens.loading.AbstractLoadingScreen;
import com.beverly.hills.money.gang.screens.menu.ErrorScreen;
import com.beverly.hills.money.gang.screens.menu.MainMenuScreen;
import com.beverly.hills.money.gang.utils.Converter;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RespawnScreen extends AbstractLoadingScreen {

  private static final Logger LOG = LoggerFactory.getLogger(RespawnScreen.class);

  private String errorMessage;

  private final GameBootstrapData oldGameBootstrapData;

  private final GlobalGameConnection gameConnection;

  public RespawnScreen(final DaiKombatGame game,
      final GameBootstrapData oldGameBootstrapData,
      final GlobalGameConnection gameConnection) {
    super(game);
    this.oldGameBootstrapData = oldGameBootstrapData;
    this.gameConnection = gameConnection;
  }

  @Override
  public void show() {
    if (gameConnection.isAnyDisconnected()) {
      errorMessage = "Connection lost";
    } else {
      gameConnection.write(RespawnCommand.newBuilder()
          .setGameId(oldGameBootstrapData.getCompleteJoinGameData().getGameRoomId())
          .setPlayerId(oldGameBootstrapData.getPlayerId()).build());
    }
  }

  @Override
  public void handleInput(final float delta) {
    if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
      removeAllEntities();
      gameConnection.disconnect();
      getGame().setScreen(new MainMenuScreen(getGame()));
    }
  }

  @Override
  protected void onLoadingRender(final float delta) {
    if (errorMessage != null) {
      gameConnection.disconnect();
      removeAllEntities();
      getGame().setScreen(new ErrorScreen(getGame(), errorMessage));
      return;
    }

    gameConnection.pollPrimaryConnectionResponse().ifPresent(response -> {
      if (response.hasErrorEvent()) {
        errorMessage = response.getErrorEvent().getMessage();
      } else if (response.hasGameOver()) {
        removeAllEntities();
        stopBgMusic();
        getGame().setScreen(
            new GameOverScreen(getGame(),
                new LeaderBoardDataLayer(oldGameBootstrapData.getPlayerId(),
                    LeaderBoardPlayer.createFromGameOver(response.getGameOver())),
                oldGameBootstrapData, gameConnection));
      } else if (response.hasGameEvents()) {
        var gameEvent = response.getGameEvents().getEvents(0);
        if (gameEvent.getEventType() != ServerResponse.GameEvent.GameEventType.SPAWN
            || gameEvent.getPlayer().getPlayerId()
            != oldGameBootstrapData.getPlayerId()) {
          // not our event
          return;
        }
        removeAllEntities();
        stopBgMusic();
        getGame().setScreen(
            new PlayScreen(getGame(), gameConnection, createPlayerContextData(response)));
      }
    });

    gameConnection.pollErrors().stream().findFirst().ifPresent(throwable -> {
      LOG.error("Error while loading", throwable);
      gameConnection.disconnect();
      errorMessage = ExceptionUtils.getMessage(throwable);
    });
  }

  private GameBootstrapData createPlayerContextData(ServerResponse response) {
    var gameEvent = response.getGameEvents().getEvents(0);
    int playerId = gameEvent.getPlayer().getPlayerId();
    return oldGameBootstrapData.toBuilder()
        .playerId(playerId)
        .playersOnline(response.getGameEvents().getPlayersOnline())
        .spawn(Converter.convertToVector2(gameEvent.getPlayer().getPosition()))
        .direction(Converter.convertToVector2(gameEvent.getPlayer().getDirection()))
        .leaderBoardItemList(gameEvent.getLeaderBoard().getItemsList())
        .build();
  }

  @Override
  public void dispose() {
    gameConnection.disconnect();
  }

  @Override
  protected void onEscape() {
    gameConnection.disconnect();
  }

  @Override
  protected void onTimeout() {
    gameConnection.disconnect();
  }
}
