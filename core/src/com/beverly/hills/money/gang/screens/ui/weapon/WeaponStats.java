package com.beverly.hills.money.gang.screens.ui.weapon;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class WeaponStats {

  private final int delayMls;

  private final float maxDistance;

  private final Float projectileRadius;

}
