package com.beverly.hills.money.gang.screens;

import com.beverly.hills.money.gang.Constants;
import com.beverly.hills.money.gang.DaiKombatGame;
import com.beverly.hills.money.gang.entity.GameServerCreds;
import com.beverly.hills.money.gang.entity.HostPort;
import com.beverly.hills.money.gang.network.GameConnection;
import com.beverly.hills.money.gang.network.LoadBalancedGameConnection;
import com.beverly.hills.money.gang.network.SecondaryGameConnection;
import com.beverly.hills.money.gang.proto.GetServerInfoCommand;
import com.beverly.hills.money.gang.proto.ServerResponse.WeaponInfo;
import com.beverly.hills.money.gang.screens.data.ConnectGameData;
import com.beverly.hills.money.gang.screens.data.PlayerConnectionContextData;
import com.beverly.hills.money.gang.screens.ui.weapon.Weapon;
import com.beverly.hills.money.gang.screens.ui.weapon.WeaponMapper;
import com.beverly.hills.money.gang.screens.ui.weapon.WeaponStats;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectServerScreen extends ReconnectableScreen {

  private static final Logger LOG = LoggerFactory.getLogger(ConnectServerScreen.class);
  private final AtomicReference<String> errorMessage = new AtomicReference<>();
  private final ConnectGameData connectGameData;

  private final int connectionTrial;

  private final String loadingMessage;

  private final AtomicReference<LoadBalancedGameConnection> gameConnectionRef = new AtomicReference<>();

  private final PlayerConnectionContextData.PlayerConnectionContextDataBuilder playerContextDataBuilder;


  public ConnectServerScreen(final DaiKombatGame game,
      final ConnectGameData connectGameData,
      final int connectionTrial) {
    super(game);
    this.connectionTrial = connectionTrial;
    this.connectGameData = connectGameData;
    this.playerContextDataBuilder = PlayerConnectionContextData.builder();
    if (connectionTrial > 0) {
      this.loadingMessage = Constants.CONNECTING + " ATTEMPT " + (connectionTrial + 1);
    } else {
      this.loadingMessage = Constants.CONNECTING;
    }
  }

  public ConnectServerScreen(final DaiKombatGame game,
      final ConnectGameData connectGameData) {
    this(game, connectGameData, 0);
  }

  @Override
  public void show() {
    new Thread(() -> {
      try {
        var creds = GameServerCreds.builder()
            .hostPort(HostPort.builder()
                .host(connectGameData.getServerHost())
                .port(connectGameData.getServerPort()).build())
            .password(connectGameData.getServerPassword())
            .build();
        var connection = new GameConnection(creds);
        LoadBalancedGameConnection loadBalancedGameConnection
            = new LoadBalancedGameConnection(
            connection, createSecondaryConnections(creds));
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

  private List<SecondaryGameConnection> createSecondaryConnections(GameServerCreds creds)
      throws IOException {
    List<SecondaryGameConnection> secondaryGameConnections = new ArrayList<>();
    for (int i = 0; i < com.beverly.hills.money.gang.Configs.SECONDARY_CONNECTIONS_TO_OPEN; i++) {
      secondaryGameConnections.add(new SecondaryGameConnection(creds));
    }
    return secondaryGameConnections;
  }

  @Override
  protected void onEscape() {
    Optional.ofNullable(gameConnectionRef.get()).ifPresent(LoadBalancedGameConnection::disconnect);
  }

  @Override
  protected void onLoadingRender(final float delta) {
    if (errorMessage.get() != null) {
      reconnectOnError(errorMessage.get(), connectionTrial, gameConnectionRef.get(),
          connectGameData);
      return;
    }
    Optional.ofNullable(gameConnectionRef.get()).ifPresent(
        connection -> {
          connection.pollPrimaryConnectionResponse().ifPresent(response -> {
            if (response.hasErrorEvent()) {
              errorMessage.set(response.getErrorEvent().getMessage());
            } else if (response.hasGameOver()) {
              LOG.info("Game is over. Try to reconnect");
              reconnectOnError("Game over", connectionTrial, connection, connectGameData);
            } else if (response.hasServerInfo()) {
              var serverInfo = response.getServerInfo();
              playerContextDataBuilder.movesUpdateFreqMls(serverInfo.getMovesUpdateFreqMls());
              playerContextDataBuilder.fragsToWin(serverInfo.getFragsToWin());
              playerContextDataBuilder.speed(serverInfo.getPlayerSpeed());
              playerContextDataBuilder.weaponStats(getWeaponStats(serverInfo.getWeaponsInfoList()));
              removeAllEntities();
              LOG.info("Got server info. Try join the game");
              getGame().setScreen(new JoinGameScreen(getGame(),
                  playerContextDataBuilder, connectGameData, connection, connectionTrial));
            }
          });
          connection.pollErrors().stream().findFirst().ifPresent(throwable -> {
            LOG.error("Error while loading", throwable);
            connection.disconnect();
            errorMessage.set(ExceptionUtils.getMessage(throwable));
          });
        });
  }


  private Map<Weapon, WeaponStats> getWeaponStats(List<WeaponInfo> weaponInfo) {
    Map<Weapon, WeaponStats> weaponStats = new HashMap<>();
    weaponInfo.forEach(info -> weaponStats.put(WeaponMapper.getWeapon(info.getWeaponType()),
        WeaponStats.builder()
            .delayMls(info.getDelayMls())
            .maxDistance((float) info.getMaxDistance() - Constants.HALF_UNIT)
            .build()));
    if (weaponStats.size() != Weapon.values().length) {
      throw new IllegalStateException("Not all weapons have max distance");
    }
    return weaponStats;
  }


  @Override
  protected void onTimeout() {
    Optional.ofNullable(gameConnectionRef.get()).ifPresent(LoadBalancedGameConnection::disconnect);
  }

  @Override
  protected String getBaseLoadingMessage() {
    return loadingMessage;
  }

}
