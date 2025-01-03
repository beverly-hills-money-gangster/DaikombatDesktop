package com.beverly.hills.money.gang.entities.achievement;

import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import com.beverly.hills.money.gang.screens.ui.weapon.Weapon;

public class HolyShitAchievement extends Achievement {

  public HolyShitAchievement() {
    super(SoundRegistry.HOLY_SHIT);
  }

  @Override
  public boolean isActiveOnKill(Weapon weapon, KillStats killStats) {
    return killStats.getTotalKills() == 5;
  }
}
