package com.beverly.hills.money.gang.entities.effect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.beverly.hills.money.gang.entities.effect.PlayerEffects.Intensity;
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

  @Test
  public void testGetPowerUpEffectIntensityNotPickedUp() {
    assertEquals(Intensity.NO, playerEffects.getPowerUpEffectIntensity(PowerUpType.DEFENCE));
  }

  @Test
  public void testGetPowerUpEffectIntensityNotInEffectAnymore() throws InterruptedException {
    int timeoutMls = 1_000;
    playerEffects.activatePowerUp(PowerUpType.DEFENCE, timeoutMls);
    Thread.sleep(timeoutMls + 100);
    assertEquals(Intensity.NO, playerEffects.getPowerUpEffectIntensity(PowerUpType.DEFENCE));
  }

  @Test
  public void testGetPowerUpEffectIntensityAboutToFinish() throws InterruptedException {
    int timeoutMls = 5_000;
    playerEffects.activatePowerUp(PowerUpType.DEFENCE, timeoutMls);
    Thread.sleep(3_000);
    assertEquals(Intensity.HIGH, playerEffects.getPowerUpEffectIntensity(PowerUpType.DEFENCE));
  }

  @Test
  public void testGetPowerUpEffectIntensityNormal() {
    int timeoutMls = 5_000;
    playerEffects.activatePowerUp(PowerUpType.DEFENCE, timeoutMls);
    assertEquals(Intensity.LOW, playerEffects.getPowerUpEffectIntensity(PowerUpType.DEFENCE));
  }

  @Test
  public void testGetActivePowerUpMessageNoPowerUpInEffect() {
    assertTrue(playerEffects.getActivePowerUpMessage().isEmpty());
  }

  @Test
  public void testGetActivePowerUpMessageDefenseInEffect() {
    int timeoutMls = 5_500;
    playerEffects.activatePowerUp(PowerUpType.DEFENCE, timeoutMls);
    assertEquals("DEFENCE 5 SEC", playerEffects.getActivePowerUpMessage());
  }


  @Test
  public void testGetActivePowerUpMessageDefenceActiveDeactivate() throws InterruptedException {
    int timeoutMls = 1000;
    playerEffects.activatePowerUp(PowerUpType.DEFENCE, timeoutMls);
    Thread.sleep(timeoutMls + 150);
    assertTrue(playerEffects.getActivePowerUpMessage().isEmpty(),
        "Should be empty because defence power-up is not active anymore");
  }

}
