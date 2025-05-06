package com.beverly.hills.money.gang.screens.ui.weapon;

import static com.beverly.hills.money.gang.Constants.PUNCH_ANIMATION_MLS;

import com.badlogic.gdx.math.Vector2;
import com.beverly.hills.money.gang.Constants;
import com.beverly.hills.money.gang.assets.managers.DaiKombatAssetsManager;
import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import com.beverly.hills.money.gang.assets.managers.sound.SoundVolumeType;
import com.beverly.hills.money.gang.assets.managers.sound.TimeLimitedSound;
import com.beverly.hills.money.gang.assets.managers.sound.TimeLimitedSound.TimeLimitSoundConf;
import com.beverly.hills.money.gang.assets.managers.sound.UserSettingSound;
import com.beverly.hills.money.gang.entities.item.PowerUpType;
import com.beverly.hills.money.gang.entities.player.Player;
import com.beverly.hills.money.gang.registry.ScreenWeaponStateFactoriesRegistry;
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

  private final ScreenWeaponStateFactoriesRegistry screenWeaponStateFactoriesRegistry;

  private final ScheduledExecutorService scheduledExecutor
      = Executors.newScheduledThreadPool(1,
      new BasicThreadFactory.Builder().namingPattern("weapon-change-scheduler-%d").daemon(true)
          .build());

  private final Queue<Runnable> tasks = new ArrayDeque<>();

  protected static final int CHANGE_WEAPON_DELAY_MLS = 75;

  private final TimeLimitedSound quadDamageAttack;

  private final UserSettingSound weaponChangeSound;

  private long weaponChangedLastTimeMls;

  final Map<Weapon, WeaponState> weaponStates = new EnumMap<>(Weapon.class);

  @Getter
  protected Weapon weaponBeingUsed;


  public ScreenWeapon(
      final DaiKombatAssetsManager assetsManager,
      final Map<Weapon, WeaponStats> weaponStats,
      final ScreenWeaponStateFactoriesRegistry screenWeaponStateFactoriesRegistry) {
    this.screenWeaponStateFactoriesRegistry = screenWeaponStateFactoriesRegistry;
    quadDamageAttack = new TimeLimitedSound(
        assetsManager.getUserSettingSound(SoundRegistry.QUAD_DAMAGE_ATTACK));
    weaponChangeSound = assetsManager.getUserSettingSound(SoundRegistry.WEAPON_CHANGE);
    for (Weapon value : Weapon.values()) {
      putWeapon(value, assetsManager, weaponStats);
    }
    setWeaponBeingUsed(Weapon.SHOTGUN);
  }

  private void putWeapon(final Weapon weapon, final DaiKombatAssetsManager assetsManager,
      final Map<Weapon, WeaponStats> weaponStats) {
    weaponStates.put(weapon, screenWeaponStateFactoriesRegistry.get(weapon)
        .create(assetsManager, weaponStats.get(weapon)));
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

  public void setWeaponBeingUsed(Weapon weapon) {
    weaponBeingUsed = weapon;
  }

  public void registerHit(Weapon weapon) {
    Optional.ofNullable(weaponStates.get(weapon))
        .map(WeaponState::getHitTargetSound)
        .ifPresent(userSettingSound -> new TimeLimitedSound(userSettingSound).play(
            TimeLimitSoundConf.builder()
                .soundVolumeType(SoundVolumeType.LOUD).frequencyMls(500).build()));

  }

  public float getWeaponDistance(Weapon weapon) {
    return weaponStates.get(weapon).getDistance();
  }

  public WeaponState getWeaponState(Weapon weapon) {
    return weaponStates.get(weapon);
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
      state.getFireSound().play(Constants.DEFAULT_SHOOTING_VOLUME);
      if (player.getPlayerEffects().isPowerUpActive(PowerUpType.QUAD_DAMAGE)) {
        quadDamageAttack.play(TimeLimitSoundConf.builder()
            .soundVolumeType(SoundVolumeType.LOUD).frequencyMls(450)
            .build());
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
