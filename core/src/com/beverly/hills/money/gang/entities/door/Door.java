package com.beverly.hills.money.gang.entities.door;


import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.beverly.hills.money.gang.configs.Constants;
import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import com.beverly.hills.money.gang.assets.managers.sound.UserSettingSound;
import com.beverly.hills.money.gang.entities.SoundMakingEntity;
import com.beverly.hills.money.gang.entities.enemies.EnemyPlayer;
import com.beverly.hills.money.gang.entities.player.Player;
import com.beverly.hills.money.gang.models.ModelInstanceBB;
import com.beverly.hills.money.gang.rect.RectanglePlus;
import com.beverly.hills.money.gang.rect.filters.RectanglePlusFilter;
import com.beverly.hills.money.gang.screens.game.PlayScreen;
import lombok.Getter;

public class Door extends SoundMakingEntity {

  public final Vector3 position = new Vector3();

  private final ModelInstanceBB mdlInstDoor;

  private final DoorDirection direction;


  @Getter
  private DoorState state = DoorState.CLOSE;
  private final Vector3 openPos = new Vector3();
  private final Vector3 closedPos = new Vector3();

  private static final float START_OPEN_DISTANCE = 1.75f;

  private static final float DOOR_OPEN_SPEED = 8f;

  private final UserSettingSound soundClose;

  private final UserSettingSound soundOpen;

  private RectanglePlus rect;

  private final Vector3 currentTranslation = new Vector3();


  public Door(final Vector3 position, final DoorDirection direction,
      final PlayScreen screen) {
    super(screen);
    this.soundOpen = screen.getGame().getAssMan().getUserSettingSound(SoundRegistry.DOOR_OPEN);
    this.soundClose = screen.getGame().getAssMan().getUserSettingSound(SoundRegistry.DOOR_CLOSE);
    this.position.set(position.cpy().add(Constants.HALF_UNIT, Constants.HALF_UNIT, 0));
    this.direction = direction;
    mdlInstDoor = new ModelInstanceBB(screen.getCellBuilder().getMdlDoor());
    switch (direction) {
      case NORTH ->
          closedPos.set(this.position.cpy().add(new Vector3(0, -0.25f, 1 - Constants.PPU * 2)));
      case EAST -> closedPos.set(this.position.cpy()
          .add(
              new Vector3(Constants.HALF_UNIT - Constants.PPU * 2, -0.25f, Constants.HALF_UNIT)));
      case WEST -> closedPos.set(this.position.cpy()
          .add(new Vector3(-Constants.HALF_UNIT + Constants.PPU * 2, -0.25f,
              Constants.HALF_UNIT)));
      case SOUTH ->
          closedPos.set(this.position.cpy().add(new Vector3(0, -0.25f, 0 + Constants.PPU * 2)));
    }

    openPos.set(closedPos.cpy().add(0, -1.5f, 0));

    mdlInstDoor.transform.setToTranslation(closedPos.cpy());

    if (direction == DoorDirection.WEST || direction == DoorDirection.EAST) {
      mdlInstDoor.transform.rotate(Vector3.Y, -90);
    }
    mdlInstDoor.transform.scale(1, 1.5f, 1);

    setupRect();

    screen.getGame().getRectMan().addRect(rect);
  }

  @Override
  public void destroy() {
    if (rect != null) {
      getScreen().getGame().getRectMan().removeRect(rect);
    }
    super.destroy(); // should be last.
  }


  @Override
  public void render3D(final ModelBatch mdlBatch, final Environment env, final float delta) {
    mdlInstDoor.setInFrustum(getScreen().frustumCull(getScreen().getCurrentCam(), mdlInstDoor));
    if (mdlInstDoor.isInFrustum()) {
      mdlBatch.render(mdlInstDoor, env);
    }
  }

  private void setupRect() {
    if (direction == DoorDirection.NORTH || direction == DoorDirection.SOUTH) {
      rect = new RectanglePlus(
          mdlInstDoor.transform.getTranslation(new Vector3()).x
              - mdlInstDoor.getRenderBox().getWidth() / 2,
          mdlInstDoor.transform.getTranslation(new Vector3()).z
              - mdlInstDoor.getRenderBox().getDepth() / 2,
          mdlInstDoor.getRenderBox().getWidth(), mdlInstDoor.getRenderBox().getDepth(),
          getEntityId(),
          RectanglePlusFilter.DOOR);
    } else {
      rect = new RectanglePlus(
          mdlInstDoor.transform.getTranslation(new Vector3()).x
              - mdlInstDoor.getRenderBox().getDepth() / 2,
          mdlInstDoor.transform.getTranslation(new Vector3()).z
              - mdlInstDoor.getRenderBox().getWidth() / 2,
          mdlInstDoor.getRenderBox().getDepth(), mdlInstDoor.getRenderBox().getWidth(),
          getEntityId(),
          RectanglePlusFilter.DOOR);
    }
  }

  @Override
  public void tick(final float delta) {
    var targetState = getTargetState();
    if (targetState == state) {
      return;
    }
    if (targetState == DoorState.CLOSE) {
      if (state == DoorState.TO_BE_CLOSED) {
        soundClose.play(getSFXVolume(), getSFXPan());
        state = DoorState.BEING_CLOSED;
      } else if (state != DoorState.BEING_CLOSED) {
        state = DoorState.TO_BE_CLOSED;
      }
    } else if (targetState == DoorState.OPEN) {
      if (state == DoorState.TO_BE_OPEN) {
        soundOpen.play(getSFXVolume(), getSFXPan());
        state = DoorState.BEING_OPEN;
      } else if (state != DoorState.BEING_OPEN) {
        state = DoorState.TO_BE_OPEN;
      }
    }
    if (state == DoorState.BEING_OPEN) {
      mdlInstDoor.transform.getTranslation(currentTranslation);
      currentTranslation.y -= DOOR_OPEN_SPEED * delta;
      mdlInstDoor.transform.setTranslation(currentTranslation.cpy());

      if (currentTranslation.y <= openPos.y) {
        mdlInstDoor.transform.setTranslation(openPos.cpy());
        state = DoorState.OPEN;
      }
    } else if (state == DoorState.BEING_CLOSED) {
      mdlInstDoor.transform.getTranslation(currentTranslation);
      currentTranslation.y += DOOR_OPEN_SPEED * delta;
      mdlInstDoor.transform.setTranslation(currentTranslation.cpy());

      if (currentTranslation.y >= closedPos.y) {
        mdlInstDoor.transform.setTranslation(closedPos.cpy());
        state = DoorState.CLOSE;
      }
    }

    rect.setPosition(mdlInstDoor.transform.getTranslation(new Vector3()).x
            - mdlInstDoor.getRenderBox().getWidth() / 2,
        mdlInstDoor.transform.getTranslation(new Vector3()).z
            - mdlInstDoor.getRenderBox().getDepth() / 2);
  }

  private DoorState getTargetState() {
    for (final RectanglePlus otherRect : getScreen().getGame().getRectMan().getRects()) {
      if (otherRect != rect
          && (otherRect.getFilter() == RectanglePlusFilter.PLAYER
          || otherRect.getFilter() == RectanglePlusFilter.PROJECTILE
          || otherRect.getFilter() == RectanglePlusFilter.ENEMY) &&
          new Vector2(position.x, position.z).dst(otherRect.getPosition(new Vector2()))
              < START_OPEN_DISTANCE) {
        if (otherRect.getFilter() == RectanglePlusFilter.ENEMY) {
          var enemyPlayer = (EnemyPlayer) getScreen().getGame().getEntMan()
              .getEntityFromId(otherRect.getConnectedEntityId());
          if (!enemyPlayer.isVisible()) {
            continue;
          }
        }
        return DoorState.OPEN;
      }
    }
    return DoorState.CLOSE;
  }

  @Override
  protected Player getPlayer() {
    return getScreen().getPlayer();
  }

  @Override
  protected RectanglePlus getRect() {
    return rect;
  }


  public enum DoorState {
    OPEN, CLOSE, TO_BE_OPEN, TO_BE_CLOSED, BEING_CLOSED, BEING_OPEN
  }

  public enum DoorDirection {
    NORTH, SOUTH, EAST, WEST
  }

}