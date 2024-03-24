package com.beverly.hills.money.gang.entities.enemies;

import com.badlogic.gdx.math.Vector2;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;

@Getter
@Builder
public class EnemyPlayerAction {

  private final Vector2 route;
  private final Vector2 direction;
  private final EnemyPlayerActionType enemyPlayerActionType;
  @Default
  private final Runnable onComplete = () -> {
  };

}
