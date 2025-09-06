package com.beverly.hills.money.gang.screens.ui.weapon;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.beverly.hills.money.gang.assets.managers.sound.UserSettingSound;
import com.beverly.hills.money.gang.screens.ui.selection.GamePlayerClass;
import java.util.Map;
import java.util.function.Function;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Getter
@Builder
public class WeaponState {

  private final float distance;
  private final Float projectileRadius;
  @NonNull
  private final UserSettingSound fireSound;
  private final UserSettingSound hitTargetSound;
  @NonNull
  private final Map<GamePlayerClass, TextureRegion> fireTextures;
  @NonNull
  private final Map<GamePlayerClass, TextureRegion> idleTextures;
  private final int animationDelayMls;
  private final float screenRatioX;
  private final float screenRatioY;
  private final int backoffDelayMls;
  private final boolean center;
  private final Function<Long, Vector2> weaponScreenPositioning;

  private final Integer maxAmmo;
}