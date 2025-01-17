package com.beverly.hills.money.gang.entities.projectile;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.beverly.hills.money.gang.assets.managers.registry.TexturesRegistry;
import com.beverly.hills.money.gang.entities.player.Player;

public class EnemyPlasmaProjectile extends AbstractEnemyProjectileFlying {

  public EnemyPlasmaProjectile(final Vector3 startPosition,
      final Vector2 finishPosition,
      final Player player) {
    super(startPosition, finishPosition, player, player.getScreen().getGame().getAssMan()
        .getTextureRegion(TexturesRegistry.PLASMA, 0, 0, 11, 11));
  }

}
