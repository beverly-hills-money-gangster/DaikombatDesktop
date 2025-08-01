package com.beverly.hills.money.gang.entities.projectile;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.beverly.hills.money.gang.animation.Animation;
import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import com.beverly.hills.money.gang.assets.managers.registry.TexturesRegistry;
import com.beverly.hills.money.gang.entities.player.Player;
import com.beverly.hills.money.gang.screens.GameScreen;
import com.beverly.hills.money.gang.screens.game.PlayScreen;
import com.beverly.hills.money.gang.screens.ui.weapon.WeaponProjectile;
import com.beverly.hills.money.gang.screens.ui.weapon.WeaponState;

public class RocketProjectile extends AbstractPlayerProjectile {

  public RocketProjectile(final Player player, final Vector3 startPosition,
      final Vector2 finishPosition,
      final PlayScreen screen,
      final WeaponState weaponState) {
    super(player, startPosition, finishPosition, screen, screen.getGame().getAssMan()
            .getTextureRegion(TexturesRegistry.FIREBALL, 0, 0, 11, 11), Animation.builder()
            .animationStepMls(50)
            .width(100).height(99).texturesRegistry(TexturesRegistry.BOOM_SPRITES).build(),
        weaponState, WeaponProjectile.ROCKET, SoundRegistry.ROCKET_BOOM);
  }
}
