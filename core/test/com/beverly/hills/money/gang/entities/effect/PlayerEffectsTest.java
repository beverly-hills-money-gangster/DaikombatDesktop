package com.beverly.hills.money.gang.entities.effect;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.beverly.hills.money.gang.entities.item.PowerUpType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PlayerEffectsTest {

  private PlayerEffects playerEffects;

  @BeforeEach
  public void setUp() {
    playerEffects = new PlayerEffects();
  }

  @Test
  public void testIsPowerUpActiveNotActive() {
    assertFalse(playerEffects.isPowerUpActive(PowerUpType.INVISIBILITY));
  }

  @Test
  public void testIsPowerUpActiveIsActive() {
    playerEffects.activatePowerUp(PowerUpType.INVISIBILITY, 1_000);
    assertTrue(playerEffects.isPowerUpActive(PowerUpType.INVISIBILITY));
  }

  @Test
  public void testIsPowerUpActiveIsDeactivated() throws InterruptedException {
    int timeoutMls = 1_000;
    playerEffects.activatePowerUp(PowerUpType.INVISIBILITY, timeoutMls);
    Thread.sleep(timeoutMls + 100);
    assertFalse(playerEffects.isPowerUpActive(PowerUpType.INVISIBILITY));
  }

}
