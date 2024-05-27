package com.beverly.hills.money.gang.entities.effect;

public class PlayerEffects {

  private long quadDamageUntil;

  private long invisibleUntil;

  public boolean isQuadDamageEffectActive() {
    return System.currentTimeMillis() < quadDamageUntil;
  }

  public void quadDamage(int quadDamageTimeout) {
    quadDamageUntil = System.currentTimeMillis() + quadDamageTimeout;
  }

  public void invisible(int invisibleTimeout) {
    invisibleUntil = System.currentTimeMillis() + invisibleTimeout;
  }

  public boolean isInvisibilityEffectActive() {
    return System.currentTimeMillis() < invisibleUntil;
  }
}
