package com.beverly.hills.money.gang.models;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import lombok.Getter;
import lombok.Setter;

public class ModelInstanceBB extends ModelInstance {

  @Getter
  private final Vector3 center = new Vector3(); // for sphere
  @Getter
  private final Vector3 dimensions = new Vector3(); // for sphere
  @Getter
  private final float radius; // for sphere
  @Getter
  private final BoundingBox renderBox = new BoundingBox();

  @Setter
  @Getter
  private boolean isInFrustum;

  public ModelInstanceBB(final Model model) {
    super(model);

    calculateTransforms();
    calculateBoundingBox(renderBox);
    renderBox.mul(transform);

    renderBox.getCenter(center);
    renderBox.getDimensions(dimensions);
    radius = dimensions.len() / 2f;
  }

}
