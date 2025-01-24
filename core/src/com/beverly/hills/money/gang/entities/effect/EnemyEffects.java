package com.beverly.hills.money.gang.entities.effect;

import lombok.Getter;

public class EnemyEffects extends PlayerEffects {

  private long beingAttackedUntilMls;

  @Getter
  private long beingSpawnedUntilMls;

  public void beingAttacked(long until) {
    this.beingAttackedUntilMls = until;
  }

  public boolean isBeingAttacked() {
    return System.currentTimeMillis() < beingAttackedUntilMls;
  }

  public void beingSpawned(long until) {
    this.beingSpawnedUntilMls = until;
  }

  public boolean isBeingSpawned() {
    return System.currentTimeMillis() < beingSpawnedUntilMls;
  }

}
