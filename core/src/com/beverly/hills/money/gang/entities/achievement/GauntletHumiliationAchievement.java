package com.beverly.hills.money.gang.entities.achievement;

import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import com.beverly.hills.money.gang.screens.ui.weapon.Weapon;


public class GauntletHumiliationAchievement extends Achievement {

  public GauntletHumiliationAchievement() {
    super(SoundRegistry.GAUNTLET_HUMILIATION);
  }

  @Override
  public boolean isActiveOnKill(Weapon weapon, KillStats killStats) {
    return weapon == Weapon.GAUNTLET;
  }
}
