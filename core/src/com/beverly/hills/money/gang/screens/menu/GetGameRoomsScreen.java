package com.beverly.hills.money.gang.screens.menu;

import com.beverly.hills.money.gang.DaiKombatGame;
import com.beverly.hills.money.gang.entity.HostPort;
import com.beverly.hills.money.gang.network.GameConnection;
import com.beverly.hills.money.gang.proto.GetServerInfoCommand;
import com.beverly.hills.money.gang.screens.GameScreen;
import com.beverly.hills.money.gang.screens.data.ConnectServerData;
import com.beverly.hills.money.gang.screens.data.JoinGameData;
import com.beverly.hills.money.gang.screens.loading.AbstractLoadingScreen;
import com.beverly.hills.money.gang.screens.loading.JoinGameScreen;
import com.beverly.hills.money.gang.screens.ui.selection.GameRoom;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetGameRoomsScreen extends AbstractLoadingScreen {

  private static final Logger LOG = LoggerFactory.getLogger(GetGameRoomsScreen.class);

  private final JoinGameData joinGameData;

  private final ConnectServerData connectServerData;

  private final AtomicReference<String> errorMessage = new AtomicReference<>();

  private final AtomicReference<GameConnection> gameConnectionRef = new AtomicReference<>();


  public GetGameRoomsScreen(final DaiKombatGame game,
      final JoinGameData joinGameData,
      final ConnectServerData connectServerData) {
    super(game);
    this.joinGameData = joinGameData;
    this.connectServerData = connectServerData;

  }

  @Override
  public void show() {
    new Thread(() -> {
      try {
        var hostPort = HostPort.builder()
            .host(connectServerData.getServerHost())
            .port(connectServerData.getServerPort()).build();
        var connection = new GameConnection(hostPort);
        if (!connection.waitUntilConnected(5_000)) {
          errorMessage.set("Connection timeout");
          connection.disconnect();
          return;
        }
        gameConnectionRef.set(connection);
        connection.write(GetServerInfoCommand.newBuilder().setPlayerClass(
            JoinGameScreen.createPlayerClass(joinGameData.getGamePlayerClass())).build());
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
  public void onExitScreen(GameScreen screen) {
    Optional.ofNullable(gameConnectionRef.get()).ifPresent(GameConnection::disconnect);
  }


  @Override
  protected void onTimeout() {
    Optional.ofNullable(gameConnectionRef.get()).ifPresent(GameConnection::disconnect);
  }

  @Override
  public void hide() {
    super.hide();
    Optional.ofNullable(gameConnectionRef.get()).ifPresent(GameConnection::disconnect);
  }

  @Override
  public void dispose() {
    super.dispose();
    Optional.ofNullable(gameConnectionRef.get()).ifPresent(GameConnection::disconnect);
  }

  @Override
  protected void onLoadingRender(float delta) {
    if (StringUtils.isNotBlank(errorMessage.get())) {
      getGame().setScreen(new ErrorScreen(getGame(), errorMessage.get()));
      return;
    }
    Optional.ofNullable(gameConnectionRef.get()).ifPresent(
        connection -> {
          connection.getResponse().poll().ifPresent(response -> {
            if (response.hasErrorEvent()) {
              errorMessage.set(response.getErrorEvent().getMessage());
            } else if (response.hasGameOver()) {
              errorMessage.set("Can't connect. Game is over.");
            } else if (response.hasServerInfo()) {
              var serverInfo = response.getServerInfo();
              LOG.info("Got server info {}", serverInfo);
              var gameRooms = serverInfo.getGamesList().stream()
                  .map(gameInfo -> GameRoom.builder().roomId(gameInfo.getGameId())
                      .playersOnline(gameInfo.getPlayersOnline())
                      .mapName(gameInfo.getMapMetadata().getName())
                      .mapHash(gameInfo.getMapMetadata().getHash())
                      .description(gameInfo.getDescription())
                      .title(gameInfo.getTitle())
                      .build()).collect(Collectors.toList());
              removeAllEntities();
              getGame().setScreen(new ChooseGameRoomScreen(getGame(),
                  joinGameData, connectServerData, gameRooms));

            }
          });
          connection.getErrors().poll().stream().findFirst().ifPresent(throwable -> {
            LOG.error("Error while loading", throwable);
            connection.disconnect();
            errorMessage.set(ExceptionUtils.getMessage(throwable));
          });
        });
  }


}
