package com.beverly.hills.money.gang.entities.achievement;

import com.beverly.hills.money.gang.screens.ui.weapon.Weapon;
import java.util.List;
import java.util.Optional;

public class AchievementFactory {

  private static final List<Achievement> achievementList = List.of(
      new AccuracyAchievement(),
      new DoubleKillAchievement(),
      new GauntletHumiliationAchievement(),
      new HolyShitAchievement());

  public static Optional<Achievement> create(Weapon weapon, KillStats killStats) {
    return achievementList.stream().filter(
            achievement -> achievement.isActiveOnKill(weapon, killStats))
        .findFirst();
  }

}
