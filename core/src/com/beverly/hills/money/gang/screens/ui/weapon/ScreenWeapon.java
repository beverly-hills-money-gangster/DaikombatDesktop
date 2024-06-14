package com.beverly.hills.money.gang.screens.ui.weapon;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.beverly.hills.money.gang.Configs;
import com.beverly.hills.money.gang.assets.managers.DaiKombatAssetsManager;
import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import com.beverly.hills.money.gang.assets.managers.registry.TexturesRegistry;
import com.beverly.hills.money.gang.assets.managers.sound.UserSettingSound;
import com.beverly.hills.money.gang.entities.item.PowerUpType;
import com.beverly.hills.money.gang.entities.player.Player;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

public class ScreenWeapon {

  private static final int GUNSHOT_ANIMATION_MLS = 350;

  private static final int PUNCH_ANIMATION_MLS = 175;

  private final UserSettingSound quadDamageAttack;

  final Map<Weapon, WeaponState> weaponStates = new EnumMap<>(Weapon.class);

  protected Weapon weaponBeingUsed;

  private final TextureRegion idleWeapon;

  public ScreenWeapon(DaiKombatAssetsManager assetsManager) {
    quadDamageAttack = assetsManager.getUserSettingSound(SoundRegistry.QUAD_DAMAGE_ATTACK);
    idleWeapon = assetsManager.getTextureRegion(TexturesRegistry.GUN_IDLE, 0, 0, 149, 117);
    weaponStates.put(Weapon.SHOTGUN, WeaponState.builder()
        .distance(Configs.SHOOTING_DISTANCE)
        .fireSound(assetsManager.getUserSettingSound(SoundRegistry.PLAYER_SHOTGUN))
        .hitTargetSound(assetsManager.getUserSettingSound(SoundRegistry.SHOOT_HIT_SOUND))
        .screenRatioX(0.35f)
        .screenRatioY(0.40f)
        .animationDelayMls(GUNSHOT_ANIMATION_MLS)
        .fireTexture(assetsManager.getTextureRegion(TexturesRegistry.GUN_SHOOT, 0, 0,
            149, 117 - 10))
        .weaponScreenPositioning(animationTime -> Vector2.Zero)
        .build());

    weaponStates.put(Weapon.GAUNTLET, WeaponState.builder()
        .distance(Configs.MELEE_DISTANCE)
        .fireSound(assetsManager.getUserSettingSound(SoundRegistry.PUNCH_THROWN))
        .hitTargetSound(assetsManager.getUserSettingSound(SoundRegistry.PUNCH_HIT))
        .screenRatioX(0.5f)
        .screenRatioY(0.45f)
        .animationDelayMls(PUNCH_ANIMATION_MLS)
        .fireTexture(assetsManager.getTextureRegion(TexturesRegistry.PUNCH, 0, 0,
            273, 175))
        .weaponScreenPositioning(
            animationTime -> new Vector2(-(animationTime / (float) PUNCH_ANIMATION_MLS) * 200 + 200,
                0))
        .build());
  }

  boolean canAttack() {
    return isNoAnimation();
  }

  private boolean isNoAnimation() {
    return Optional.ofNullable(weaponStates.get(weaponBeingUsed))
        .map(weaponState -> weaponState.animationStartMls + weaponState.animationDelayMls
            < System.currentTimeMillis())
        .orElse(true);
  }

  public float getWeaponDistance(Weapon weapon) {
    return weaponStates.get(weapon).getDistance();
  }

  public WeaponRenderData getActiveWeaponForRendering() {
    if (isNoAnimation()) {
      weaponBeingUsed = null;
      return getIdleWeaponRendering();
    }
    return Optional.ofNullable(weaponStates.get(weaponBeingUsed))
        .map(currentActiveWeaponState
            -> WeaponRenderData.builder().textureRegion(currentActiveWeaponState.fireTexture)
            .distance(currentActiveWeaponState.distance)
            .positioning(currentActiveWeaponState.weaponScreenPositioning
                .apply(System.currentTimeMillis() - currentActiveWeaponState.animationStartMls))
            .screenRatioX(currentActiveWeaponState.getScreenRatioX())
            .screenRatioY(currentActiveWeaponState.screenRatioY)
            .build())
        .orElse(getIdleWeaponRendering());
  }

  private WeaponRenderData getIdleWeaponRendering() {
    return WeaponRenderData.builder().textureRegion(idleWeapon)
        .distance(Configs.SHOOTING_DISTANCE)
        .screenRatioX(0.35f)
        .screenRatioY(0.40f)
        .positioning(Vector2.Zero).build();
  }

  public boolean attack(Player player, Weapon weapon, float soundVolume) {
    if (canAttack()) {
      var state = weaponStates.get(weapon);
      state.animationStartMls = System.currentTimeMillis();
      weaponBeingUsed = weapon;
      state.fireSound.play(soundVolume);
      if (player.getPlayerEffects().isPowerUpActive(PowerUpType.QUAD_DAMAGE)) {
        quadDamageAttack.play(soundVolume);
      }
      return true;
    } else {
      return false;
    }
  }


  public void registerHit( Weapon weapon, float volume) {
    Optional.ofNullable(weaponStates.get(weapon))
        .map(weaponState -> weaponState.hitTargetSound)
        .ifPresent(userSettingSound -> userSettingSound.play(volume));

  }

  public enum Weapon {
    SHOTGUN, GAUNTLET;

  }

  @Getter
  @Builder
  static class WeaponState {

    private final float distance;
    @NonNull
    private final UserSettingSound fireSound;
    private final UserSettingSound hitTargetSound;
    private final int animationDelayMls;
    @NonNull
    private final TextureRegion fireTexture;
    private final float screenRatioX;
    private final float screenRatioY;
    @Builder.Default
    private long animationStartMls = 0;
    private final Function<Long, Vector2> weaponScreenPositioning;
  }

  @Builder
  @Getter
  public static class WeaponRenderData {

    private final float distance;
    private final Vector2 positioning;
    private final TextureRegion textureRegion;
    private final float screenRatioX;
    private final float screenRatioY;
  }
}
