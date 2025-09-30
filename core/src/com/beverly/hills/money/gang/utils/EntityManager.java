package com.beverly.hills.money.gang.utils;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.utils.Array;
import com.beverly.hills.money.gang.entities.Entity;
import com.beverly.hills.money.gang.screens.GameScreen;

public class EntityManager {

  public final Array<Entity> entities = new Array<>();
  private int nextId;
  private GameScreen screen;

  public void addEntity(final Entity ent) {
    entities.add(ent);
  }

  public int assignId() {
    nextId++;
    return nextId - 1;
  }

  public Entity getEntityFromId(final int id) {
    for (int i = 0; i < entities.size; i++) {
      if (entities.get(i).getEntityId() == id) {
        return entities.get(i);
      }
    }

    return null;
  }

  public GameScreen getScreen() {
    return screen;
  }

  public void setScreen(final GameScreen screen) {
    this.screen = screen;
  }

  public void removeEntity(final int id) {
    int indexToRemove = -1;
    for (int i = 0; i < entities.size; i++) {
      if (id == entities.get(i).getEntityId()) {
        indexToRemove = i;
        break;
      }
    }
    if (indexToRemove >= 0) {
      entities.removeIndex(indexToRemove);
    }
  }

  public void render3DAllEntities(final ModelBatch mdlBatch, final Environment env,
      final float delta) {
    for (final Entity ent : entities) {
      ent.render3D(mdlBatch, env, delta);
    }
  }

  public void tickAllEntities(final float delta) {
    for (final Entity ent : entities) {
      ent.tick(delta);
    }
  }
}
