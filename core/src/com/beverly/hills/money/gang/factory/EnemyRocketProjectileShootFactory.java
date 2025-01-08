package com.beverly.hills.money.gang.factory;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.beverly.hills.money.gang.entities.player.Player;
import com.beverly.hills.money.gang.entities.projectile.EnemyRocketProjectile;
import com.beverly.hills.money.gang.entities.projectile.Projectile;

public class EnemyRocketProjectileShootFactory implements EnemyProjectileShootFactory {


  @Override
  public Projectile create(Vector2 position, Vector2 direction,
      Player player) {
    return new EnemyRocketProjectile(
        new Vector3(position.x + direction.x * 1.1f, 0,
            position.y + direction.y * 1.1f),
        new Vector2(position.x + direction.x * 999,
            position.y + direction.y * 999),
        player);
  }
}
