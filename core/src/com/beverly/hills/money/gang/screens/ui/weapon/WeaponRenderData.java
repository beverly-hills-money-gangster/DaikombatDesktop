package com.beverly.hills.money.gang.screens.ui.weapon;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class WeaponRenderData {

  private final float distance;
  private final Vector2 positioning;
  private final TextureRegion textureRegion;
  private final float screenRatioX;
  private final float screenRatioY;
  private final boolean center;
}
