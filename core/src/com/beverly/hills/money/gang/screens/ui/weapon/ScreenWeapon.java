package com.beverly.hills.money.gang.screens.ui.weapon;

import com.badlogic.gdx.math.Vector2;
import com.beverly.hills.money.gang.Constants;
import com.beverly.hills.money.gang.assets.managers.DaiKombatAssetsManager;
import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import com.beverly.hills.money.gang.assets.managers.registry.TexturesRegistry;
import com.beverly.hills.money.gang.assets.managers.sound.SoundVolumeType;
import com.beverly.hills.money.gang.assets.managers.sound.TimeLimitedSound;
import com.beverly.hills.money.gang.assets.managers.sound.UserSettingSound;
import com.beverly.hills.money.gang.entities.item.PowerUpType;
import com.beverly.hills.money.gang.entities.player.Player;
import java.util.ArrayDeque;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

public class ScreenWeapon {

  private final Map<Weapon, Long> animationStart = new HashMap<>();

  private final ScheduledExecutorService scheduledExecutor
      = Executors.newScheduledThreadPool(1,
      new BasicThreadFactory.Builder().namingPattern("weapon-change-scheduler-%d").daemon(true)
          .build());

  private final Queue<Runnable> tasks = new ArrayDeque<>();

  protected static final int CHANGE_WEAPON_DELAY_MLS = 75;

  private static final int GUNSHOT_ANIMATION_MLS = 150;

  private static final int RAILGUN_ANIMATION_MLS = 200;

  private static final int MINIGUN_ANIMATION_MLS = 100;

  private static final int PUNCH_ANIMATION_MLS = 155;

  private final TimeLimitedSound quadDamageAttack;

  private final UserSettingSound weaponChangeSound;

  private long weaponChangedLastTimeMls;

  final Map<Weapon, WeaponState> weaponStates = new EnumMap<>(Weapon.class);

  @Getter
  protected Weapon weaponBeingUsed;


  public ScreenWeapon(
      final DaiKombatAssetsManager assetsManager,
      final Map<Weapon, WeaponStats> weaponStats) {
    quadDamageAttack = new TimeLimitedSound(
        assetsManager.getUserSettingSound(SoundRegistry.QUAD_DAMAGE_ATTACK));
    weaponChangeSound = assetsManager.getUserSettingSound(SoundRegistry.WEAPON_CHANGE);
    weaponStates.put(Weapon.SHOTGUN, WeaponState.builder()
        .distance(weaponStats.get(Weapon.SHOTGUN).getMaxDistance())
        .fireSound(assetsManager.getUserSettingSound(SoundRegistry.PLAYER_SHOTGUN))
        .hitTargetSound(assetsManager.getUserSettingSound(SoundRegistry.HIT_SOUND))
        .screenRatioX(0.35f)
        .screenRatioY(0.40f)
        .center(false)
        .backoffDelayMls(getBackoffDelay(weaponStats.get(Weapon.SHOTGUN), GUNSHOT_ANIMATION_MLS))
        .animationDelayMls(GUNSHOT_ANIMATION_MLS)
        .fireTexture(assetsManager.getTextureRegion(TexturesRegistry.GUN_SHOOT, 0, 0,
            149, 117 - 10))
        .idleTexture(assetsManager.getTextureRegion(TexturesRegistry.GUN_IDLE, 0, 0,
            149, 117 - 10))
        .weaponScreenPositioning(animationTime -> new Vector2(0, -35))
        .build());

    weaponStates.put(Weapon.GAUNTLET, WeaponState.builder()
        .distance(weaponStats.get(Weapon.GAUNTLET).getMaxDistance())
        .fireSound(assetsManager.getUserSettingSound(SoundRegistry.PUNCH_THROWN))
        .hitTargetSound(assetsManager.getUserSettingSound(SoundRegistry.PUNCH_HIT))
        .screenRatioX(0.5f)
        .screenRatioY(0.45f)
        .center(false)
        .backoffDelayMls(getBackoffDelay(weaponStats.get(Weapon.GAUNTLET), PUNCH_ANIMATION_MLS))
        .animationDelayMls(PUNCH_ANIMATION_MLS)
        .fireTexture(assetsManager.getTextureRegion(TexturesRegistry.PUNCH, 0, 0,
            273, 175))
        .idleTexture(assetsManager.getTextureRegion(TexturesRegistry.PUNCH_IDLE, 0, 0,
            273, 175))
        .weaponScreenPositioning(
            animationTime -> new Vector2(-(animationTime / (float) PUNCH_ANIMATION_MLS) * 200 + 200,
                0))
        .build());

    weaponStates.put(Weapon.RAILGUN, WeaponState.builder()
        .distance(weaponStats.get(Weapon.RAILGUN).getMaxDistance())
        .hitTargetSound(assetsManager.getUserSettingSound(SoundRegistry.HIT_SOUND))
        .fireSound(assetsManager.getUserSettingSound(SoundRegistry.PLAYER_RAILGUN))
        .screenRatioX(0.35f)
        .screenRatioY(0.40f)
        .center(false)
        .backoffDelayMls(getBackoffDelay(weaponStats.get(Weapon.RAILGUN), RAILGUN_ANIMATION_MLS))
        .animationDelayMls(RAILGUN_ANIMATION_MLS)
        .fireTexture(assetsManager.getTextureRegion(TexturesRegistry.RAILGUN_SHOOTING, 0, 0,
            170, 118))
        .idleTexture(assetsManager.getTextureRegion(TexturesRegistry.RAILGUN_IDLE, 0, 0,
            170, 118))
        .weaponScreenPositioning(animationTime -> new Vector2(0, -35))
        .build());

    weaponStates.put(Weapon.MINIGUN, WeaponState.builder()
        .distance(weaponStats.get(Weapon.MINIGUN).getMaxDistance())
        .hitTargetSound(assetsManager.getUserSettingSound(SoundRegistry.HIT_SOUND))
        .fireSound(assetsManager.getUserSettingSound(SoundRegistry.PLAYER_MINIGUN))
        .screenRatioX(0.45f)
        .screenRatioY(0.40f)
        .backoffDelayMls(getBackoffDelay(weaponStats.get(Weapon.MINIGUN), MINIGUN_ANIMATION_MLS))
        .animationDelayMls(MINIGUN_ANIMATION_MLS)
        .center(true)
        .fireTexture(assetsManager.getTextureRegion(TexturesRegistry.MINIGUN_FIRE, 0, 0,
            185, 96))
        .idleTexture(assetsManager.getTextureRegion(TexturesRegistry.MINIGUN_IDLE, 0, 0,
            185, 96))
        .weaponScreenPositioning(
            animationTime -> new Vector2((float) Math.sin(animationTime) * 15f, -35))
        .build());

    setWeaponBeingUsed(Weapon.SHOTGUN);
  }

  public boolean punch(Player player) {
    var prevWeapon = weaponBeingUsed;
    setWeaponBeingUsed(Weapon.GAUNTLET);
    var attacked = attack(player);
    if (attacked && prevWeapon != Weapon.GAUNTLET) {
      // get back to the previous weapon when animation is done
      scheduledExecutor.schedule(() -> {
        tasks.add(() -> setWeaponBeingUsed(prevWeapon));
      }, PUNCH_ANIMATION_MLS, TimeUnit.MILLISECONDS);
    } else {
      setWeaponBeingUsed(prevWeapon);
    }
    return attacked;
  }

  public void executeTask() {
    Runnable toExecute;
    while ((toExecute = tasks.poll()) != null) {
      toExecute.run();
    }
  }

  private static int getBackoffDelay(WeaponStats weaponStats, int animationMls) {
    return Math.max(0, weaponStats.getDelayMls() - animationMls);
  }

  public void changeWeapon(Weapon weapon) {
    if (weaponBeingUsed == weapon
        || System.currentTimeMillis() < weaponChangedLastTimeMls + CHANGE_WEAPON_DELAY_MLS) {
      return;
    }
    weaponChangedLastTimeMls = System.currentTimeMillis();
    setWeaponBeingUsed(weapon);
    weaponChangeSound.play(Constants.DEFAULT_SFX_VOLUME);
  }

  public void changeToNextWeapon() {
    changeWeapon(weaponBeingUsed.nextWeapon());
  }

  public void changeToPrevWeapon() {
    changeWeapon(weaponBeingUsed.prevWeapon());
  }

  private void setWeaponBeingUsed(Weapon weapon) {
    weaponBeingUsed = weapon;
  }

  public void registerHit(Weapon weapon) {
    Optional.ofNullable(weaponStates.get(weapon))
        .map(WeaponState::getHitTargetSound)
        .ifPresent(
            userSettingSound -> new TimeLimitedSound(userSettingSound).play(SoundVolumeType.LOUD,
                0.f, 500));

  }

  public float getWeaponDistance(Weapon weapon) {
    return weaponStates.get(weapon).getDistance();
  }

  public WeaponRenderData getActiveWeaponForRendering() {
    if (isNoAnimation()) {
      return getIdleWeaponRendering();
    }
    return Optional.ofNullable(weaponStates.get(weaponBeingUsed))
        .map(currentActiveWeaponState
            -> WeaponRenderData.builder().textureRegion(currentActiveWeaponState.getFireTexture())
            .distance(currentActiveWeaponState.getDistance())
            .center(currentActiveWeaponState.isCenter())
            .positioning(currentActiveWeaponState.getWeaponScreenPositioning()
                .apply(
                    System.currentTimeMillis() - animationStart.getOrDefault(weaponBeingUsed, 0L)))
            .screenRatioX(currentActiveWeaponState.getScreenRatioX())
            .screenRatioY(currentActiveWeaponState.getScreenRatioY())
            .build())
        .orElse(getIdleWeaponRendering());
  }

  public boolean attack(Player player) {
    if (canAttack()) {
      var state = weaponStates.get(weaponBeingUsed);
      animationStart.put(weaponBeingUsed, System.currentTimeMillis());
      state.getFireSound().play(Constants.DEFAULT_SFX_VOLUME);
      if (player.getPlayerEffects().isPowerUpActive(PowerUpType.QUAD_DAMAGE)) {
        quadDamageAttack.play(SoundVolumeType.LOUD, 0, 450);
      }
      return true;
    } else {
      return false;
    }
  }

  boolean canAttack() {
    return Optional.ofNullable(weaponStates.get(weaponBeingUsed))
        .map(weaponState -> animationStart.getOrDefault(weaponBeingUsed, 0L)
            + weaponState.getAnimationDelayMls()
            + weaponState.getBackoffDelayMls()
            < System.currentTimeMillis())
        .orElse(true);
  }

  private boolean isNoAnimation() {
    return Optional.ofNullable(weaponStates.get(weaponBeingUsed))
        .map(weaponState ->
            animationStart.getOrDefault(weaponBeingUsed, 0L)
                + weaponState.getAnimationDelayMls()
                < System.currentTimeMillis())
        .orElse(true);
  }

  private WeaponRenderData getIdleWeaponRendering() {
    var weaponState = weaponStates.get(weaponBeingUsed);
    return WeaponRenderData.builder().textureRegion(weaponState.getIdleTexture())
        .distance(weaponState.getDistance())
        .center(weaponState.isCenter())
        .screenRatioX(weaponState.getScreenRatioX())
        .screenRatioY(weaponState.getScreenRatioY())
        .positioning(Vector2.Zero).build();
  }

}
