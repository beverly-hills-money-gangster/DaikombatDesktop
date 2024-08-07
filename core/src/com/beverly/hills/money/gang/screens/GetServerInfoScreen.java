package com.beverly.hills.money.gang.screens;

import com.beverly.hills.money.gang.Constants;
import com.beverly.hills.money.gang.DaiKombatGame;
import com.beverly.hills.money.gang.entity.GameServerCreds;
import com.beverly.hills.money.gang.entity.HostPort;
import com.beverly.hills.money.gang.network.GameConnection;
import com.beverly.hills.money.gang.network.LoadBalancedGameConnection;
import com.beverly.hills.money.gang.network.SecondaryGameConnection;
import com.beverly.hills.money.gang.proto.GetServerInfoCommand;
import com.beverly.hills.money.gang.proto.ServerResponse.GameEvent;
import com.beverly.hills.money.gang.proto.ServerResponse.WeaponInfo;
import com.beverly.hills.money.gang.screens.data.JoinGameData;
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
      removeAllEntities();
      LOG.error("Got error '{}'", errorMessage.get());
      Optional.ofNullable(gameConnectionRef.get())
          .ifPresent(LoadBalancedGameConnection::disconnect);
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
              playerContextDataBuilder.weaponStats(getWeaponStats(serverInfo.getWeaponsInfoList()));
              removeAllEntities();
              LOG.info("Got server info. Try join the game");
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

}
