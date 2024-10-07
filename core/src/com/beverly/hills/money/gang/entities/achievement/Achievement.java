package com.beverly.hills.money.gang.entities.achievement;

import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import com.beverly.hills.money.gang.screens.ui.weapon.Weapon;
import lombok.Getter;

public abstract class Achievement {

  @Getter
  private final SoundRegistry sound;

  public Achievement(SoundRegistry sound) {
    this.sound = sound;
  }

  public abstract boolean isActiveOnKill(Weapon weapon, KillStats killStats);
}
