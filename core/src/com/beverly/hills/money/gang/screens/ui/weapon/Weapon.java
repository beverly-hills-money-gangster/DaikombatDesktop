package com.beverly.hills.money.gang.screens.ui.weapon;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Weapon {
  GAUNTLET(false, null),
  SHOTGUN(false, null),
  RAILGUN(false, null),
  MINIGUN(true, null),
  ROCKET_LAUNCHER(false, WeaponProjectile.ROCKET),
  PLASMAGUN(true, WeaponProjectile.PLASMA);

  @Getter
  private final boolean automatic;

  @Getter
  private final WeaponProjectile projectileRef;

  public static Weapon getWeaponForProjectile(WeaponProjectile weaponProjectile) {
    return Arrays.stream(Weapon.values()).filter(
        weapon -> weapon.getProjectileRef() == weaponProjectile).findFirst().get();
  }

  // TODO test that only available weapons switch
  public Weapon nextWeapon(Set<Weapon> availableWeapons) {
    var weaponsForSwitching = getWeaponsForSwitching(availableWeapons);
    var index = getWeaponIndex(weaponsForSwitching, this);
    return weaponsForSwitching.get((index + 1) % weaponsForSwitching.size());
  }

  private List<Weapon> getWeaponsForSwitching(Set<Weapon> availableWeapons) {
    return availableWeapons.stream()
        .sorted(Comparator.comparingInt(Enum::ordinal))
        .collect(Collectors.toList());
  }

  private int getWeaponIndex(List<Weapon> weapons, Weapon weapon) {
    for (int i = 0; i < weapons.size(); i++) {
      if (weapons.get(i) == weapon) {
        return i;
      }
    }
    throw new IllegalStateException("Can't find weapon " + weapon);
  }


  public boolean hasProjectile() {
    return projectileRef != null;
  }

  public Weapon prevWeapon(Set<Weapon> availableWeapons) {
    var weaponsForSwitching = getWeaponsForSwitching(availableWeapons);
    var index = getWeaponIndex(weaponsForSwitching, this);
    int prevIndex = index - 1;
    if (prevIndex < 0) {
      prevIndex = weaponsForSwitching.size() - 1;
    }
    return weaponsForSwitching.get(prevIndex % weaponsForSwitching.size());
  }


}
