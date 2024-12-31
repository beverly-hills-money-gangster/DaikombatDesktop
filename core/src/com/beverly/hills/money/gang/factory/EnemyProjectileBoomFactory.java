package com.beverly.hills.money.gang.factory;

import com.badlogic.gdx.math.Vector2;
import com.beverly.hills.money.gang.entities.SoundMakingEntity;
import com.beverly.hills.money.gang.entities.player.Player;

public interface EnemyProjectileBoomFactory {

  SoundMakingEntity create(Vector2 position, Player player);
}
