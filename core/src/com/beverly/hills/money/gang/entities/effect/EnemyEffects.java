package com.beverly.hills.money.gang.entities.effect;

public class EnemyEffects extends PlayerEffects {

  private long redUntil;

  public void beingAttacked(long until) {
    this.redUntil = until;
  }

  public boolean isBeingAttacked() {
    return System.currentTimeMillis() < redUntil;
  }

}
