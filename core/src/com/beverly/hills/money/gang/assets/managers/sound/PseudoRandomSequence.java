package com.beverly.hills.money.gang.assets.managers.sound;

import java.util.Arrays;
import java.util.Random;
import org.apache.commons.lang3.ArrayUtils;

public class PseudoRandomSequence<T> {

  private static final Random RANDOM = new Random();

  private final T[] sounds;
  private int currentSoundIdx;

  public PseudoRandomSequence(T... sounds) {
    this.sounds = Arrays.copyOf(sounds, sounds.length);
    ArrayUtils.shuffle(this.sounds);
    currentSoundIdx = RANDOM.nextInt(sounds.length);
  }

  public T getNext() {
    currentSoundIdx++;
    return sounds[currentSoundIdx % sounds.length];
  }
}
