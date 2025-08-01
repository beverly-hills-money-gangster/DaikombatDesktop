package com.beverly.hills.money.gang.entities.projectile;

import com.badlogic.gdx.math.Vector3;
import com.beverly.hills.money.gang.animation.Animation;
import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import com.beverly.hills.money.gang.assets.managers.registry.TexturesRegistry;
import com.beverly.hills.money.gang.entities.player.Player;

public class EnemyPlasmaBoom extends AbstractEnemyProjectileBoom {

  public EnemyPlasmaBoom(final Player player,
      final Vector3 position) {
    super(player, position, Animation.builder()
            .animationStepMls(50)
            .width(100).height(99)
            .texturesRegistry(TexturesRegistry.PLASMA_BOOM_SPRITES).build(),
        SoundRegistry.PLASMA_BOOM);
  }
}