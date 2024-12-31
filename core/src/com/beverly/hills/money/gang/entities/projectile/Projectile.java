package com.beverly.hills.money.gang.entities.projectile;

import com.badlogic.gdx.math.Vector2;
import com.beverly.hills.money.gang.entities.SoundMakingEntity;
import com.beverly.hills.money.gang.screens.GameScreen;
import com.beverly.hills.money.gang.screens.ui.weapon.WeaponProjectile;

public abstract class Projectile extends SoundMakingEntity {

  public Projectile(GameScreen screen) {
    super(screen);
  }

  public abstract WeaponProjectile getProjectileType();

  public abstract Vector2 currentPosition();

}
