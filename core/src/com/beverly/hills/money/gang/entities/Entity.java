package com.beverly.hills.money.gang.entities;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.beverly.hills.money.gang.screens.GameScreen;
import com.beverly.hills.money.gang.screens.game.PlayScreen;
import lombok.Getter;

public class Entity {

  @Getter
  private final PlayScreen screen;

  @Getter
  private final int entityId;

  public Entity(final PlayScreen screen) {
    this.screen = screen;
    entityId = screen.getGame().getEntMan().assignId();
  }

  public void destroy() {
    screen.getGame().getEntMan().removeEntity(entityId);
  }

  public void render3D(final ModelBatch mdlBatch, final Environment env, final float delta) {

  }

  public void tick(final float delta) {

  }

  public void onCollisionWithPlayer() {

  }

}
