package com.beverly.hills.money.gang.factory;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.beverly.hills.money.gang.entities.SoundMakingEntity;
import com.beverly.hills.money.gang.entities.player.Player;
import com.beverly.hills.money.gang.entities.projectile.EnemyPlasmaBoom;

public class EnemyPlasmaProjectileBoomFactory implements EnemyProjectileBoomFactory {

  @Override
  public SoundMakingEntity create(Vector2 position, Player player) {
    return new EnemyPlasmaBoom(player,
        new Vector3(position.x, 0, position.y));
  }
}
