package com.beverly.hills.money.gang.screens;

import com.beverly.hills.money.gang.DaiKombatGame;
import com.beverly.hills.money.gang.entity.GameServerCreds;
import com.beverly.hills.money.gang.entity.HostPort;
import com.beverly.hills.money.gang.network.GameConnection;
import com.beverly.hills.money.gang.proto.GetServerInfoCommand;
import com.beverly.hills.money.gang.screens.data.JoinGameData;
import com.beverly.hills.money.gang.screens.data.PlayerConnectionContextData;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO test it
public class GetServerInfoScreen extends AbstractLoadingScreen {

  private static final Logger LOG = LoggerFactory.getLogger(GetServerInfoScreen.class);
  private final AtomicReference<String> errorMessage = new AtomicReference<>();
  private final JoinGameData joinGameData;

  private final AtomicReference<GameConnection> gameConnectionRef = new AtomicReference<>();

  private final PlayerConnectionContextData.PlayerConnectionContextDataBuilder playerContextDataBuilder;


  public GetServerInfoScreen(final DaiKombatGame game,
      final JoinGameData joinGameData) {
    super(game);
    this.joinGameData = joinGameData;
    this.playerContextDataBuilder = PlayerConnectionContextData.builder();
  }

  @Override
  public void show() {
    new Thread(() -> {
      try {
        gameConnectionRef.set(new GameConnection(GameServerCreds.builder()
            .hostPort(HostPort.builder()
                .host(joinGameData.getServerHost())
                .port(joinGameData.getServerPort()).build())
            .password(joinGameData.getServerPassword())
            .build(),
            gameConnection -> gameConnection.write(GetServerInfoCommand.newBuilder().build())));

      } catch (Throwable e) {
        LOG.error("Can't create connection", e);
        errorMessage.set(ExceptionUtils.getMessage(e));
      }
    }).start();
  }


  @Override
  protected void onEscape() {
    Optional.ofNullable(gameConnectionRef.get()).ifPresent(GameConnection::disconnect);
  }

  @Override
  protected void onLoadingRender(final float delta) {
    if (errorMessage.get() != null) {
      removeAllEntities();
      getGame().setScreen(new ErrorScreen(getGame(), errorMessage.get()));
      return;
    }
    Optional.ofNullable(gameConnectionRef.get())
        .ifPresent(conn -> conn.getResponse().poll().ifPresentOrElse(response -> {
          if (response.hasErrorEvent()) {
            errorMessage.set(response.getErrorEvent().getMessage());
          } else if (response.hasServerInfo()) {
            var serverInfo = response.getServerInfo();
            playerContextDataBuilder.movesUpdateFreqMls(serverInfo.getMovesUpdateFreqMls());
            playerContextDataBuilder.fragsToWin(serverInfo.getFragsToWin());
            removeAllEntities();
            getGame().setScreen(new JoinGameScreen(getGame(),
                playerContextDataBuilder, joinGameData, conn));
          }
        }, () -> conn.getErrors().poll().ifPresent(throwable -> {
          LOG.error("Error while loading", throwable);
          conn.disconnect();
          errorMessage.set(ExceptionUtils.getMessage(throwable));
        })));
  }


  @Override
  protected void onTimeout() {
    Optional.ofNullable(gameConnectionRef.get()).ifPresent(GameConnection::disconnect);
  }

}
