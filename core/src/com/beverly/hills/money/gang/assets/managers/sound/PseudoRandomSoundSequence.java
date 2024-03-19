package com.beverly.hills.money.gang.assets.managers.sound;

import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import java.util.Arrays;
import java.util.Random;
import org.apache.commons.lang3.ArrayUtils;

public class PseudoRandomSoundSequence {

  private static final Random RANDOM = new Random();

  private final SoundRegistry[] sounds;
  private int currentSoundIdx;

  public PseudoRandomSoundSequence(SoundRegistry... sounds) {
    this.sounds = Arrays.copyOf(sounds, sounds.length);
    ArrayUtils.shuffle(this.sounds);
    currentSoundIdx = RANDOM.nextInt(sounds.length);
  }

  public SoundRegistry getNextSound() {
    currentSoundIdx++;
    return sounds[currentSoundIdx % sounds.length];
  }
}
