package com.beverly.hills.money.gang.rect.filters;

import lombok.Getter;

public enum RectanglePlusFilter {
  NONE(-1), WALL(1), DOOR(2), PLAYER(3), ENEMY(4), ENEMY_PROJECTILE(5), ITEM(6);

  @Getter
  private final int value;

  RectanglePlusFilter(final int value) {
    this.value = value;
  }
}
