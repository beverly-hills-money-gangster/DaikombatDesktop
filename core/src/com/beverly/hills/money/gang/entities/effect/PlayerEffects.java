package com.beverly.hills.money.gang.entities.effect;

import com.beverly.hills.money.gang.entities.item.PowerUpType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PlayerEffects {

  private final Map<PowerUpType, Long> powerUpsInEffect = new HashMap<>();

  {
    Arrays.stream(PowerUpType.values()).forEach(
        powerUpType -> powerUpsInEffect.put(powerUpType, 0L));
  }

  public boolean isPowerUpActive(PowerUpType powerUpType) {
    return System.currentTimeMillis() < powerUpsInEffect.get(powerUpType);
  }

  public void activatePowerUp(PowerUpType powerUpType, int timeoutMls) {
    powerUpsInEffect.put(powerUpType, System.currentTimeMillis() + timeoutMls);
  }
}
