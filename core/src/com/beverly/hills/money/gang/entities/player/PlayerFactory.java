package com.beverly.hills.money.gang.entities.player;

import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import com.beverly.hills.money.gang.assets.managers.sound.SoundVolumeType;
import com.beverly.hills.money.gang.assets.managers.sound.TimeLimitedSound;
import com.beverly.hills.money.gang.assets.managers.sound.TimeLimitedSound.TimeLimitSoundConf;
import com.beverly.hills.money.gang.entities.enemies.EnemyPlayer;
import com.beverly.hills.money.gang.entities.item.PowerUpType;
import com.beverly.hills.money.gang.entities.projectile.Projectile;
import com.beverly.hills.money.gang.network.GlobalGameConnection;
import com.beverly.hills.money.gang.proto.ProjectileStats;
import com.beverly.hills.money.gang.proto.ProjectileType;
import com.beverly.hills.money.gang.proto.PushGameEventCommand;
import com.beverly.hills.money.gang.proto.PushGameEventCommand.GameEventType;
import com.beverly.hills.money.gang.proto.Vector;
import com.beverly.hills.money.gang.proto.WeaponType;
import com.beverly.hills.money.gang.screens.game.PlayScreen;
import com.beverly.hills.money.gang.screens.data.GameBootstrapData;
import com.beverly.hills.money.gang.screens.ui.EnemyAim;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO maybe test it
public class PlayerFactory {

  private static final Logger LOG = LoggerFactory.getLogger(PlayerFactory.class);

  public static Player create(
      PlayScreen screen,
      GlobalGameConnection gameConnection,
      GameBootstrapData gameBootstrapData) {
    return new Player(screen,
        playerWeapon -> {
          if (playerWeapon.getPlayer().isCollidedWithTeleport()) {
            LOG.warn("Can't shoot while being teleported");
            return;
          }
          WeaponType weaponType = switch (playerWeapon.getWeapon()) {
            case GAUNTLET -> WeaponType.PUNCH;
            case SHOTGUN -> WeaponType.SHOTGUN;
            case RAILGUN -> WeaponType.RAILGUN;
            case MINIGUN -> WeaponType.MINIGUN;
            case ROCKET_LAUNCHER -> WeaponType.ROCKET_LAUNCHER;
            case PLASMAGUN -> WeaponType.PLASMAGUN;
          };
          var direction = playerWeapon.getPlayer().getCurrent2DDirection();
          var position = playerWeapon.getPlayer().getCurrent2DPosition();
          Optional<EnemyPlayer> enemyHit =
              playerWeapon.getWeapon().hasProjectile() ? Optional.empty() :
                  playerWeapon.getPlayer().getEnemyRectInRangeFromCam(
                      playerWeapon.getPlayer().getWeaponDistance(playerWeapon.getWeapon()));

          var commandBuilder = PushGameEventCommand.newBuilder()
              .setGameId(gameBootstrapData.getCompleteJoinGameData().getGameRoomId())
              .setMatchId(gameBootstrapData.getMatchId())
              .setSequence(screen.getActionSequence().incrementAndGet())
              .setPingMls(
                  Optional.ofNullable(gameConnection.getPrimaryNetworkStats().getPingMls())
                      .orElse(0))
              .setPlayerId(gameBootstrapData.getPlayerId())
              .setDirection(
                  Vector.newBuilder().setX(direction.x).setY(direction.y)
                      .build())
              .setPosition(
                  Vector.newBuilder().setX(position.x).setY(position.y)
                      .build())
              .setWeaponType(weaponType)
              .setEventType(GameEventType.ATTACK);

          enemyHit.ifPresent(enemyPlayer -> {
            enemyPlayer.getHit();
            playerWeapon.getPlayer().playWeaponHitSound(playerWeapon.getWeapon());
            commandBuilder.setAffectedPlayerId(enemyPlayer.getEnemyPlayerId());
          });
          gameConnection.write(commandBuilder.build());
        },
        enemy -> {
          if (!enemy.getEnemyEffects().isPowerUpActive(PowerUpType.INVISIBILITY)) {
            screen.setEnemyAim(
                EnemyAim.builder().name(enemy.getName()).hp(enemy.getHp())
                    .playerClass(enemy.getEnemyClass().toString())
                    .build());
          }
        },
        player -> {
          if (!screen.isTimeToSendMoves()
              || gameConnection.isAnyDisconnected()
              || player.isCollidedWithTeleport()) {
            return;
          }
          screen.sendCurrentPlayerPosition();
        },
        projectileEnemy -> {
          if (projectileEnemy.getPlayer().isCollidedWithTeleport()) {
            LOG.warn("Can't shoot projectiles while being teleported");
            return;
          }
          var enemies = projectileEnemy.getEnemyPlayers();
          var projectile = projectileEnemy.getProjectile();
          var player = projectileEnemy.getPlayer();
          var projectilePosition = projectile.currentPosition();
          var direction = player.getCurrent2DDirection();
          var position = player.getCurrent2DPosition();

          var commandBuilder = PushGameEventCommand.newBuilder()
              .setGameId(gameBootstrapData.getCompleteJoinGameData().getGameRoomId())
              .setMatchId(gameBootstrapData.getMatchId())
              .setSequence(screen.getActionSequence().incrementAndGet())
              .setPingMls(
                  Optional.ofNullable(gameConnection.getPrimaryNetworkStats().getPingMls())
                      .orElse(0))
              .setPlayerId(gameBootstrapData.getPlayerId())
              .setDirection(
                  Vector.newBuilder().setX(direction.x).setY(direction.y).build())
              .setPosition(
                  Vector.newBuilder().setX(position.x).setY(position.y).build())
              .setProjectile(ProjectileStats.newBuilder().setPosition(
                      Vector.newBuilder().setX(projectilePosition.x).setY(projectilePosition.y)
                          .build())
                  .setProjectileType(mapProjectileToWeaponType(projectile)).build())
              .setEventType(GameEventType.ATTACK);

          enemies.forEach(enemyPlayer -> {
            enemyPlayer.getHit();
            new TimeLimitedSound(
                screen.getGame().getAssMan().getUserSettingSound(SoundRegistry.HIT_SOUND))
                .play(TimeLimitSoundConf.builder()
                    .soundVolumeType(SoundVolumeType.LOUD).frequencyMls(500)
                    .build());
          });
          gameConnection.write(commandBuilder.build());
        },
        gameBootstrapData.getSpawn(),
        gameBootstrapData.getDirection(),
        gameBootstrapData.getSpeed(),
        gameBootstrapData.getWeaponStats(),
        gameBootstrapData.getMaxVisibility(),
        gameBootstrapData.getCompleteJoinGameData().getJoinGameData().getGamePlayerClass()
        , () -> new TimeLimitedSound(
        screen.getGame().getAssMan().getUserSettingSound(SoundRegistry.NO_AMMO)).play(
        TimeLimitSoundConf.builder().soundVolumeType(SoundVolumeType.NORMAL).frequencyMls(250)
            .build()));
  }

  private static ProjectileType mapProjectileToWeaponType(Projectile projectile) {
    return switch (projectile.getProjectileType()) {
      case ROCKET -> ProjectileType.ROCKET;
      case PLASMA -> ProjectileType.PLASMA;
    };
  }


}
