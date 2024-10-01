package com.beverly.hills.money.gang.entities.achievement;

import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import com.beverly.hills.money.gang.screens.ui.weapon.Weapon;

public class AccuracyAchievement extends Achievement {

  public AccuracyAchievement() {
    // TODO use accuracy
    super(SoundRegistry.ACCURACY);
  }

  @Override
  public boolean isActiveOnKill(Weapon weapon, KillStats killStats) {
    return weapon == Weapon.RAILGUN;
  }
}
