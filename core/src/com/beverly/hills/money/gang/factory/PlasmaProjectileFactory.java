package com.beverly.hills.money.gang.factory;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.beverly.hills.money.gang.entities.player.Player;
import com.beverly.hills.money.gang.entities.projectile.PlasmaProjectile;
import com.beverly.hills.money.gang.entities.projectile.Projectile;
import com.beverly.hills.money.gang.screens.ui.weapon.WeaponState;

public class PlasmaProjectileFactory implements ProjectileFactory {

  @Override
  public Projectile create(final Player player, final WeaponState weaponState) {
    var playerCam = player.getPlayerCam();
    var screen = player.getScreen();
    return new PlasmaProjectile(player,
        new Vector3(playerCam.position.x - 0.5f + playerCam.direction.x * 0.001f, 0,
            playerCam.position.z - 0.5f + playerCam.direction.z * 0.001f),
        new Vector2(playerCam.position.x - 0.5f + playerCam.direction.x * weaponState.getDistance(),
            playerCam.position.z + playerCam.direction.z * weaponState.getDistance()),
        screen, weaponState);
  }
}
