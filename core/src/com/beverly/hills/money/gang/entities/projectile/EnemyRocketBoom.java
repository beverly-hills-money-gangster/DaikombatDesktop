package com.beverly.hills.money.gang.entities.projectile;

import com.badlogic.gdx.math.Vector3;
import com.beverly.hills.money.gang.animation.Animation;
import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import com.beverly.hills.money.gang.assets.managers.registry.TexturesRegistry;
import com.beverly.hills.money.gang.entities.player.Player;

public class EnemyRocketBoom extends AbstractEnemyProjectileBoom {

  public EnemyRocketBoom(final Player player,
      final Vector3 position) {
    super(player, position, Animation.builder()
            .animationSteps(5).animationStepMls(50)
            .width(100).height(99)
            .texturesRegistry(TexturesRegistry.BOOM_SPRITES).build(),
        SoundRegistry.ROCKET_BOOM);
  }
}