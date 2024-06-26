package com.beverly.hills.money.gang.screens;

import com.beverly.hills.money.gang.DaiKombatGame;
import com.beverly.hills.money.gang.entity.GameServerCreds;
import com.beverly.hills.money.gang.entity.HostPort;
import com.beverly.hills.money.gang.network.GameConnection;
import com.beverly.hills.money.gang.network.LoadBalancedGameConnection;
import com.beverly.hills.money.gang.network.SecondaryGameConnection;
import com.beverly.hills.money.gang.proto.GetServerInfoCommand;
import com.beverly.hills.money.gang.screens.data.JoinGameData;
import com.beverly.hills.money.gang.screens.data.PlayerConnectionContextData;
import java.util.List;
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

  private final AtomicReference<LoadBalancedGameConnection> gameConnectionRef = new AtomicReference<>();

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
        var creds = GameServerCreds.builder()
            .hostPort(HostPort.builder()
                .host(joinGameData.getServerHost())
                .port(joinGameData.getServerPort()).build())
            .password(joinGameData.getServerPassword())
            .build();
        var connection = new GameConnection(creds);
        List<SecondaryGameConnection> secondaryGameConnections
            = List.of(new SecondaryGameConnection(creds), new SecondaryGameConnection(creds));
        LoadBalancedGameConnection loadBalancedGameConnection
            = new LoadBalancedGameConnection(
            connection, secondaryGameConnections);
        if (!loadBalancedGameConnection.waitUntilAllConnected(5_000)) {
          errorMessage.set("Connection timeout");
          loadBalancedGameConnection.disconnect();
          return;
        }
        gameConnectionRef.set(loadBalancedGameConnection);
        connection.write(GetServerInfoCommand.newBuilder().build());
      } catch (Throwable e) {
        LOG.error("Can't create connection", e);
        errorMessage.set(ExceptionUtils.getMessage(e));
      }
    }).start();
  }


  @Override
  protected void onEscape() {
    Optional.ofNullable(gameConnectionRef.get()).ifPresent(LoadBalancedGameConnection::disconnect);
  }

  @Override
  protected void onLoadingRender(final float delta) {
    if (errorMessage.get() != null) {
      removeAllEntities();
      LOG.error("Got error '{}'", errorMessage.get());
      Optional.ofNullable(gameConnectionRef.get()).ifPresent(LoadBalancedGameConnection::disconnect);
      getGame().setScreen(new ErrorScreen(getGame(), errorMessage.get()));
      return;
    }
    Optional.ofNullable(gameConnectionRef.get()).ifPresent(
        connection -> {
          connection.pollPrimaryConnectionResponse().ifPresent(response -> {
            if (response.hasErrorEvent()) {

              errorMessage.set(response.getErrorEvent().getMessage());
            } else if (response.hasServerInfo()) {
              var serverInfo = response.getServerInfo();
              playerContextDataBuilder.movesUpdateFreqMls(serverInfo.getMovesUpdateFreqMls());
              playerContextDataBuilder.fragsToWin(serverInfo.getFragsToWin());
              playerContextDataBuilder.speed(serverInfo.getPlayerSpeed());
              removeAllEntities();
              getGame().setScreen(new JoinGameScreen(getGame(),
                  playerContextDataBuilder, joinGameData, connection));
            }
          });
          connection.pollErrors().stream().findFirst().ifPresent(throwable -> {
            LOG.error("Error while loading", throwable);
            connection.disconnect();
            errorMessage.set(ExceptionUtils.getMessage(throwable));
          });
        });
  }


  @Override
  protected void onTimeout() {
    Optional.ofNullable(gameConnectionRef.get()).ifPresent(LoadBalancedGameConnection::disconnect);
  }

}
