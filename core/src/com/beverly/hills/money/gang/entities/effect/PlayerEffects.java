package com.beverly.hills.money.gang.entities.effect;

import com.beverly.hills.money.gang.entities.item.PowerUpType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;

public class PlayerEffects {

  private static final int MLS_LEFT_ABOUT_TO_FINISH = 3_000;

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

  public Intensity getPowerUpEffectIntensity(PowerUpType powerUpType) {
    return Optional.ofNullable(powerUpsInEffect.get(powerUpType))
        .filter(inEffectUntilMls -> inEffectUntilMls > System.currentTimeMillis())
        .map(inEffectUntilMls -> {
          int mlsLeft = (int) (inEffectUntilMls - System.currentTimeMillis());
          if (mlsLeft <= MLS_LEFT_ABOUT_TO_FINISH) {
            // if power up is about to end then increase the intensity
            return Intensity.HIGH;
          } else {
            return Intensity.LOW;
          }
        })
        .orElse(Intensity.NO);
  }

  public enum Intensity {
    NO(0), LOW(7), HIGH(25);

    @Getter
    private final int level;

    Intensity(int level) {
      this.level = level;
    }
  }
}
