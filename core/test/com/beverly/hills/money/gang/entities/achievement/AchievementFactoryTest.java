package com.beverly.hills.money.gang.entities.achievement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import com.beverly.hills.money.gang.screens.ui.weapon.Weapon;
import org.junit.jupiter.api.Test;

public class AchievementFactoryTest {

  @Test
  public void testCreateNoAchievements() {
    assertTrue(AchievementFactory.create(Weapon.SHOTGUN, new KillStats()).isEmpty());
  }

  @Test
  public void testCreateGauntletHumiliation() {
    var achievement = AchievementFactory.create(Weapon.GAUNTLET, new KillStats());
    assertTrue(achievement.isPresent());
    assertEquals(GauntletHumiliationAchievement.class, achievement.get().getClass());
    assertEquals(SoundRegistry.GAUNTLET_HUMILIATION, achievement.get().getSound());
  }

  @Test
  public void testCreateAccuracy() {
    var achievement = AchievementFactory.create(Weapon.RAILGUN, new KillStats());
    assertTrue(achievement.isPresent());
    assertEquals(AccuracyAchievement.class, achievement.get().getClass());
    assertEquals(SoundRegistry.ACCURACY, achievement.get().getSound());
  }

  @Test
  public void testCreateHolyShit() {
    KillStats killStats = new KillStats();
    killStats.registerKill();
    killStats.registerKill();
    killStats.registerKill();
    killStats.registerKill();
    killStats.registerKill();
    var achievement = AchievementFactory.create(Weapon.SHOTGUN, killStats);
    assertTrue(achievement.isPresent());
    assertEquals(HolyShitAchievement.class, achievement.get().getClass());
    assertEquals(SoundRegistry.HOLY_SHIT, achievement.get().getSound());
  }

  @Test
  public void testCreateDoubleKill() {
    KillStats killStats = new KillStats();
    killStats.registerKill();
    killStats.registerKill();
    var achievement = AchievementFactory.create(Weapon.SHOTGUN, killStats);
    assertTrue(achievement.isPresent());
    assertEquals(DoubleKillAchievement.class, achievement.get().getClass());
    assertEquals(SoundRegistry.TWO_FRAGS_TO_SEC, achievement.get().getSound());
  }

  @Test
  public void testCreateNoDoubleKill() throws InterruptedException {
    KillStats killStats = new KillStats();
    killStats.registerKill();
    Thread.sleep(3_500);
    killStats.registerKill();
    assertTrue(AchievementFactory.create(Weapon.SHOTGUN, killStats).isEmpty(),
        "Should be no achievement because more than 2 seconds passed since last kill");
  }

}
