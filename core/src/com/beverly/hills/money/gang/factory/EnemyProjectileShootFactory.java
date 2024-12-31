package com.beverly.hills.money.gang.factory;

import com.badlogic.gdx.math.Vector2;
import com.beverly.hills.money.gang.entities.player.Player;
import com.beverly.hills.money.gang.entities.projectile.Projectile;

public interface EnemyProjectileShootFactory {

  Projectile create(Vector2 position, Vector2 direction, Player player);

}
