package com.beverly.hills.money.gang.entities.player;

import static com.badlogic.gdx.Input.Keys.NUM_0;
import static com.beverly.hills.money.gang.configs.Constants.LONG_TIME_NO_MOVE_MLS;
import static com.beverly.hills.money.gang.configs.EnvConfigs.SPEED_BOOST;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.beverly.hills.money.gang.configs.Constants;
import com.beverly.hills.money.gang.configs.EnvConfigs;
import com.beverly.hills.money.gang.configs.KeyMappings;
import com.beverly.hills.money.gang.entities.Entity;
import com.beverly.hills.money.gang.entities.door.Door;
import com.beverly.hills.money.gang.entities.door.Door.DoorState;
import com.beverly.hills.money.gang.entities.effect.PlayerEffects;
import com.beverly.hills.money.gang.entities.enemies.EnemyPlayer;
import com.beverly.hills.money.gang.entities.item.PowerUpType;
import com.beverly.hills.money.gang.entities.projectile.Projectile;
import com.beverly.hills.money.gang.entities.teleport.Teleport;
import com.beverly.hills.money.gang.rect.RectanglePlus;
import com.beverly.hills.money.gang.rect.filters.RectanglePlusFilter;
import com.beverly.hills.money.gang.registry.EnemiesRegistry;
import com.beverly.hills.money.gang.registry.PlayerProjectileFactoriesRegistry;
import com.beverly.hills.money.gang.registry.ScreenWeaponStateFactoriesRegistry;
import com.beverly.hills.money.gang.screens.game.PlayScreen;
import com.beverly.hills.money.gang.screens.ui.selection.GamePlayerClass;
import com.beverly.hills.money.gang.screens.ui.selection.UserSettingsUISelection;
import com.beverly.hills.money.gang.screens.ui.weapon.ScreenWeapon;
import com.beverly.hills.money.gang.screens.ui.weapon.Weapon;
import com.beverly.hills.money.gang.screens.ui.weapon.WeaponRenderData;
import com.beverly.hills.money.gang.screens.ui.weapon.WeaponStats;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.stream.Streams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Player extends Entity {

  private static final Logger LOG = LoggerFactory.getLogger(Player.class);

  private static final Set<RectanglePlusFilter> ENEMY_COLLISION_FILTERS
      = Set.of(RectanglePlusFilter.ENEMY, RectanglePlusFilter.DOOR, RectanglePlusFilter.WALL);

  private long lastTimeMovedMls = 0;

  private final PlayerProjectileFactoriesRegistry playerProjectileFactoriesRegistry
      = new PlayerProjectileFactoriesRegistry();

  @Getter
  private long deathTimeMls;

  @Getter
  private final PerspectiveCamera playerCam;
  @Getter
  private RectanglePlus rect;

  private final Consumer<Player> onMovementListener;

  private final Consumer<PlayerWeapon> onAttackListener;

  private final Consumer<ScreenWeapon> onNoAmmo;

  @Getter
  private final Consumer<ProjectileEnemy> onProjectileAttackHit;

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

  @Getter
  private final EnemiesRegistry enemiesRegistry;

  @Getter
  private final PlayerEffects playerEffects = new PlayerEffects();

  private float camY = Constants.DEFAULT_PLAYER_CAM_Y;
  private boolean moved = false;

  private int currentHP = 100;

  @Setter
  private Teleport colliedTeleport;

  private final Float speed;

  @Getter
  private final GamePlayerClass playerClass;


  public Player(final PlayScreen screen,
      final Consumer<PlayerWeapon> onAttackListener,
      final Consumer<EnemyPlayer> onEnemyAim,
      final Consumer<Player> onMovementListener,
      final Consumer<ProjectileEnemy> onProjectileAttackHit,
      final Vector2 spawnPosition,
      final Vector2 lookAt,
      final Float speed,
      final Map<Weapon, WeaponStats> weaponStats,
      final int maxVisibility,
      final GamePlayerClass playerClass,
      final Consumer<ScreenWeapon> onNoAmmo) {
    super(screen);
    this.playerClass = playerClass;
    this.onNoAmmo = onNoAmmo;
    this.enemiesRegistry = screen.getEnemiesRegistry();
    this.speed = speed * SPEED_BOOST;
    screenWeapon = new ScreenWeapon(screen.getGame().getAssMan(), weaponStats,
        new ScreenWeaponStateFactoriesRegistry(), playerClass);
    this.onMovementListener = onMovementListener;
    this.onProjectileAttackHit = onProjectileAttackHit;
    this.onAttackListener = onAttackListener;
    this.onEnemyAim = onEnemyAim;

    playerCam = new PerspectiveCamera(75, 640, 480);
    playerCam.position.set(new Vector3(0, Constants.DEFAULT_PLAYER_CAM_Y, 0));
    playerCam.lookAt(new Vector3(lookAt.x, Constants.DEFAULT_PLAYER_CAM_Y, lookAt.y));
    playerCam.near = 0.01f;
    playerCam.far = maxVisibility * 0.8f;
    playerCam.fieldOfView = 90;
    playerCam.update();
    createRect(spawnPosition.cpy()
        .set(spawnPosition.x,
            spawnPosition.y));

    Gdx.input.setInputProcessor(new InputAdapter() {
      @Override
      public boolean scrolled(float amountX, float amountY) {
        if (isDead.get()) {
          return true;
        }
        if (amountY > 0) {
          screenWeapon.changeToPrevWeapon();
        } else if (amountY < 0) {
          screenWeapon.changeToNextWeapon();
        }
        return true;
      }
    });
  }

  public Vector2 getCurrent2DDirection() {
    return new Vector2(this.playerCam.direction.x, this.playerCam.direction.z);
  }

  public Vector2 getCurrent2DPosition() {
    return new Vector2(
        this.playerCam.position.x,
        this.playerCam.position.z);
  }

  public void teleport(final Vector2 position, final Vector2 lookAt) {
    getScreen().getGame().getRectMan().removeRect(rect); // never forget!
    playerCam.position.set(new Vector3(0, Constants.DEFAULT_PLAYER_CAM_Y, 0));
    playerCam.lookAt(new Vector3(lookAt.x, Constants.DEFAULT_PLAYER_CAM_Y, lookAt.y));
    createRect(position.cpy().set(position.x, position.y));
    colliedTeleport.finish();
    colliedTeleport = null;
  }

  public boolean isCollidedWithTeleport() {
    return colliedTeleport != null;
  }

  private void createRect(final Vector2 position) {
    rect = new RectanglePlus(
        position.x - Constants.PLAYER_RECT_SIZE / 2,
        position.y - Constants.PLAYER_RECT_SIZE / 2,
        Constants.PLAYER_RECT_SIZE, Constants.PLAYER_RECT_SIZE, getEntityId(),
        RectanglePlusFilter.PLAYER);
    rect.getOldPosition().set(position.x, position.y);
    rect.getNewPosition()
        .set(position.x, position.y); // Needed for spawning at correct position.

    getScreen().getGame().getRectMan().addRect(rect); // never forget!
    playerCam.position.set(position.x, Constants.DEFAULT_PLAYER_CAM_Y, position.y);

  }

  @Override
  public void destroy() {
    getScreen().getGame().getRectMan().removeRect(rect);
    screenWeapon.dispose();
    super.destroy(); // should be last.
  }

  public int getCurrentHP() {
    return currentHP;
  }


  public final Optional<EnemyPlayer> getEnemyRectInRangeFromCam(
      final float weaponDistance) {
    return Streams.of(getScreen().getGame().getRectMan().getRects())
        .filter(rect -> ENEMY_COLLISION_FILTERS.contains(rect.getFilter()))
        .filter(rectanglePlus -> {
          switch (rectanglePlus.getFilter()) {
            case ENEMY -> {
              var enemy = (EnemyPlayer) getScreen().getGame().getEntMan()
                  .getEntityFromId(rectanglePlus.getConnectedEntityId());
              return enemy.isVisible();
            }
            case DOOR -> {
              var door = (Door) getScreen().getGame().getEntMan()
                  .getEntityFromId(rectanglePlus.getConnectedEntityId());
              // you can't shoot through closed doors only
              return door.getState() == DoorState.CLOSE;
            }
            case WALL -> {
              return !EnvConfigs.SHOOT_THRU_WALLS;
            }
            default -> {
              return true;
            }
          }
        })
        .filter(rect -> Intersector.intersectSegmentRectangle(playerCam.position.x,
            playerCam.position.z,
            playerCam.position.x + playerCam.direction.x * weaponDistance,
            playerCam.position.z + playerCam.direction.z * weaponDistance, rect))
        .min((o1, o2) -> Float.compare(distToPlayer(o1), distToPlayer(o2)))
        .map(closestRect -> {
          if (closestRect.getFilter() == RectanglePlusFilter.ENEMY) {
            return (EnemyPlayer) getScreen().getGame().getEntMan()
                .getEntityFromId(closestRect.getConnectedEntityId());
          }
          return null;
        });
  }


  private float distToPlayer(final RectanglePlus rect) {
    return Vector2.dst2(
        playerCam.position.x,
        playerCam.position.z,
        rect.x + rect.getWidth() / 2, rect.y + rect.getHeight() / 2);
  }

  public Weapon getCurrentWeapon() {
    return screenWeapon.getWeaponBeingUsed();
  }

  public String getCurrentWeaponAmmo() {
    return screenWeapon.getAmmoStats();
  }

  public void setWeapon(Weapon weapon) {
    screenWeapon.setWeaponBeingUsed(weapon);
  }

  public void setWeaponAmmo(Weapon weapon, int ammo) {
    screenWeapon.setWeaponAmmo(weapon, ammo);
  }

  public void handleInput(final float delta) {

    movementDir.setZero();

    if (screenWeapon.getWeaponBeingUsed().isAutomatic() &&
        (Gdx.input.isButtonPressed(Input.Buttons.LEFT)
            || Gdx.input.isKeyPressed(Input.Keys.ALT_RIGHT))) {
      attack();
    } else if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)
        || Gdx.input.isKeyJustPressed(Input.Keys.ALT_RIGHT)) {
      attack();
    } else if (Gdx.input.isButtonJustPressed(Buttons.RIGHT)
        || Gdx.input.isKeyJustPressed(Keys.CONTROL_RIGHT)) {
      punch();
    }

    if (Gdx.input.isKeyJustPressed(KeyMappings.NEXT_WEAPONS.getKey())) {
      screenWeapon.changeToNextWeapon();
    } else if (Gdx.input.isKeyJustPressed(KeyMappings.PREV_WEAPONS.getKey())) {
      screenWeapon.changeToPrevWeapon();
    }

    int weaponIndex = 0;
    for (Weapon availableWeapon : screenWeapon.getAvailableWeapons()) {
      if (Gdx.input.isKeyJustPressed(NUM_0 + weaponIndex)) {
        screenWeapon.changeWeapon(availableWeapon);
      }
      weaponIndex++;
    }

    // otherwise the screen goes 180 degrees on startup if you don't move the mouse on main menu screens
    if (Math.abs(Gdx.input.getDeltaX()) < 500) {
      var angle = Gdx.input.getDeltaX() * -Constants.MOUSE_CAMERA_ROTATION_SPEED
          * UserSettingsUISelection.MOUSE_SENS.getState().getNormalized() * delta;
      playerCam.rotate(Vector3.Y, angle);
    }
    handleArrows();
    handleWASD();

    movementDirVec2.set(movementDir.x, movementDir.z);
    rect.getNewPosition().set(
        rect.getPosition(new Vector2()).cpy()
            .add(movementDirVec2.nor().cpy().scl(speed * delta)));
  }

  private void attack() {
    if (screenWeapon.attack(this)) {
      var currentWeapon = screenWeapon.getWeaponBeingUsed();
      onAttackListener.accept(PlayerWeapon
          .builder().player(this).weapon(currentWeapon).build());

      if (currentWeapon.hasProjectile()) {
        var projectileFactory = playerProjectileFactoriesRegistry.get(
            currentWeapon.getProjectileRef());
        getScreen().getGame().getEntMan()
            .addEntity(projectileFactory.create(this, screenWeapon.getWeaponState(currentWeapon)));
      }
    } else if (!screenWeapon.hasAmmo()) {
      onNoAmmo.accept(screenWeapon);
    }
  }

  private boolean longTimeNoMove() {
    return System.currentTimeMillis() - lastTimeMovedMls > LONG_TIME_NO_MOVE_MLS;
  }

  private void punch() {
    if (screenWeapon.punch(this)) {
      onAttackListener.accept(PlayerWeapon
          .builder().player(this).weapon(screenWeapon.getWeaponBeingUsed()).build());
    }
  }

  public void playWeaponHitSound(Weapon weapon) {
    screenWeapon.registerHit(weapon);
  }

  private void handleWASD() {
    if (Gdx.input.isKeyPressed(Input.Keys.W)) {
      movementDir.add(playerCam.direction.cpy());
      moved = true;
    }
    if (Gdx.input.isKeyPressed(Input.Keys.S)) {
      movementDir.sub(playerCam.direction.cpy());
      moved = true;
    }
    if (Gdx.input.isKeyPressed(Input.Keys.A)) {
      movementDir.sub(playerCam.direction.cpy().crs(playerCam.up));
      moved = true;
    }
    if (Gdx.input.isKeyPressed(Input.Keys.D)) {
      movementDir.add(playerCam.direction.cpy().crs(playerCam.up));
      moved = true;
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
    getHit();
  }

  public void getHit() {
    gotHit = true;
  }

  public void setHP(final int newHp) {
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

  public WeaponRenderData getActiveWeaponRenderingData() {
    return screenWeapon.getActiveWeaponForRendering();
  }


  public float getAlphaChannel() {
    if (getPlayerEffects().isPowerUpActive(PowerUpType.INVISIBILITY)) {
      return 0.6f;
    } else {
      return 1f;
    }
  }

  public float getWeaponDistance(Weapon weapon) {
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
    screenWeapon.executeTask();
    var enemyOnAim = getEnemyRectInRangeFromCam(
        screenWeapon.getWeaponDistance(screenWeapon.getWeaponBeingUsed()));
    enemyOnAim.ifPresent(onEnemyAim);
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

    if (moved || longTimeNoMove()) {
      onMovementListener.accept(this);
      lastTimeMovedMls = System.currentTimeMillis();
    }
    if (moved) {
      camY = Constants.DEFAULT_PLAYER_CAM_Y;
      final float sinOffset = (float) (
          Math.sin(getScreen().getGame().getTimeSinceLaunch() * speed * 3f)
              * 0.01875f);
      camY += sinOffset;
      weaponY = -25f;
      weaponY += sinOffset * 200f * 3f;
      moved = false;
    }

    getScreen().checkOverlaps(rect);
    getScreen().getGame().getRectMan().onCollisionWithPlayer(rect);
    playerCam.position.set(rect.x + Constants.PLAYER_RECT_SIZE / 2, camY,
        rect.y + Constants.PLAYER_RECT_SIZE / 2);
    rect.getOldPosition().set(rect.x, rect.y);
  }


  @Builder
  @Getter
  public static class PlayerWeapon {

    private final Player player;
    private final Weapon weapon;
  }

  @Builder
  @Getter
  public static class ProjectileEnemy {

    private final Projectile projectile;
    private final Player player;
    private final List<EnemyPlayer> enemyPlayers;
  }

}
