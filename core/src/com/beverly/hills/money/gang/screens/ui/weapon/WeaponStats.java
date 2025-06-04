package com.beverly.hills.money.gang.screens.ui.weapon;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class WeaponStats {

  private final int delayMls;

  private final float maxDistance;

  private final Float projectileRadius;

  private final Integer maxAmmo;

}
