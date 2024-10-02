package com.beverly.hills.money.gang.entities.achievement;

import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import com.beverly.hills.money.gang.screens.ui.weapon.Weapon;

public class DoubleKillAchievement extends Achievement {

  private static final int DOUBLE_KILL_WINDOW_MLS = 2_999;

  public DoubleKillAchievement() {
    super(SoundRegistry.TWO_FRAGS_TO_SEC);
  }

  @Override
  public boolean isActiveOnKill(Weapon weapon, KillStats killStats) {
    return killStats.countKillsInTimeWindow(DOUBLE_KILL_WINDOW_MLS) == 2;
  }
}
