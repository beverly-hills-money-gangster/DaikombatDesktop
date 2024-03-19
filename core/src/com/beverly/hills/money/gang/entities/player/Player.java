package com.beverly.hills.money.gang.entities.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.beverly.hills.money.gang.Configs;
import com.beverly.hills.money.gang.Constants;
import com.beverly.hills.money.gang.entities.Entity;
import com.beverly.hills.money.gang.entities.enemies.EnemyPlayer;
import com.beverly.hills.money.gang.rect.RectanglePlus;
import com.beverly.hills.money.gang.rect.filters.RectanglePlusFilter;
import com.beverly.hills.money.gang.screens.GameScreen;
import com.beverly.hills.money.gang.screens.ui.selection.UserSettingsUISelection;
import com.beverly.hills.money.gang.screens.ui.weapon.ScreenWeapon;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.stream.Streams;

public class Player extends Entity {

  @Getter
  private long deathTimeMls;

  @Getter
  private final PerspectiveCamera playerCam;
  @Getter
  private final RectanglePlus rect;

  private final Consumer<Player> onMovementListener;

  private final Consumer<PlayerWeapon> onAttackListener;

  private final Consumer<EnemyPlayer> onEnemyAim;
  private final Vector3 movementDir = new Vector3();
  final Vector2 movementDirVec2 = new Vector2(movementDir.x, movementDir.z);

  public boolean gotHit = false;
  public boolean renderBloodOverlay = false;
  public float bloodOverlayAlpha = Constants.BLOOD_OVERLAY_ALPHA_MIN;

  @Getter
  private String killedBy;

  private final AtomicBoolean isDead = new AtomicBoolean(false);

  @Getter
  private float weaponY;

  private final ScreenWeapon screenWeapon;


  private float camY = Constants.DEFAULT_PLAYER_CAM_Y;
  private boolean headbob = false;
  private int currentHP = 100;


  public Player(final GameScreen screen,
      final Consumer<PlayerWeapon> onAttackListener,
      final Consumer<EnemyPlayer> onEnemyAim,
      final Consumer<Player> onMovementListener,
      final Vector2 spawnPosition,
      final Vector2 lookAt) {
    super(screen);
    screenWeapon = new ScreenWeapon(screen.getGame().getAssMan());
    this.onMovementListener = onMovementListener;
    this.onAttackListener = onAttackListener;
    this.onEnemyAim = onEnemyAim;

    playerCam = new PerspectiveCamera(70, 640, 480);
    playerCam.position.set(new Vector3(0, Constants.DEFAULT_PLAYER_CAM_Y, 0));
    playerCam.lookAt(new Vector3(lookAt.x, Constants.DEFAULT_PLAYER_CAM_Y, lookAt.y));
    playerCam.near = 0.01f;
    playerCam.far = 10f;
    playerCam.update();

    rect = new RectanglePlus(
        spawnPosition.x + Constants.HALF_UNIT - Constants.PLAYER_RECT_SIZE / 2f,
        spawnPosition.y + Constants.HALF_UNIT - Constants.PLAYER_RECT_SIZE / 2f,
        Constants.PLAYER_RECT_SIZE, Constants.PLAYER_RECT_SIZE, getEntityId(),
        RectanglePlusFilter.PLAYER);
    rect.getOldPosition().set(spawnPosition.x, spawnPosition.y);
    rect.getNewPosition()
        .set(spawnPosition.x, spawnPosition.y); // Needed for spawning at correct position.
    screen.getGame().getRectMan().addRect(rect); // never forget!
    playerCam.position.set(rect.x + rect.width / 2f, Constants.DEFAULT_PLAYER_CAM_Y,
        rect.y + rect.height / 2f);
  }

  public Vector2 getCurrent2DDirection() {
    return new Vector2(this.playerCam.direction.x, this.playerCam.direction.z);
  }

  public Vector2 getCurrent2DPosition() {
    return new Vector2(this.rect.x, this.rect.y);
  }

  @Override
  public void destroy() {
    getScreen().getGame().getRectMan().removeRect(rect);
    super.destroy(); // should be last.
  }

  public int getCurrentHP() {
    return currentHP;
  }


  public final boolean getEnemyRectInRangeFromCam(final Consumer<EnemyPlayer> onEnemyIntersect,
      final float weaponDistance) {
    AtomicBoolean intersected = new AtomicBoolean(false);
    Streams.of(getScreen().getGame().getRectMan().getRects())
        .filter(rect -> (rect.getFilter() == RectanglePlusFilter.ENEMY
            || rect.getFilter() == RectanglePlusFilter.WALL))
        .filter(rect -> Intersector.intersectSegmentRectangle(playerCam.position.x,
            playerCam.position.z,
            playerCam.position.x + playerCam.direction.x * weaponDistance,
            playerCam.position.z + playerCam.direction.z * weaponDistance, rect))
        .min((o1, o2) -> Float.compare(distToPlayer(o1), distToPlayer(o2)))
        .ifPresent(closestRect -> {
          if (closestRect.getFilter() == RectanglePlusFilter.ENEMY) {
            EnemyPlayer enemy = (EnemyPlayer) getScreen().getGame().getEntMan()
                .getEntityFromId(closestRect.getConnectedEntityId());
            if (enemy != null) {
              intersected.set(true);
              onEnemyIntersect.accept(enemy);
            }
          }
        });
    return intersected.get();
  }

  private float distToPlayer(final RectanglePlus rect) {
    return Vector2.dst2(playerCam.position.x, playerCam.position.z,
        rect.x + rect.getWidth() / 2, rect.y + rect.getHeight() / 2);
  }

  public void handleInput(final float delta) {

    movementDir.setZero();

    if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)
        || Gdx.input.isKeyJustPressed(Input.Keys.ALT_RIGHT)) {
      attack(ScreenWeapon.Weapon.SHOTGUN);
    } else if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)
        || Gdx.input.isKeyJustPressed(Input.Keys.CONTROL_RIGHT)) {
      attack(ScreenWeapon.Weapon.GAUNTLET);
    }
    // otherwise the screen goes 180 degrees on startup if you don't move the mouse on main menu screens
    if (Math.abs(Gdx.input.getDeltaX()) < 500) {
      playerCam.rotate(Vector3.Y, Gdx.input.getDeltaX() * -Constants.MOUSE_CAMERA_ROTATION_SPEED
          * UserSettingsUISelection.MOUSE_SENS.getState().getNormalized() * delta);
    }
    handleArrows();
    handleWASD();
    if (headbob) {
      onMovementListener.accept(this);
      camY = Constants.DEFAULT_PLAYER_CAM_Y;
      final float sinOffset = (float) (
          Math.sin(getScreen().getGame().getTimeSinceLaunch() * Configs.PLAYER_MOVE_SPEED * 4f)
              * 0.01875f);
      camY += sinOffset;
      weaponY = -25f;
      weaponY += sinOffset * 200f * 3f;
      headbob = false;
    }

    movementDirVec2.set(movementDir.x, movementDir.z);
    rect.getNewPosition().set(
        rect.getPosition(new Vector2()).cpy()
            .add(movementDirVec2.nor().cpy().scl(Configs.PLAYER_MOVE_SPEED * delta)));
  }

  private void attack(ScreenWeapon.Weapon weapon) {
    if (screenWeapon.attack(weapon, Constants.DEFAULT_SFX_VOLUME)) {
      onAttackListener.accept(PlayerWeapon
          .builder().player(this).weapon(weapon).build());
    }
  }

  public void playWeaponHitSound(ScreenWeapon.Weapon weapon) {
    screenWeapon.registerHit(weapon, Constants.DEFAULT_SFX_VOLUME * 1.5f);
  }

  private void handleWASD() {
    if (Gdx.input.isKeyPressed(Input.Keys.W)) {
      movementDir.add(playerCam.direction.cpy());
      headbob = true;
    }
    if (Gdx.input.isKeyPressed(Input.Keys.S)) {
      movementDir.sub(playerCam.direction.cpy());
      headbob = true;
    }
    if (Gdx.input.isKeyPressed(Input.Keys.A)) {
      movementDir.sub(playerCam.direction.cpy().crs(playerCam.up));
      headbob = true;
    }
    if (Gdx.input.isKeyPressed(Input.Keys.D)) {
      movementDir.add(playerCam.direction.cpy().crs(playerCam.up));
      headbob = true;
    }
  }

  private void handleArrows() {
    if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
      playerCam.rotate(Vector3.Y, Constants.ARROWS_CAMERA_ROTATION);
    } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
      playerCam.rotate(Vector3.Y, -Constants.ARROWS_CAMERA_ROTATION);
    }
  }

  public void getHit(final int newHp) {
    if (newHp <= 0) {
      throw new IllegalArgumentException("New HP can't be less or equal to zero");
    }
    this.currentHP = newHp;
    gotHit = true;
  }

  public void buffHp(final int newHp) {
    if (newHp <= 0) {
      throw new IllegalArgumentException("New HP can't be less or equal to zero");
    }
    this.currentHP = newHp;
  }

  public void die(final String killedBy) {
    this.currentHP = 0;
    this.killedBy = killedBy;
    gotHit = true;
    isDead.set(true);
    this.deathTimeMls = System.currentTimeMillis();
  }

  public ScreenWeapon.WeaponRenderData getActiveWeaponRenderingData() {
    return screenWeapon.getActiveWeaponForRendering();
  }

  public float getWeaponDistance(ScreenWeapon.Weapon weapon) {
    return screenWeapon.getWeaponDistance(weapon);
  }

  public boolean isDead() {
    return isDead.get();
  }

  @Override
  public void tick(final float delta) {
    if (isDead.get()) {
      return;
    }
    getEnemyRectInRangeFromCam(onEnemyAim, Configs.SHOOTING_DISTANCE);
    if (gotHit) {
      renderBloodOverlay = true;
      bloodOverlayAlpha = Constants.BLOOD_OVERLAY_ALPHA_MAX;
      gotHit = false;
    }

    if (renderBloodOverlay) {
      bloodOverlayAlpha -= delta * Constants.BLOOD_OVERLAY_ALPHA_SPEED;

      if (bloodOverlayAlpha <= Constants.BLOOD_OVERLAY_ALPHA_MIN) {
        renderBloodOverlay = false;
      }
    }

    getScreen().checkOverlaps(rect);
    playerCam.position.set(rect.x + rect.width / 2f, camY, rect.y + rect.height / 2f);
    rect.getOldPosition().set(rect.x, rect.y);
  }


  @Builder
  @Getter
  public static class PlayerWeapon {

    private final Player player;
    private final ScreenWeapon.Weapon weapon;
  }

}
