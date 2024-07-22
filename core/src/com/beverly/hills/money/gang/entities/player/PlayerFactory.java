package com.beverly.hills.money.gang.entities.player;

import com.beverly.hills.money.gang.Configs;
import com.beverly.hills.money.gang.entities.item.PowerUpType;
import com.beverly.hills.money.gang.network.LoadBalancedGameConnection;
import com.beverly.hills.money.gang.proto.PushGameEventCommand;
import com.beverly.hills.money.gang.screens.PlayScreen;
import com.beverly.hills.money.gang.screens.data.PlayerConnectionContextData;
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
          PushGameEventCommand.GameEventType eventType = switch (playerWeapon.getWeapon()) {
            case GAUNTLET -> PushGameEventCommand.GameEventType.PUNCH;
            case SHOTGUN -> PushGameEventCommand.GameEventType.SHOOT;
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
                    PushGameEventCommand.Vector.newBuilder().setX(direction.x).setY(direction.y)
                        .build())
                .setPosition(
                    PushGameEventCommand.Vector.newBuilder().setX(position.x).setY(position.y)
                        .build())
                .setAffectedPlayerId(enemy.getEnemyPlayerId())
                .setEventType(eventType)
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
                    PushGameEventCommand.Vector.newBuilder().setX(direction.x).setY(direction.y)
                        .build())
                .setPosition(
                    PushGameEventCommand.Vector.newBuilder().setX(position.x).setY(position.y)
                        .build())
                .setEventType(eventType)
                .build());
          }
        },
        enemy -> {
          if (!enemy.getEnemyEffects().isPowerUpActive(PowerUpType.INVISIBILITY)) {
            screen.setEnemyAimName(enemy.getName());
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
        playerConnectionContextData.getSpeed());
  }

}
