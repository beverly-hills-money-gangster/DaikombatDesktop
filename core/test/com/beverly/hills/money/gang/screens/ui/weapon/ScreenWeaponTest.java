package com.beverly.hills.money.gang.screens.ui.weapon;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.beverly.hills.money.gang.Configs;
import com.beverly.hills.money.gang.assets.managers.DaiKombatAssetsManager;
import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import com.beverly.hills.money.gang.assets.managers.registry.TexturesRegistry;
import com.beverly.hills.money.gang.assets.managers.sound.UserSettingSound;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

public class ScreenWeaponTest {

    private ScreenWeapon screenWeapon;

    private DaiKombatAssetsManager daiKombatAssetsManager;

    private UserSettingSound playerShotgunFireSound;
    private UserSettingSound punchSound;
    private UserSettingSound punchHitSound;

    private TextureRegion shotgunFireTexture;

    private TextureRegion shotgunIdleTexture;

    private TextureRegion punchTexture;


    @BeforeEach
    public void setUp() {
        shotgunIdleTexture = mock(TextureRegion.class);
        shotgunFireTexture = mock(TextureRegion.class);
        punchTexture = mock(TextureRegion.class);

        playerShotgunFireSound = mock(UserSettingSound.class);
        punchSound = mock(UserSettingSound.class);
        punchHitSound = mock(UserSettingSound.class);

        daiKombatAssetsManager = mock(DaiKombatAssetsManager.class);

        doReturn(playerShotgunFireSound).when(daiKombatAssetsManager).getUserSettingSound(SoundRegistry.PLAYER_SHOTGUN);
        doReturn(punchSound).when(daiKombatAssetsManager).getUserSettingSound(SoundRegistry.PUNCH_THROWN);
        doReturn(punchHitSound).when(daiKombatAssetsManager).getUserSettingSound(SoundRegistry.PUNCH_HIT);

        doReturn(shotgunIdleTexture).when(daiKombatAssetsManager).getTextureRegion(
                eq(TexturesRegistry.GUN_IDLE), anyInt(), anyInt(), anyInt(), anyInt());
        doReturn(shotgunFireTexture).when(daiKombatAssetsManager).getTextureRegion(
                eq(TexturesRegistry.GUN_SHOOT), anyInt(), anyInt(), anyInt(), anyInt());
        doReturn(punchTexture).when(daiKombatAssetsManager).getTextureRegion(
                eq(TexturesRegistry.PUNCH), anyInt(), anyInt(), anyInt(), anyInt());

        screenWeapon = new ScreenWeapon(daiKombatAssetsManager);
    }

    @Test
    public void testDefaults() {
        assertEquals(ScreenWeapon.Weapon.values().length, screenWeapon.weaponStates.size(),
                "All weapons must be registered");
        Arrays.stream(ScreenWeapon.Weapon.values())
                .forEach(weapon -> assertNotNull(screenWeapon.weaponStates.get(weapon),
                        "Weapon " + weapon.name() + " must be registered"));
    }

    @Test
    public void testCanAttack() {
        assertTrue(screenWeapon.canAttack(), "By default, you should be able to attack");
    }

    @Test
    public void testCanAttackAfterRecentAttack() {
        screenWeapon.attack(ScreenWeapon.Weapon.SHOTGUN, 0);
        assertFalse(screenWeapon.canAttack(), "You can't attack right after. There must be some cool down");
    }

    @Test
    public void testCanAttackAfterWait() throws InterruptedException {
        screenWeapon.attack(ScreenWeapon.Weapon.SHOTGUN, 0);
        // wait a little
        Thread.sleep(screenWeapon.weaponStates.get(ScreenWeapon.Weapon.SHOTGUN).getAnimationDelayMls() + 50);
        assertTrue(screenWeapon.canAttack(), "Must be ok to attack as we waited");
    }

    @Test
    public void testGetWeaponDistance() {
        assertEquals(Configs.SHOOTING_DISTANCE,
                screenWeapon.getWeaponDistance(ScreenWeapon.Weapon.SHOTGUN), 0.00001);
        assertEquals(Configs.MELEE_DISTANCE,
                screenWeapon.getWeaponDistance(ScreenWeapon.Weapon.GAUNTLET), 0.00001);
    }

    @Test
    public void testAttackShotgun() {
        float volume = 0.5f;
        assertTrue(screenWeapon.attack(ScreenWeapon.Weapon.SHOTGUN, volume));
        verify(playerShotgunFireSound).play(volume);
        assertEquals(ScreenWeapon.Weapon.SHOTGUN, screenWeapon.weaponBeingUsed);

    }

    @Test
    public void testAttackPunch() {
        float volume = 0.7f;
        assertTrue(screenWeapon.attack(ScreenWeapon.Weapon.GAUNTLET, volume));
        verify(punchSound).play(volume);
        assertEquals(ScreenWeapon.Weapon.GAUNTLET, screenWeapon.weaponBeingUsed);
    }

    @Test
    public void testAttackTwiceNoDelay() {
        float volume = 0.5f;
        assertTrue(screenWeapon.attack(ScreenWeapon.Weapon.SHOTGUN, volume));
        assertFalse(screenWeapon.attack(ScreenWeapon.Weapon.SHOTGUN, volume),
                "If no delay, then we shouldn't be able to attack");
        verify(playerShotgunFireSound).play(volume);
    }

    @Test
    public void testAttackTwiceDelay() throws InterruptedException {
        float volume = 0.5f;
        assertTrue(screenWeapon.attack(ScreenWeapon.Weapon.SHOTGUN, volume));
        // wait a little
        Thread.sleep(screenWeapon.weaponStates.get(ScreenWeapon.Weapon.SHOTGUN).getAnimationDelayMls() + 50);
        assertTrue(screenWeapon.attack(ScreenWeapon.Weapon.SHOTGUN, volume),
                "If we have a  delay, then we SHOULD be able to attack");
        verify(playerShotgunFireSound, times(2)).play(volume);
    }

    @Test
    public void testRegisterHitShotgun() {
        screenWeapon.registerHit(ScreenWeapon.Weapon.SHOTGUN, 0.5f);
        // nothing happens. there is no hit sound for shotgun
        verifyNoInteractions(playerShotgunFireSound, punchSound, punchHitSound);
    }

    @Test
    public void testRegisterHitPunch() {
        screenWeapon.registerHit(ScreenWeapon.Weapon.GAUNTLET, 0.5f);
        verify(punchHitSound).play(0.5f);
    }

    @Test
    public void testGetActiveWeaponForRenderingIdle() {
        var renderingData = screenWeapon.getActiveWeaponForRendering();
        assertEquals(shotgunIdleTexture, renderingData.getTextureRegion(), "Be default, idle texture should be returned");
    }

    @Test
    public void testGetActiveWeaponForRenderingShotgun() {
        screenWeapon.attack(ScreenWeapon.Weapon.SHOTGUN, 0.5f);
        var renderingData = screenWeapon.getActiveWeaponForRendering();
        assertEquals(shotgunFireTexture, renderingData.getTextureRegion());
    }

    @Test
    public void testGetActiveWeaponForRenderingPunch() {
        screenWeapon.attack(ScreenWeapon.Weapon.GAUNTLET, 0.5f);
        var renderingData = screenWeapon.getActiveWeaponForRendering();
        assertEquals(punchTexture, renderingData.getTextureRegion());
    }

    @Test
    public void testGetActiveWeaponForRenderingWaitAnimationFinish() throws InterruptedException {
        screenWeapon.attack(ScreenWeapon.Weapon.SHOTGUN, 0.5f);
        // wait a little
        Thread.sleep(screenWeapon.weaponStates.get(ScreenWeapon.Weapon.SHOTGUN).getAnimationDelayMls() + 50);
        var renderingData = screenWeapon.getActiveWeaponForRendering();
        assertEquals(shotgunIdleTexture, renderingData.getTextureRegion(),
                "After animation finish, idle texture should be returned");
    }

}
