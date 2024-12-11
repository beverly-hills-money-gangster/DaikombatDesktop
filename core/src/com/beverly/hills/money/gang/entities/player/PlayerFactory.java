package com.beverly.hills.money.gang.entities.player;

import com.beverly.hills.money.gang.Configs;
import com.beverly.hills.money.gang.entities.item.PowerUpType;
import com.beverly.hills.money.gang.network.LoadBalancedGameConnection;
import com.beverly.hills.money.gang.proto.PushGameEventCommand;
import com.beverly.hills.money.gang.proto.PushGameEventCommand.GameEventType;
import com.beverly.hills.money.gang.proto.Vector;
import com.beverly.hills.money.gang.proto.WeaponType;
import com.beverly.hills.money.gang.screens.PlayScreen;
import com.beverly.hills.money.gang.screens.data.PlayerConnectionContextData;
import com.beverly.hills.money.gang.screens.ui.EnemyAim;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO maybe test it
public class PlayerFactory {

  private static final Logger LOG = LoggerFactory.getLogger(PlayerFactory.class);

  public static Player create(
      PlayScreen screen,
      LoadBalancedGameConnection gameConnection,
      PlayerConnectionContextData playerConnectionContextData) {
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
          };
          var direction = playerWeapon.getPlayer().getCurrent2DDirection();
          var position = playerWeapon.getPlayer().getCurrent2DPosition();
          boolean hitEnemy = playerWeapon.getPlayer().getEnemyRectInRangeFromCam(enemy -> {
            enemy.getHit();
            playerWeapon.getPlayer().playWeaponHitSound(playerWeapon.getWeapon());
            gameConnection.write(PushGameEventCommand.newBuilder()
                .setGameId(Configs.GAME_ID)
                .setPingMls(
                    Optional.ofNullable(gameConnection.getPrimaryNetworkStats().getPingMls())
                        .orElse(0))
                .setPlayerId(playerConnectionContextData.getPlayerId())
                .setSequence(screen.getActionSequence().incrementAndGet())
                .setDirection(
                    Vector.newBuilder().setX(direction.x).setY(direction.y)
                        .build())
                .setPosition(
                    Vector.newBuilder().setX(position.x).setY(position.y)
                        .build())
                .setAffectedPlayerId(enemy.getEnemyPlayerId())
                .setEventType(GameEventType.ATTACK)
                .setWeaponType(weaponType)
                .build());
          }, playerWeapon.getPlayer().getWeaponDistance(playerWeapon.getWeapon()));
          if (!hitEnemy) {
            // if we haven't hit anybody
            gameConnection.write(PushGameEventCommand.newBuilder()
                .setGameId(Configs.GAME_ID)
                .setSequence(screen.getActionSequence().incrementAndGet())
                .setPingMls(
                    Optional.ofNullable(gameConnection.getPrimaryNetworkStats().getPingMls())
                        .orElse(0))
                .setPlayerId(playerConnectionContextData.getPlayerId())
                .setDirection(
                    Vector.newBuilder().setX(direction.x).setY(direction.y)
                        .build())
                .setPosition(
                    Vector.newBuilder().setX(position.x).setY(position.y)
                        .build())
                .setWeaponType(weaponType)
                .setEventType(GameEventType.ATTACK)
                .build());
          }
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
        playerConnectionContextData.getSpawn(),
        playerConnectionContextData.getDirection(),
        playerConnectionContextData.getSpeed(),
        playerConnectionContextData.getWeaponStats());
  }

}
