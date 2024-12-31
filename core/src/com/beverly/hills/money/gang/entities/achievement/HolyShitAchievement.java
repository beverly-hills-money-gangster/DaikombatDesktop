package com.beverly.hills.money.gang.entities.achievement;

import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import com.beverly.hills.money.gang.screens.ui.weapon.Weapon;

public class HolyShitAchievement extends Achievement {

  private static final int HOLY_SHIT_KILL_WINDOW_MLS = 2_999;

  public HolyShitAchievement() {
    super(SoundRegistry.HOLY_SHIT);
  }

  @Override
  public boolean isActiveOnKill(Weapon weapon, KillStats killStats) {
    return killStats.getTotalKills() == 5
        || killStats.countKillsInTimeWindow(HOLY_SHIT_KILL_WINDOW_MLS) > 2;
  }
}
