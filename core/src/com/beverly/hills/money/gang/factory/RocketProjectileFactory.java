package com.beverly.hills.money.gang.factory;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.beverly.hills.money.gang.entities.player.Player;
import com.beverly.hills.money.gang.entities.player.Player.ProjectileEnemy;
import com.beverly.hills.money.gang.entities.player.Player.ProjectilePlayer;
import com.beverly.hills.money.gang.entities.projectile.Projectile;
import com.beverly.hills.money.gang.entities.projectile.RocketProjectile;
import com.beverly.hills.money.gang.screens.ui.weapon.WeaponState;

public class RocketProjectileFactory implements ProjectileFactory {

  @Override
  public Projectile create(final Player player, final WeaponState weaponState) {
    var playerCam = player.getPlayerCam();
    var screen = player.getScreen();
    return new RocketProjectile(player,
        new Vector3(playerCam.position.x - 0.5f + playerCam.direction.x * 0.001f, 0,
            playerCam.position.z - 0.5f + playerCam.direction.z * 0.001f),
        new Vector2(playerCam.position.x - 0.5f + playerCam.direction.x * weaponState.getDistance(),
            playerCam.position.z + playerCam.direction.z * weaponState.getDistance()),
        screen, projectile -> {

      var enemiesInRange = player.getEnemiesRegistry()
          .getEnemiesInRange(projectile.currentPosition(),
              weaponState.getProjectileRadius());
      if (enemiesInRange.isEmpty()) {
        player.getOnProjectileAttackHit().accept(
            ProjectileEnemy.builder().enemyPlayer(null).projectile(projectile).player(player)
                .build());
      } else {
        enemiesInRange.forEach(enemyPlayer -> player.getOnProjectileAttackHit().accept(
            ProjectileEnemy.builder().enemyPlayer(enemyPlayer).projectile(projectile).player(player)
                .build()));
      }
      // if projectile hit myself
      if (weaponState.getProjectileRadius() > player.getCurrent2DPosition()
          .dst(projectile.currentPosition())) {
        player.getOnProjectileSelfHit()
            .accept(ProjectilePlayer.builder().player(player).projectile(projectile).build());
      }

    });
  }
}
