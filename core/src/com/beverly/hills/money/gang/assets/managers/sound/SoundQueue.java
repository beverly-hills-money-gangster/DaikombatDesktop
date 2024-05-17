package com.beverly.hills.money.gang.assets.managers.sound;

import java.util.ArrayDeque;
import java.util.Optional;
import java.util.Queue;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SoundQueue {

  private final Queue<UserSettingSound> queue = new ArrayDeque<>();

  private final int delay;

  private final float volume;

  private long lastPlayedMls;

  public void addSound(UserSettingSound userSettingSound) {
    queue.add(userSettingSound);
  }

  public void play() {
    if (queue.isEmpty()) {
      return;
    }
    if (System.currentTimeMillis() > lastPlayedMls + delay) {
      Optional.ofNullable(queue.poll()).ifPresent(userSettingSound -> {
        userSettingSound.play(volume);
        lastPlayedMls = System.currentTimeMillis();
      });
    }
  }

  public int size() {
    return queue.size();
  }

}
