package com.beverly.hills.money.gang.assets.managers.sound;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static com.beverly.hills.money.gang.Constants.SHOOTING_SOUND_FREQ_MLS;
import static org.mockito.Mockito.*;

public class ShootingSoundTest {

    private ShootingSound shootingSound;

    private UserSettingSound userSettingSound;

    @BeforeEach
    public void setUp() {
        userSettingSound = mock(UserSettingSound.class);
        shootingSound = new ShootingSound(userSettingSound);
        ShootingSound.init();
    }

    @Test
    public void testPlayFirstTime() {
        shootingSound.play(SoundVolumeType.LOUD);
        verify(userSettingSound).play(SoundVolumeType.LOUD);
    }

    @Test
    public void testPlayTooOften() {
        // played recently
        shootingSound.play(SoundVolumeType.LOUD);

        // play again
        shootingSound.play(SoundVolumeType.LOUD);

        verify(userSettingSound, times(1)).play(SoundVolumeType.LOUD); // play only once
    }

    @Test
    public void testPlayWait() throws InterruptedException {
        // played recently
        shootingSound.play(SoundVolumeType.LOUD);

        Thread.sleep(SHOOTING_SOUND_FREQ_MLS + 10);

        // play again
        shootingSound.play(SoundVolumeType.LOUD);

        verify(userSettingSound, times(2)).play(SoundVolumeType.LOUD); // play twice
    }

    @Test
    public void testPlayDifferentSounds() {
        Arrays.stream(SoundVolumeType.values())
                .forEach(soundVolumeType -> shootingSound.play(soundVolumeType));

        Arrays.stream(SoundVolumeType.values())
                .forEach(soundVolumeType -> verify(userSettingSound).play(soundVolumeType));

    }

    @Test
    public void testPlayDifferentSoundsTwice() {
        // first time
        Arrays.stream(SoundVolumeType.values())
                .forEach(soundVolumeType -> shootingSound.play(soundVolumeType));

        // second time
        Arrays.stream(SoundVolumeType.values())
                .forEach(soundVolumeType -> shootingSound.play(soundVolumeType));


        // played once only
        Arrays.stream(SoundVolumeType.values())
                .forEach(soundVolumeType -> verify(userSettingSound, times(1))
                        .play(soundVolumeType));

    }

    @Test
    public void testPlayDifferentSoundsWait() throws InterruptedException {
        // first time
        Arrays.stream(SoundVolumeType.values())
                .forEach(soundVolumeType -> shootingSound.play(soundVolumeType));

        Thread.sleep(SHOOTING_SOUND_FREQ_MLS + 10);

        // second time
        Arrays.stream(SoundVolumeType.values())
                .forEach(soundVolumeType -> shootingSound.play(soundVolumeType));


        // played once only
        Arrays.stream(SoundVolumeType.values())
                .forEach(soundVolumeType -> verify(userSettingSound, times(2))
                        .play(soundVolumeType));

    }
}
