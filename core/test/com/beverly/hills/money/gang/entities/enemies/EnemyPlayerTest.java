package com.beverly.hills.money.gang.entities.enemies;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayDeque;
import java.util.Queue;
import org.junit.jupiter.api.Test;

public class EnemyPlayerTest {

  private final int defaultSpeed = 5;

  @Test
  public void testGetSpeedEmptyQueue() {
    assertEquals(defaultSpeed, EnemyPlayer.getSpeed(createActions(0), defaultSpeed));
  }

  @Test
  public void testGetSpeedEmptyQueueOneAction() {
    assertEquals(defaultSpeed, EnemyPlayer.getSpeed(createActions(1), defaultSpeed));
  }

  @Test
  public void testGetSpeedEmptyQueueSlightlyClogged() {
    assertEquals(defaultSpeed * 1.15, EnemyPlayer.getSpeed(createActions(3), defaultSpeed));
  }

  @Test
  public void testGetSpeedEmptyQueueClogged() {
    assertEquals(defaultSpeed * 1.25, EnemyPlayer.getSpeed(createActions(5), defaultSpeed));
  }

  @Test
  public void testGetSpeedEmptyQueueVeryClogged() {
    assertEquals(defaultSpeed * 2, EnemyPlayer.getSpeed(createActions(12), defaultSpeed));
  }

  @Test
  public void testGetSpeedEmptyQueueSuperClogged() {
    assertEquals(defaultSpeed * 3, EnemyPlayer.getSpeed(createActions(25), defaultSpeed));
  }

  private Queue<EnemyPlayerAction> createActions(int numberOfActions) {
    Queue<EnemyPlayerAction> enemyPlayerActions = new ArrayDeque<>();
    for (int i = 0; i < numberOfActions; i++) {
      enemyPlayerActions.add(EnemyPlayerAction.builder().build());
    }
    return enemyPlayerActions;
  }

}
