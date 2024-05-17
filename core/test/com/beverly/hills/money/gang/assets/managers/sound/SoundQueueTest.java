package com.beverly.hills.money.gang.assets.managers.sound;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

public class SoundQueueTest {

  @Test
  public void testPlayEmpty() {
    SoundQueue soundQueue = new SoundQueue(1000, 1f);
    soundQueue.play(); // nothing happens
    assertEquals(0, soundQueue.size());
  }

  @Test
  public void testPlay() {
    SoundQueue soundQueue = new SoundQueue(1000, 1f);
    UserSettingSound userSettingSound = mock(UserSettingSound.class);
    soundQueue.addSound(userSettingSound);
    assertEquals(1, soundQueue.size());
    soundQueue.play();
    verify(userSettingSound).play(1f);
    assertEquals(0, soundQueue.size());
  }


  @Test
  public void testPlayPrematurely() {
    int delay = 500;
    SoundQueue soundQueue = new SoundQueue(delay, 1f);
    UserSettingSound userSettingSound1 = mock(UserSettingSound.class);
    UserSettingSound userSettingSound2 = mock(UserSettingSound.class);
    soundQueue.addSound(userSettingSound1);
    soundQueue.addSound(userSettingSound2);
    assertEquals(2, soundQueue.size());
    soundQueue.play();
    soundQueue.play(); // play with no delay
    verify(userSettingSound1).play(1f); // played only once
    assertEquals(1, soundQueue.size(),
        "Should be 1 because the second sound is not yet to be played");
  }

  @Test
  public void testPlayTwoSound() throws InterruptedException {
    int delay = 500;
    SoundQueue soundQueue = new SoundQueue(delay, 1f);
    UserSettingSound userSettingSound1 = mock(UserSettingSound.class);
    UserSettingSound userSettingSound2 = mock(UserSettingSound.class);
    soundQueue.addSound(userSettingSound1);
    soundQueue.addSound(userSettingSound2);
    assertEquals(2, soundQueue.size());
    soundQueue.play();
    Thread.sleep(delay + 50); // wait
    soundQueue.play(); // play again
    InOrder inOrder = Mockito.inOrder(userSettingSound1, userSettingSound2);
    inOrder.verify(userSettingSound1).play(1f);
    inOrder.verify(userSettingSound2).play(1f);
    assertEquals(0, soundQueue.size());
  }

}
