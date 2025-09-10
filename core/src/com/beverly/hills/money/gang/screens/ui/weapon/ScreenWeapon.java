package com.beverly.hills.money.gang.screens.ui.weapon;

import static com.beverly.hills.money.gang.configs.Constants.PUNCH_ANIMATION_MLS;

import com.badlogic.gdx.math.Vector2;
import com.beverly.hills.money.gang.configs.Constants;
import com.beverly.hills.money.gang.assets.managers.DaiKombatAssetsManager;
import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import com.beverly.hills.money.gang.assets.managers.sound.SoundVolumeType;
import com.beverly.hills.money.gang.assets.managers.sound.TimeLimitedSound;
import com.beverly.hills.money.gang.assets.managers.sound.TimeLimitedSound.TimeLimitSoundConf;
import com.beverly.hills.money.gang.assets.managers.sound.UserSettingSound;
import com.beverly.hills.money.gang.entities.item.PowerUpType;
import com.beverly.hills.money.gang.entities.player.Player;
import com.beverly.hills.money.gang.registry.ScreenWeaponStateFactoriesRegistry;
import com.beverly.hills.money.gang.screens.ui.selection.GamePlayerClass;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.Getter;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

public class ScreenWeapon {

  private final Map<Weapon, Long> animationStart = new HashMap<>();

  private final GamePlayerClass playerClass;

  protected final Map<Weapon, WeaponAmmo> ammo = new HashMap<>();

  private final ScreenWeaponStateFactoriesRegistry screenWeaponStateFactoriesRegistry;

  private final ScheduledExecutorService scheduledExecutor
      = Executors.newScheduledThreadPool(1,
      new BasicThreadFactory.Builder().namingPattern("weapon-change-scheduler-%d").daemon(true)
          .build());

  private final Queue<Runnable> tasks = new ArrayDeque<>();

  protected static final int CHANGE_WEAPON_DELAY_MLS = 100;

  private final TimeLimitedSound quadDamageAttack;

  private final UserSettingSound weaponChangeSound;

  private long weaponChangedLastTimeMls;

  final Map<Weapon, WeaponState> weaponStates = new EnumMap<>(Weapon.class);

  @Getter
  protected Weapon weaponBeingUsed;


  public ScreenWeapon(
      final DaiKombatAssetsManager assetsManager,
      final Map<Weapon, WeaponStats> weaponStats,
      final ScreenWeaponStateFactoriesRegistry screenWeaponStateFactoriesRegistry,
      final GamePlayerClass playerClass) {
    this.playerClass = playerClass;
    this.screenWeaponStateFactoriesRegistry = screenWeaponStateFactoriesRegistry;
    quadDamageAttack = new TimeLimitedSound(
        assetsManager.getUserSettingSound(SoundRegistry.QUAD_DAMAGE_ATTACK));
    weaponChangeSound = assetsManager.getUserSettingSound(SoundRegistry.WEAPON_CHANGE);
    registerWeapons(assetsManager, weaponStats);
    // TODO check on server side that at least one weapon is available per class
    setWeaponBeingUsed(getAvailableWeapons().stream()
        .filter(weapon -> weapon != Weapon.GAUNTLET).findFirst().get());
  }

  private void registerWeapons(final DaiKombatAssetsManager assetsManager,
      final Map<Weapon, WeaponStats> weaponsStats) {
    weaponsStats.forEach((weapon, weaponStats) -> {
      weaponStates.put(weapon, screenWeaponStateFactoriesRegistry.get(weapon)
          .create(assetsManager, weaponStats));
      Optional.ofNullable(weaponStats)
          .map(WeaponStats::getMaxAmmo).ifPresent(
              maxAmmo -> ammo.put(weapon, new WeaponAmmo(maxAmmo)));
    });
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
    if (weaponStates.size() <= 1) {
      return;
    }
    var newWeapon = weaponBeingUsed.nextWeapon(weaponStates.keySet());
    if (newWeapon == Weapon.GAUNTLET) {
      newWeapon = newWeapon.nextWeapon(weaponStates.keySet());
    }
    changeWeapon(newWeapon);
  }

  public List<Weapon> getAvailableWeapons() {
    return weaponStates.keySet().stream().sorted(Comparator.comparingInt(Enum::ordinal))
        .collect(Collectors.toList());
  }

  public void changeToPrevWeapon() {
    if (weaponStates.size() <= 1) {
      return;
    }
    var newWeapon = weaponBeingUsed.prevWeapon(weaponStates.keySet());
    if (newWeapon == Weapon.GAUNTLET) {
      newWeapon = newWeapon.prevWeapon(weaponStates.keySet());
    }
    changeWeapon(newWeapon);
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
            -> WeaponRenderData.builder()
            .textureRegion(currentActiveWeaponState.getFireTextures().get(playerClass))
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
      wasteAmmo();
      var state = weaponStates.get(weaponBeingUsed);
      animationStart.put(weaponBeingUsed, System.currentTimeMillis());
      state.getFireSound().play(Constants.DEFAULT_SHOOTING_VOLUME);
      if (player.getPlayerEffects().isPowerUpActive(PowerUpType.QUAD_DAMAGE)
          || player.getPlayerEffects().isPowerUpActive(PowerUpType.BEAST)) {
        quadDamageAttack.play(TimeLimitSoundConf.builder()
            .soundVolumeType(SoundVolumeType.LOUD).frequencyMls(450)
            .build());
      }
      return true;
    } else {
      return false;
    }
  }

  public boolean hasAmmo() {
    return Optional.ofNullable(ammo.get(weaponBeingUsed))
        .map(WeaponAmmo::hasAmmo)
        // if weapon has no concept of ammo (like gauntlet)
        .orElse(true);
  }

  public String getAmmoStats() {
    return Optional.ofNullable(ammo.get(weaponBeingUsed))
        .map(WeaponAmmo::toString).orElse("");
  }

  private void wasteAmmo() {
    Optional.ofNullable(ammo.get(weaponBeingUsed))
        .filter(WeaponAmmo::hasAmmo).ifPresent(WeaponAmmo::wasteAmmo);
  }

  public void setWeaponAmmo(Weapon weapon, int maxAmmo) {
    ammo.put(weapon, new WeaponAmmo(maxAmmo));
  }

  boolean canAttack() {
    return hasAmmo() && Optional.ofNullable(weaponStates.get(weaponBeingUsed))
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
    return WeaponRenderData.builder().textureRegion(weaponState.getIdleTextures().get(playerClass))
        .distance(weaponState.getDistance())
        .center(weaponState.isCenter())
        .screenRatioX(weaponState.getScreenRatioX())
        .screenRatioY(weaponState.getScreenRatioY())
        .positioning(Vector2.Zero).build();
  }

  public void dispose() {
    scheduledExecutor.shutdown();
  }

}
