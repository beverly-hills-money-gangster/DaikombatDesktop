package com.beverly.hills.money.gang.entities.enemies;

import com.badlogic.gdx.math.Vector2;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@Builder
@ToString
@EqualsAndHashCode
public class EnemyPlayerAction {

  private final Vector2 route;
  private final Vector2 direction;
  private final EnemyPlayerActionType enemyPlayerActionType;
  @NonNull
  private final Integer eventSequenceId;
  @Default
  private final Runnable onComplete = () -> {
  };

}
