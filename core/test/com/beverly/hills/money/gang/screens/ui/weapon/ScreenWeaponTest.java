package com.beverly.hills.money.gang.screens.ui.weapon;

import static com.beverly.hills.money.gang.Constants.DEFAULT_SFX_VOLUME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.beverly.hills.money.gang.assets.managers.DaiKombatAssetsManager;
import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import com.beverly.hills.money.gang.assets.managers.sound.SoundVolumeType;
import com.beverly.hills.money.gang.assets.managers.sound.TimeLimitedSound;
import com.beverly.hills.money.gang.assets.managers.sound.UserSettingSound;
import com.beverly.hills.money.gang.entities.effect.PlayerEffects;
import com.beverly.hills.money.gang.entities.item.PowerUpType;
import com.beverly.hills.money.gang.entities.player.Player;
import com.beverly.hills.money.gang.factory.ScreenWeaponStateFactory;
import com.beverly.hills.money.gang.registry.ScreenWeaponStateFactoriesRegistry;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ScreenWeaponTest {

  private ScreenWeapon screenWeapon;

  private DaiKombatAssetsManager daiKombatAssetsManager;

  private Map<Weapon, WeaponState> weaponStateMap;

  private UserSettingSound quadDamageAttackSound, weaponChangeSound;

  private Player player;

  private PlayerEffects playerEffects;


  @BeforeEach
  public void setUp() {
    weaponStateMap = new HashMap<>();
    TimeLimitedSound.clear();
    daiKombatAssetsManager = mock(DaiKombatAssetsManager.class);
    quadDamageAttackSound = mock(UserSettingSound.class);
    weaponChangeSound = mock(UserSettingSound.class);
    doReturn(quadDamageAttackSound).when(daiKombatAssetsManager)
        .getUserSettingSound(SoundRegistry.QUAD_DAMAGE_ATTACK);
    doReturn(weaponChangeSound).when(daiKombatAssetsManager)
        .getUserSettingSound(SoundRegistry.WEAPON_CHANGE);
    var screenWeaponStateFactoriesRegistry = mock(ScreenWeaponStateFactoriesRegistry.class);
    for (Weapon value : Weapon.values()) {
      var screenWeaponStateFactory = mock(ScreenWeaponStateFactory.class);
      var mockWeaponState = mock(WeaponState.class);
      doReturn(mock(UserSettingSound.class)).when(mockWeaponState).getFireSound();
      doReturn(mock(UserSettingSound.class)).when(mockWeaponState).getHitTargetSound();
      doReturn(mock(TextureRegion.class)).when(mockWeaponState).getFireTexture();
      doReturn(mock(TextureRegion.class)).when(mockWeaponState).getIdleTexture();
      doReturn(100).when(mockWeaponState).getBackoffDelayMls();
      doReturn(150).when(mockWeaponState).getAnimationDelayMls();
      doReturn((Function<Long, Vector2>) aLong
          -> new Vector2(0, 0)).when(mockWeaponState).getWeaponScreenPositioning();
      weaponStateMap.put(value, mockWeaponState);
      doReturn(mockWeaponState).when(screenWeaponStateFactory).create(any(), any());
      doReturn(screenWeaponStateFactory).when(screenWeaponStateFactoriesRegistry).get(eq(value));
    }

    screenWeapon = new ScreenWeapon(daiKombatAssetsManager,
        Map.of(
            Weapon.SHOTGUN, WeaponStats.builder().maxDistance(7).delayMls(500).build(),
            Weapon.GAUNTLET, WeaponStats.builder().maxDistance(1).delayMls(500).build(),
            Weapon.MINIGUN, WeaponStats.builder().maxDistance(7).delayMls(150).build(),
            Weapon.RAILGUN, WeaponStats.builder().maxDistance(10).delayMls(1_500).build(),
            Weapon.ROCKET_LAUNCHER, WeaponStats.builder().maxDistance(999).delayMls(1_700).build()),
        screenWeaponStateFactoriesRegistry
    );
    player = mock(Player.class);
    playerEffects = mock(PlayerEffects.class);
    doReturn(playerEffects).when(player).getPlayerEffects();
  }


  @Test
  public void testDefaults() {
    assertEquals(Weapon.values().length, screenWeapon.weaponStates.size(),
        "All weapons must be registered");
    Arrays.stream(Weapon.values())
        .forEach(weapon -> assertNotNull(screenWeapon.weaponStates.get(weapon),
            "Weapon " + weapon.name() + " must be registered"));
  }

  @Test
  public void testCanAttack() {
    assertTrue(screenWeapon.canAttack(), "By default, you should be able to attack");
  }

  @Test
  public void testCanAttackAfterRecentAttack() {
    screenWeapon.changeWeapon(Weapon.SHOTGUN);
    screenWeapon.attack(player);
    assertFalse(screenWeapon.canAttack(),
        "You can't attack right after. There must be some cool down");
  }

  @Test
  public void testCanAttackAfterWait() throws InterruptedException {
    screenWeapon.changeWeapon(Weapon.SHOTGUN);
    screenWeapon.attack(player);
    // wait a little
    Thread.sleep(
        screenWeapon.weaponStates.get(Weapon.SHOTGUN).getAnimationDelayMls()
            + screenWeapon.weaponStates.get(Weapon.SHOTGUN).getBackoffDelayMls() + 50);
    assertTrue(screenWeapon.canAttack(), "Must be ok to attack as we waited");
  }

  @Test
  public void testAttackShotgun() {
    screenWeapon.changeWeapon(Weapon.SHOTGUN);
    assertTrue(screenWeapon.attack(player));
    verify(weaponStateMap.get(Weapon.SHOTGUN).getFireSound()).play(DEFAULT_SFX_VOLUME);
    assertEquals(Weapon.SHOTGUN, screenWeapon.weaponBeingUsed);
    verifyNoInteractions(quadDamageAttackSound);
  }

  @Test
  public void testAttackShotgunQuadDamage() {
    doReturn(true).when(playerEffects).isPowerUpActive(PowerUpType.QUAD_DAMAGE);
    screenWeapon.changeWeapon(Weapon.SHOTGUN);
    assertTrue(screenWeapon.attack(player));
    verify(weaponStateMap.get(Weapon.SHOTGUN).getFireSound()).play(DEFAULT_SFX_VOLUME);
    verify(quadDamageAttackSound).play(argThat(argument -> {
      assertEquals(SoundVolumeType.LOUD.getVolume(), argument.getVolume());
      assertEquals(0, argument.getPan());
      assertEquals(1, argument.getPitch());
      return true;
    }));
    assertEquals(Weapon.SHOTGUN, screenWeapon.weaponBeingUsed);
  }

  @Test
  public void testAttackPunch() {
    screenWeapon.changeWeapon(Weapon.GAUNTLET);
    assertTrue(screenWeapon.attack(player));
    verify(weaponStateMap.get(Weapon.GAUNTLET).getFireSound()).play(anyFloat());
    assertEquals(Weapon.GAUNTLET, screenWeapon.weaponBeingUsed);
  }

  @Test
  public void testAttackTwiceNoDelay() {
    screenWeapon.changeWeapon(Weapon.SHOTGUN);
    assertTrue(screenWeapon.attack(player));
    assertFalse(screenWeapon.attack(player),
        "If no delay, then we shouldn't be able to attack");
    verify(weaponStateMap.get(Weapon.SHOTGUN).getFireSound()).play(DEFAULT_SFX_VOLUME);
  }

  @Test
  public void testAttackTwiceDelay() throws InterruptedException {
    screenWeapon.changeWeapon(Weapon.SHOTGUN);
    assertTrue(screenWeapon.attack(player));
    // wait a little
    Thread.sleep(
        screenWeapon.weaponStates.get(Weapon.SHOTGUN).getAnimationDelayMls()
            + screenWeapon.weaponStates.get(Weapon.SHOTGUN).getBackoffDelayMls() + 50);
    assertTrue(screenWeapon.attack(player),
        "If we have a  delay, then we SHOULD be able to attack");
    verify(weaponStateMap.get(Weapon.SHOTGUN).getFireSound(), times(2)).play(DEFAULT_SFX_VOLUME);
  }

  @Test
  public void testRegisterHitShotgun() {
    screenWeapon.registerHit(Weapon.SHOTGUN);
    verify(weaponStateMap.get(Weapon.SHOTGUN).getHitTargetSound())
        .play(argThat(argument -> {
          assertEquals(SoundVolumeType.LOUD.getVolume(), argument.getVolume());
          assertEquals(0, argument.getPan());
          assertEquals(1, argument.getPitch());
          return true;
        }));
  }

  @Test
  public void testRegisterHitRailgun() {
    screenWeapon.registerHit(Weapon.RAILGUN);
    verify(weaponStateMap.get(Weapon.RAILGUN).getHitTargetSound())
        .play(argThat(argument -> {
          assertEquals(SoundVolumeType.LOUD.getVolume(), argument.getVolume());
          assertEquals(0, argument.getPan());
          assertEquals(1, argument.getPitch());
          return true;
        }));
  }

  @Test
  public void testRegisterHitPunch() {
    screenWeapon.registerHit(Weapon.GAUNTLET);
    verify(weaponStateMap.get(Weapon.GAUNTLET).getHitTargetSound())
        .play(argThat(argument -> {
          assertEquals(SoundVolumeType.LOUD.getVolume(), argument.getVolume());
          assertEquals(0, argument.getPan());
          assertEquals(1, argument.getPitch());
          return true;
        }));
  }

  @Test
  public void testGetActiveWeaponForRenderingIdle() {
    var renderingData = screenWeapon.getActiveWeaponForRendering();
    assertEquals(weaponStateMap.get(Weapon.SHOTGUN).getIdleTexture(),
        renderingData.getTextureRegion(),
        "Be default, idle texture should be returned");
  }

  @Test
  public void testGetActiveWeaponForRenderingShotgun() {
    screenWeapon.changeWeapon(Weapon.SHOTGUN);
    screenWeapon.attack(player);
    var renderingData = screenWeapon.getActiveWeaponForRendering();
    assertEquals(weaponStateMap.get(Weapon.SHOTGUN).getFireTexture(),
        renderingData.getTextureRegion());
  }

  @Test
  public void testGetActiveWeaponForRenderingPunch() {
    screenWeapon.changeWeapon(Weapon.GAUNTLET);
    screenWeapon.attack(player);
    var renderingData = screenWeapon.getActiveWeaponForRendering();
    assertEquals(weaponStateMap.get(Weapon.GAUNTLET).getFireTexture(),
        renderingData.getTextureRegion());
  }

  @Test
  public void testGetActiveWeaponForRenderingWaitAnimationFinish() throws InterruptedException {
    screenWeapon.changeWeapon(Weapon.SHOTGUN);
    screenWeapon.attack(player);
    // wait a little
    Thread.sleep(
        screenWeapon.weaponStates.get(Weapon.SHOTGUN).getAnimationDelayMls() + 50);
    var renderingData = screenWeapon.getActiveWeaponForRendering();
    assertEquals(weaponStateMap.get(Weapon.SHOTGUN).getIdleTexture(),
        renderingData.getTextureRegion(),
        "After animation finish, idle texture should be returned");
  }

  @Test
  public void testChangeWeapon() {
    screenWeapon.changeWeapon(Weapon.RAILGUN);
    assertEquals(Weapon.RAILGUN, screenWeapon.getWeaponBeingUsed());
    verify(weaponChangeSound).play(anyFloat());
  }

  @Test
  public void testChangeWeaponNoDelay() {
    screenWeapon.changeWeapon(Weapon.RAILGUN);
    assertEquals(Weapon.RAILGUN, screenWeapon.getWeaponBeingUsed());
    screenWeapon.changeWeapon(Weapon.SHOTGUN);
    assertEquals(Weapon.RAILGUN, screenWeapon.getWeaponBeingUsed(),
        "Weapon should still be the same, because we can't change weapons so fast");
    verify(weaponChangeSound).play(anyFloat());

  }


  @Test
  public void testChangeWeaponSame() {
    screenWeapon.changeWeapon(Weapon.RAILGUN);
    screenWeapon.changeWeapon(Weapon.RAILGUN); // second time
    assertEquals(Weapon.RAILGUN, screenWeapon.getWeaponBeingUsed());
    verify(weaponChangeSound).play(anyFloat()); // should play only once
  }

  @Test
  public void testChangeWeaponAndShoot() throws InterruptedException {
    screenWeapon.changeWeapon(Weapon.RAILGUN);
    assertTrue(screenWeapon.canAttack());
    screenWeapon.attack(player);
    Thread.sleep(ScreenWeapon.CHANGE_WEAPON_DELAY_MLS);
    screenWeapon.changeWeapon(Weapon.SHOTGUN);
    assertTrue(screenWeapon.canAttack());
    screenWeapon.attack(player);

    assertEquals(Weapon.SHOTGUN, screenWeapon.getWeaponBeingUsed());
    verify(weaponChangeSound, times(2)).play(anyFloat());
  }

  @Test
  public void testChangeWeaponShotChangeShootChangeShoot() throws InterruptedException {
    screenWeapon.changeWeapon(Weapon.RAILGUN);
    assertTrue(screenWeapon.canAttack());
    screenWeapon.attack(player);
    Thread.sleep(ScreenWeapon.CHANGE_WEAPON_DELAY_MLS);
    screenWeapon.changeWeapon(Weapon.SHOTGUN);
    assertTrue(screenWeapon.canAttack());
    screenWeapon.attack(player);
    Thread.sleep(ScreenWeapon.CHANGE_WEAPON_DELAY_MLS);
    screenWeapon.changeWeapon(Weapon.RAILGUN);
    assertFalse(screenWeapon.canAttack(),
        "Can't attack because the animation hasn't finished yet");

    assertEquals(Weapon.RAILGUN, screenWeapon.getWeaponBeingUsed());
    verify(weaponChangeSound, times(3)).play(anyFloat());
  }

  @Test
  public void testChangeWeaponShotChangeShootChangeWaitShoot() throws InterruptedException {
    screenWeapon.changeWeapon(Weapon.RAILGUN);
    assertTrue(screenWeapon.canAttack());
    screenWeapon.attack(player);
    Thread.sleep(ScreenWeapon.CHANGE_WEAPON_DELAY_MLS);
    screenWeapon.changeWeapon(Weapon.SHOTGUN);
    assertTrue(screenWeapon.canAttack());
    screenWeapon.attack(player);

    // wait a little
    Thread.sleep(
        screenWeapon.weaponStates.get(Weapon.RAILGUN).getAnimationDelayMls()
            + screenWeapon.weaponStates.get(Weapon.RAILGUN).getBackoffDelayMls() + 50);

    screenWeapon.changeWeapon(Weapon.RAILGUN);
    assertTrue(screenWeapon.canAttack(), "Should be able to shoot because we waited");

    assertEquals(Weapon.RAILGUN, screenWeapon.getWeaponBeingUsed());
    verify(weaponChangeSound, times(3)).play(anyFloat());
  }

  @Test
  public void testChangeWeaponAllWeapons() throws InterruptedException {
    for (Weapon weapon : Weapon.values()) {
      screenWeapon.changeWeapon(weapon);
      assertEquals(weapon, screenWeapon.getWeaponBeingUsed());
      Thread.sleep(ScreenWeapon.CHANGE_WEAPON_DELAY_MLS);
    }
  }

  @Test
  public void testChangeToNextWeapon() throws InterruptedException {
    screenWeapon.changeWeapon(Weapon.GAUNTLET);
    Thread.sleep(ScreenWeapon.CHANGE_WEAPON_DELAY_MLS);
    screenWeapon.changeToNextWeapon();
    assertEquals(Weapon.SHOTGUN, screenWeapon.getWeaponBeingUsed());
  }

  @Test
  public void testChangeToNextWeaponFullCircle() {
    screenWeapon.changeWeapon(Weapon.GAUNTLET);
    for (int i = 0; i < Weapon.values().length; i++) {
      screenWeapon.changeToNextWeapon();
    }
    assertEquals(Weapon.GAUNTLET, screenWeapon.getWeaponBeingUsed(),
        "After making a full circle, we have to get back to GAUNTLET");
  }

  @Test
  public void testChangeToPrevWeapon() throws InterruptedException {
    screenWeapon.changeWeapon(Weapon.GAUNTLET);
    Thread.sleep(ScreenWeapon.CHANGE_WEAPON_DELAY_MLS);
    screenWeapon.changeToPrevWeapon();
    assertEquals(Weapon.PLASMAGUN, screenWeapon.getWeaponBeingUsed());
  }

  @Test
  public void testChangeToPrevWeaponFullCircle() {
    screenWeapon.changeWeapon(Weapon.RAILGUN);
    for (int i = 0; i < Weapon.values().length; i++) {
      screenWeapon.changeToPrevWeapon();
    }
    assertEquals(Weapon.RAILGUN, screenWeapon.getWeaponBeingUsed(),
        "After making a full circle, we have to get back to RAILGUN");
  }

}
