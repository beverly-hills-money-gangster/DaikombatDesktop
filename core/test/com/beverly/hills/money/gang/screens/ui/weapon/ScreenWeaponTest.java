package com.beverly.hills.money.gang.screens.ui.weapon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.beverly.hills.money.gang.assets.managers.DaiKombatAssetsManager;
import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import com.beverly.hills.money.gang.assets.managers.registry.TexturesRegistry;
import com.beverly.hills.money.gang.assets.managers.sound.UserSettingSound;
import com.beverly.hills.money.gang.entities.effect.PlayerEffects;
import com.beverly.hills.money.gang.entities.item.PowerUpType;
import com.beverly.hills.money.gang.entities.player.Player;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ScreenWeaponTest {

  private ScreenWeapon screenWeapon;

  private DaiKombatAssetsManager daiKombatAssetsManager;

  private UserSettingSound playerShotgunFireSound;

  private UserSettingSound playerRailgunFireSound;
  private UserSettingSound punchSound;

  private UserSettingSound weaponChangeSound;
  private UserSettingSound punchHitSound;

  private UserSettingSound quadDamageAttackSound;

  private UserSettingSound hitSound;

  private TextureRegion shotgunFireTexture;

  private TextureRegion punchIdleTexture;

  private TextureRegion shotgunIdleTexture;

  private TextureRegion railgunFireTexture;

  private TextureRegion railgunIdleTexture;

  private TextureRegion punchTexture;

  private Player player;

  private PlayerEffects playerEffects;


  @BeforeEach
  public void setUp() {
    shotgunIdleTexture = mock(TextureRegion.class);
    shotgunFireTexture = mock(TextureRegion.class);
    railgunIdleTexture = mock(TextureRegion.class);
    railgunFireTexture = mock(TextureRegion.class);
    punchTexture = mock(TextureRegion.class);
    punchIdleTexture = mock(TextureRegion.class);

    weaponChangeSound = mock(UserSettingSound.class);
    hitSound = mock(UserSettingSound.class);
    playerShotgunFireSound = mock(UserSettingSound.class);
    playerRailgunFireSound = mock(UserSettingSound.class);
    punchSound = mock(UserSettingSound.class);
    punchHitSound = mock(UserSettingSound.class);
    quadDamageAttackSound = mock(UserSettingSound.class);

    daiKombatAssetsManager = mock(DaiKombatAssetsManager.class);

    doReturn(playerShotgunFireSound).when(daiKombatAssetsManager)
        .getUserSettingSound(SoundRegistry.PLAYER_SHOTGUN);
    doReturn(playerRailgunFireSound).when(daiKombatAssetsManager)
        .getUserSettingSound(SoundRegistry.PLAYER_RAILGUN);
    doReturn(punchSound).when(daiKombatAssetsManager)
        .getUserSettingSound(SoundRegistry.PUNCH_THROWN);
    doReturn(punchHitSound).when(daiKombatAssetsManager)
        .getUserSettingSound(SoundRegistry.PUNCH_HIT);
    doReturn(hitSound).when(daiKombatAssetsManager)
        .getUserSettingSound(SoundRegistry.HIT_SOUND);
    doReturn(weaponChangeSound).when(daiKombatAssetsManager)
        .getUserSettingSound(SoundRegistry.WEAPON_CHANGE);
    doReturn(quadDamageAttackSound).when(daiKombatAssetsManager)
        .getUserSettingSound(SoundRegistry.QUAD_DAMAGE_ATTACK);

    doReturn(shotgunIdleTexture).when(daiKombatAssetsManager).getTextureRegion(
        eq(TexturesRegistry.GUN_IDLE), anyInt(), anyInt(), anyInt(), anyInt());
    doReturn(shotgunFireTexture).when(daiKombatAssetsManager).getTextureRegion(
        eq(TexturesRegistry.GUN_SHOOT), anyInt(), anyInt(), anyInt(), anyInt());
    doReturn(railgunIdleTexture).when(daiKombatAssetsManager).getTextureRegion(
        eq(TexturesRegistry.RAILGUN_IDLE), anyInt(), anyInt(), anyInt(), anyInt());
    doReturn(railgunFireTexture).when(daiKombatAssetsManager).getTextureRegion(
        eq(TexturesRegistry.RAILGUN_SHOOTING), anyInt(), anyInt(), anyInt(), anyInt());
    doReturn(punchIdleTexture).when(daiKombatAssetsManager).getTextureRegion(
        eq(TexturesRegistry.PUNCH_IDLE), anyInt(), anyInt(), anyInt(), anyInt());
    doReturn(punchTexture).when(daiKombatAssetsManager).getTextureRegion(
        eq(TexturesRegistry.PUNCH), anyInt(), anyInt(), anyInt(), anyInt());

    screenWeapon = new ScreenWeapon(daiKombatAssetsManager,
        Map.of(
            Weapon.SHOTGUN, WeaponStats.builder().maxDistance(7).delayMls(500).build(),
            Weapon.GAUNTLET, WeaponStats.builder().maxDistance(1).delayMls(500).build(),
            Weapon.RAILGUN, WeaponStats.builder().maxDistance(10).delayMls(1_500).build())
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
  public void testGetWeaponDistance() {
    assertEquals(7,
        screenWeapon.getWeaponDistance(Weapon.SHOTGUN), 0.00001);
    assertEquals(1,
        screenWeapon.getWeaponDistance(Weapon.GAUNTLET), 0.00001);
    assertEquals(10,
        screenWeapon.getWeaponDistance(Weapon.RAILGUN), 0.00001);
  }

  @Test
  public void testAttackShotgun() {
    float volume = 0.5f;
    screenWeapon.changeWeapon(Weapon.SHOTGUN);
    assertTrue(screenWeapon.attack(player));
    verify(playerShotgunFireSound).play(volume);
    assertEquals(Weapon.SHOTGUN, screenWeapon.weaponBeingUsed);
    verifyNoInteractions(quadDamageAttackSound);
  }

  @Test
  public void testAttackShotgunQuadDamage() {
    float volume = 0.5f;
    doReturn(true).when(playerEffects).isPowerUpActive(PowerUpType.QUAD_DAMAGE);
    screenWeapon.changeWeapon(Weapon.SHOTGUN);
    assertTrue(screenWeapon.attack(player));
    verify(playerShotgunFireSound).play(volume);
    verify(quadDamageAttackSound).play(volume);
    assertEquals(Weapon.SHOTGUN, screenWeapon.weaponBeingUsed);
  }

  @Test
  public void testAttackPunch() {
    screenWeapon.changeWeapon(Weapon.GAUNTLET);
    assertTrue(screenWeapon.attack(player));
    verify(punchSound).play(anyFloat());
    assertEquals(Weapon.GAUNTLET, screenWeapon.weaponBeingUsed);
  }

  @Test
  public void testAttackTwiceNoDelay() {
    float volume = 0.5f;
    screenWeapon.changeWeapon(Weapon.SHOTGUN);
    assertTrue(screenWeapon.attack(player));
    assertFalse(screenWeapon.attack(player),
        "If no delay, then we shouldn't be able to attack");
    verify(playerShotgunFireSound).play(volume);
  }

  @Test
  public void testAttackTwiceDelay() throws InterruptedException {
    float volume = 0.5f;
    screenWeapon.changeWeapon(Weapon.SHOTGUN);
    assertTrue(screenWeapon.attack(player));
    // wait a little
    Thread.sleep(
        screenWeapon.weaponStates.get(Weapon.SHOTGUN).getAnimationDelayMls()
            + screenWeapon.weaponStates.get(Weapon.SHOTGUN).getBackoffDelayMls() + 50);
    assertTrue(screenWeapon.attack(player),
        "If we have a  delay, then we SHOULD be able to attack");
    verify(playerShotgunFireSound, times(2)).play(volume);
  }

  @Test
  public void testRegisterHitShotgun() {
    screenWeapon.registerHit(Weapon.SHOTGUN);
    // nothing happens. there is no hit sound for shotgun
    verifyNoInteractions(playerShotgunFireSound, punchSound, punchHitSound);
    verify(hitSound).play(anyFloat());
  }

  @Test
  public void testRegisterHitRailgun() {
    screenWeapon.registerHit(Weapon.RAILGUN);
    // nothing happens. there is no hit sound for shotgun
    verifyNoInteractions(playerShotgunFireSound, punchSound, punchHitSound);
    verify(hitSound).play(anyFloat());
  }

  @Test
  public void testRegisterHitPunch() {
    screenWeapon.registerHit(Weapon.GAUNTLET);
    verify(punchHitSound).play(anyFloat());
  }

  @Test
  public void testGetActiveWeaponForRenderingIdle() {
    var renderingData = screenWeapon.getActiveWeaponForRendering();
    assertEquals(shotgunIdleTexture, renderingData.getTextureRegion(),
        "Be default, idle texture should be returned");
  }

  @Test
  public void testGetActiveWeaponForRenderingShotgun() {
    screenWeapon.changeWeapon(Weapon.SHOTGUN);
    screenWeapon.attack(player);
    var renderingData = screenWeapon.getActiveWeaponForRendering();
    assertEquals(shotgunFireTexture, renderingData.getTextureRegion());
  }

  @Test
  public void testGetActiveWeaponForRenderingPunch() {
    screenWeapon.changeWeapon(Weapon.GAUNTLET);
    screenWeapon.attack(player);
    var renderingData = screenWeapon.getActiveWeaponForRendering();
    assertEquals(punchTexture, renderingData.getTextureRegion());
  }

  @Test
  public void testGetActiveWeaponForRenderingWaitAnimationFinish() throws InterruptedException {
    screenWeapon.changeWeapon(Weapon.SHOTGUN);
    screenWeapon.attack(player);
    // wait a little
    Thread.sleep(
        screenWeapon.weaponStates.get(Weapon.SHOTGUN).getAnimationDelayMls() + 50);
    var renderingData = screenWeapon.getActiveWeaponForRendering();
    assertEquals(shotgunIdleTexture, renderingData.getTextureRegion(),
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
    assertEquals(Weapon.RAILGUN, screenWeapon.getWeaponBeingUsed());
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
