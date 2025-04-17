package com.beverly.hills.money.gang.screens;

import com.beverly.hills.money.gang.Configs;
import com.beverly.hills.money.gang.Constants;
import com.beverly.hills.money.gang.DaiKombatGame;
import com.beverly.hills.money.gang.entity.HostPort;
import com.beverly.hills.money.gang.network.GameConnection;
import com.beverly.hills.money.gang.network.GlobalGameConnection;
import com.beverly.hills.money.gang.network.SecondaryGameConnection;
import com.beverly.hills.money.gang.proto.GetServerInfoCommand;
import com.beverly.hills.money.gang.proto.ServerResponse.ProjectileInfo;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectServerScreen extends ReconnectableScreen {

  private static final Logger LOG = LoggerFactory.getLogger(ConnectServerScreen.class);
  private final ConnectGameData connectGameData;

  private final AtomicReference<String> errorMessage = new AtomicReference<>();

  private final AtomicReference<GlobalGameConnection> gameConnectionRef = new AtomicReference<>();

  private final PlayerConnectionContextData.PlayerConnectionContextDataBuilder playerContextDataBuilder;

  public ConnectServerScreen(final DaiKombatGame game,
      final ConnectGameData connectGameData,
      final int connectionTrial) {
    super(game, connectionTrial);
    this.connectGameData = connectGameData;
    this.playerContextDataBuilder = PlayerConnectionContextData.builder();

  }

  public ConnectServerScreen(final DaiKombatGame game,
      final ConnectGameData connectGameData) {
    this(game, connectGameData, 0);
  }

  @Override
  public void show() {
    new Thread(() -> {
      try {
        var hostPort = HostPort.builder()
            .host(connectGameData.getServerHost())
            .port(connectGameData.getServerPort()).build();
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
            JoinGameScreen.createPlayerClass(connectGameData.getGamePlayerClass())).build());
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
      reconnect(errorMessage.get(), gameConnectionRef.get(), connectGameData);
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
              playerContextDataBuilder.movesUpdateFreqMls(serverInfo.getMovesUpdateFreqMls());
              playerContextDataBuilder.maxVisibility(serverInfo.getMaxVisibility());
              playerContextDataBuilder.fragsToWin(serverInfo.getFragsToWin());
              playerContextDataBuilder.speed(serverInfo.getPlayerSpeed());
              playerContextDataBuilder.audioSamplingRate(
                  serverInfo.getVoiceChatSamplingFrequencyHertz());
              playerContextDataBuilder.voiceChatPayloadSizeBytes(
                  serverInfo.getVoiceChatPayloadBytes());
              playerContextDataBuilder.weaponStats(getWeaponStats(
                  serverInfo.getWeaponsInfoList(),
                  serverInfo.getProjectileInfoList()));
              playerContextDataBuilder.matchId(serverInfo.getGames(Configs.GAME_ID).getMatchId());
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
              .build());
    });
    if (weaponStats.size() != Weapon.values().length) {
      throw new IllegalStateException("Not all weapons have max distance");
    }
    return weaponStats;
  }


  @Override
  protected void onTimeout() {
    Optional.ofNullable(gameConnectionRef.get()).ifPresent(GlobalGameConnection::disconnect);
  }


}
