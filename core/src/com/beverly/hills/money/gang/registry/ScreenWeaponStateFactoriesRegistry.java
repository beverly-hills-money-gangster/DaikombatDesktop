package com.beverly.hills.money.gang.registry;

import com.beverly.hills.money.gang.factory.GauntletWeaponStateFactory;
import com.beverly.hills.money.gang.factory.MinigunWeaponStateFactory;
import com.beverly.hills.money.gang.factory.PlasmagunWeaponStateFactory;
import com.beverly.hills.money.gang.factory.RailgunWeaponStateFactory;
import com.beverly.hills.money.gang.factory.RocketLauncherWeaponStateFactory;
import com.beverly.hills.money.gang.factory.ScreenWeaponStateFactory;
import com.beverly.hills.money.gang.factory.ShotgunWeaponStateFactory;
import com.beverly.hills.money.gang.screens.ui.weapon.Weapon;
import java.util.HashMap;
import java.util.Map;

public class ScreenWeaponStateFactoriesRegistry {

  private final Map<Weapon, ScreenWeaponStateFactory> factories = new HashMap<>();

  {
    for (Weapon value : Weapon.values()) {
      ScreenWeaponStateFactory factory = switch (value) {
        case SHOTGUN -> new ShotgunWeaponStateFactory();
        case ROCKET_LAUNCHER -> new RocketLauncherWeaponStateFactory();
        case GAUNTLET -> new GauntletWeaponStateFactory();
        case MINIGUN -> new MinigunWeaponStateFactory();
        case RAILGUN -> new RailgunWeaponStateFactory();
        case PLASMAGUN -> new PlasmagunWeaponStateFactory();
      };
      factories.put(value, factory);
    }
  }

  public ScreenWeaponStateFactory get(final Weapon weapon) {
    return factories.get(weapon);
  }

}
