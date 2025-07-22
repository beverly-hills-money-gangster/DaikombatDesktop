package com.beverly.hills.money.gang.screens.loading;

import com.beverly.hills.money.gang.Constants;
import com.beverly.hills.money.gang.DaiKombatGame;
import com.beverly.hills.money.gang.entity.HostPort;
import com.beverly.hills.money.gang.network.GameConnection;
import com.beverly.hills.money.gang.proto.DownloadMapAssetsCommand;
import com.beverly.hills.money.gang.screens.data.CompleteJoinGameData;
import com.beverly.hills.money.gang.screens.menu.ErrorScreen;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DownloadMapAssetsScreen extends AbstractLoadingScreen {

  private static final Logger LOG = LoggerFactory.getLogger(DownloadMapAssetsScreen.class);
  private final CompleteJoinGameData completeJoinGameData;
  private final AtomicReference<String> errorMessage = new AtomicReference<>();

  private final AtomicReference<GameConnection> gameConnectionRef = new AtomicReference<>();


  public DownloadMapAssetsScreen(final DaiKombatGame game,
      final CompleteJoinGameData completeJoinGameData) {
    super(game);
    this.completeJoinGameData = completeJoinGameData;

  }


  @Override
  public void show() {
    new Thread(() -> {
      try {
        var hostPort = HostPort.builder()
            .host(completeJoinGameData.getConnectServerData().getServerHost())
            .port(completeJoinGameData.getConnectServerData().getServerPort()).build();
        var connection = new GameConnection(hostPort);
        if (!connection.waitUntilConnected(5_000)) {
          errorMessage.set("Connection timeout");
          connection.disconnect();
          return;
        }
        gameConnectionRef.set(connection);
        connection.write(
            DownloadMapAssetsCommand.newBuilder().setMapName(completeJoinGameData.getMapName())
                .build());
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
    if (StringUtils.isNotBlank(errorMessage.get())) {
      removeAllEntities();
      getGame().setScreen(
          new ErrorScreen(getGame(),
              StringUtils.defaultIfBlank(errorMessage.get(), "Can't download map")));
      return;
    }
    Optional.ofNullable(gameConnectionRef.get()).ifPresent(
        connection -> {
          connection.getResponse().poll().ifPresent(response -> {
            if (response.hasErrorEvent()) {
              errorMessage.set(response.getErrorEvent().getMessage());
            } else if (response.hasMapAssets()) {
              var mapAssets = response.getMapAssets();
              // this is blocking
              getGame().getAssMan()
                  .saveMap(completeJoinGameData.getMapName(),
                      completeJoinGameData.getMapHash(),
                      mapAssets);
              removeAllEntities();
              getGame().setScreen(new ConnectServerScreen(getGame(), completeJoinGameData));
            }
          });
          connection.getErrors().poll().ifPresent(throwable -> {
            LOG.error("Error while loading", throwable);
            connection.disconnect();
            errorMessage.set(ExceptionUtils.getMessage(throwable));
          });
        });
  }


  @Override
  protected void onTimeout() {
    Optional.ofNullable(gameConnectionRef.get()).ifPresent(GameConnection::disconnect);
  }

  @Override
  protected String getBaseLoadingMessage() {
    return Constants.DOWNLOAD_MAP;
  }

  @Override
  public void hide() {
    super.hide();
    Optional.ofNullable(gameConnectionRef.get()).ifPresent(GameConnection::disconnect);
  }

}
