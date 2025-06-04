package com.beverly.hills.money.gang.screens.ui.weapon;


import lombok.Getter;

public class WeaponAmmo {

  public WeaponAmmo(int maxAmmo) {
    this.maxAmmo = maxAmmo;
    this.currentAmmo = maxAmmo;
  }

  @Getter
  private final int maxAmmo;
  @Getter
  private int currentAmmo;

  public void wasteAmmo() {
    if (!hasAmmo()) {
      return;
    }
    currentAmmo--;
  }

  public boolean hasAmmo() {
    return currentAmmo > 0;
  }

  @Override
  public String toString() {
    return currentAmmo + " AMMO";
  }

}
