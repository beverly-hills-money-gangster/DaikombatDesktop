package com.beverly.hills.money.gang.entities.enemies;

import com.badlogic.gdx.math.Vector2;
import com.beverly.hills.money.gang.screens.ui.weapon.Weapon;
import io.micrometer.common.lang.Nullable;
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
  @Nullable
  private final Weapon weapon;
  @NonNull
  private final Integer eventSequenceId;
  @Default
  private final Runnable onComplete = () -> {
  };

}
