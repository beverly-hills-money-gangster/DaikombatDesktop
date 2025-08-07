package com.beverly.hills.money.gang.screens.loading;

import com.beverly.hills.money.gang.Constants;
import com.beverly.hills.money.gang.DaiKombatGame;
import com.beverly.hills.money.gang.entity.HostPort;
import com.beverly.hills.money.gang.network.GameConnection;
import com.beverly.hills.money.gang.network.GlobalGameConnection;
import com.beverly.hills.money.gang.network.SecondaryGameConnection;
import com.beverly.hills.money.gang.proto.GetServerInfoCommand;
import com.beverly.hills.money.gang.proto.ServerResponse.ProjectileInfo;
import com.beverly.hills.money.gang.proto.ServerResponse.WeaponInfo;
import com.beverly.hills.money.gang.screens.data.CompleteJoinGameData;
import com.beverly.hills.money.gang.screens.data.GameBootstrapData;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectServerScreen extends ReconnectableScreen {

  private static final Logger LOG = LoggerFactory.getLogger(ConnectServerScreen.class);
  private final CompleteJoinGameData completeJoinGameData;
  private final AtomicReference<String> errorMessage = new AtomicReference<>();

  private final AtomicReference<GlobalGameConnection> gameConnectionRef = new AtomicReference<>();

  private final GameBootstrapData.GameBootstrapDataBuilder playerContextDataBuilder;

  public ConnectServerScreen(final DaiKombatGame game,
      final CompleteJoinGameData completeJoinGameData,
      final int connectionTrial) {
    super(game, connectionTrial);
    this.completeJoinGameData = completeJoinGameData;
    this.playerContextDataBuilder = GameBootstrapData.builder();

  }

  public ConnectServerScreen(final DaiKombatGame game,
      final CompleteJoinGameData completeJoinGameData) {
    this(game, completeJoinGameData, 0);
  }

  @Override
  public void show() {
    new Thread(() -> {
      try {
        var hostPort = HostPort.builder()
            .host(completeJoinGameData.getConnectServerData().getServerHost())
            .port(completeJoinGameData.getConnectServerData().getServerPort()).build();
        var connection = new GameConnection(hostPort);
        GlobalGameConnection loadBalancedGameConnection
            = new GlobalGameConnection(
            connection, createSecondaryConnections(hostPort));
        if (!loadBalancedGameConnection.waitUntilAllConnected(5_000)) {
          errorMessage.set("Connection timeout");
          loadBalancedGameConnection.disconnect();
          return;
        }
        gameConnectionRef.set(loadBalancedGameConnection);
        connection.write(GetServerInfoCommand.newBuilder().setPlayerClass(
            JoinGameScreen.createPlayerClass(
                completeJoinGameData.getJoinGameData().getGamePlayerClass())).build());
      } catch (Throwable e) {
        LOG.error("Can't create connection", e);
        errorMessage.set(ExceptionUtils.getMessage(e));
      }
    }).start();
  }

  private List<SecondaryGameConnection> createSecondaryConnections(HostPort hostPort)
      throws IOException {
    List<SecondaryGameConnection> secondaryGameConnections = new ArrayList<>();
    for (int i = 0; i < com.beverly.hills.money.gang.Configs.SECONDARY_CONNECTIONS_TO_OPEN; i++) {
      secondaryGameConnections.add(new SecondaryGameConnection(hostPort));
    }
    return secondaryGameConnections;
  }

  @Override
  protected void onEscape() {
    Optional.ofNullable(gameConnectionRef.get()).ifPresent(GlobalGameConnection::disconnect);
  }

  @Override
  protected void onLoadingRenderInternal(final float delta) {
    if (StringUtils.isNotBlank(errorMessage.get())) {
      reconnect(errorMessage.get(), gameConnectionRef.get(), completeJoinGameData);
      return;
    }
    Optional.ofNullable(gameConnectionRef.get()).ifPresent(
        connection -> {
          connection.pollPrimaryConnectionResponse().ifPresent(response -> {
            if (response.hasErrorEvent()) {
              errorMessage.set(response.getErrorEvent().getMessage());
            } else if (response.hasGameOver()) {
              LOG.info("Game is over. Try to reconnect");
              errorMessage.set("Can't connect. Game is over.");
            } else if (response.hasServerInfo()) {
              var serverInfo = response.getServerInfo();
              LOG.info("Server info:\n{}", serverInfo);
              var gameRoom = serverInfo.getGamesList().stream().filter(
                      gameInfo -> gameInfo.getGameId() == completeJoinGameData.getGameRoomId())
                  .findFirst().orElseThrow(
                      () -> new IllegalStateException(
                          "Can't find room " + completeJoinGameData.getGameRoomId()));

              playerContextDataBuilder.movesUpdateFreqMls(serverInfo.getMovesUpdateFreqMls());
              playerContextDataBuilder.maxVisibility(gameRoom.getMaxVisibility());
              playerContextDataBuilder.fragsToWin(serverInfo.getFragsToWin());
              playerContextDataBuilder.speed(gameRoom.getPlayerSpeed());
              var weaponStats = getWeaponStats(
                  gameRoom.getWeaponsInfoList(), gameRoom.getProjectileInfoList());
              LOG.info("Weapon stats {}", weaponStats);
              playerContextDataBuilder.weaponStats(weaponStats);
              removeAllEntities();
              LOG.info("Got server info. Try join the game");
              getGame().setScreen(new JoinGameScreen(getGame(),
                  playerContextDataBuilder, completeJoinGameData, connection, connectionTrial));
            }
          });
          connection.pollErrors().stream().findFirst().ifPresent(throwable -> {
            LOG.error("Error while loading", throwable);
            connection.disconnect();
            errorMessage.set(ExceptionUtils.getMessage(throwable));
          });
        });
  }


  private Map<Weapon, WeaponStats> getWeaponStats(
      List<WeaponInfo> weaponInfo,
      List<ProjectileInfo> projectileInfo) {
    Map<Weapon, WeaponStats> weaponStats = new HashMap<>();
    weaponInfo.forEach(info -> {
      var weapon = WeaponMapper.getWeapon(info.getWeaponType());
      weaponStats.put(weapon,
          WeaponStats.builder()
              .delayMls(info.getDelayMls())
              .projectileRadius(projectileInfo.stream().filter(
                      projectile -> WeaponMapper.getWeaponProjectile(projectile.getProjectileType())
                          == weapon.getProjectileRef()).findFirst()
                  .map(projectile -> (float) projectile.getRadius()).orElse(null))
              .maxDistance((float) info.getMaxDistance() - Constants.HALF_UNIT)
              .maxAmmo(info.hasMaxAmmo() ? info.getMaxAmmo() : null)
              .build());
    });
    return weaponStats;
  }


  @Override
  protected void onTimeout() {
    Optional.ofNullable(gameConnectionRef.get()).ifPresent(GlobalGameConnection::disconnect);
  }

  @Override
  protected String getBaseLoadingMessage() {
    return Constants.SERVER_CONNECT;
  }

}
