package com.beverly.hills.money.gang.entities.enemies;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.beverly.hills.money.gang.Configs;
import java.util.ArrayDeque;
import java.util.Queue;
import org.junit.jupiter.api.Test;

public class EnemyPlayerTest {

  @Test
  public void testGetSpeedEmptyQueue() {
    assertEquals(Configs.PLAYER_MOVE_SPEED, EnemyPlayer.getSpeed(createActions(0)));
  }

  @Test
  public void testGetSpeedEmptyQueueOneAction() {
    assertEquals(Configs.PLAYER_MOVE_SPEED, EnemyPlayer.getSpeed(createActions(1)));
  }

  @Test
  public void testGetSpeedEmptyQueueSlightlyClogged() {
    assertEquals(Configs.PLAYER_MOVE_SPEED * 1.15, EnemyPlayer.getSpeed(createActions(3)));
  }

  @Test
  public void testGetSpeedEmptyQueueClogged() {
    assertEquals(Configs.PLAYER_MOVE_SPEED * 1.25, EnemyPlayer.getSpeed(createActions(5)));
  }

  @Test
  public void testGetSpeedEmptyQueueVeryClogged() {
    assertEquals(Configs.PLAYER_MOVE_SPEED * 2, EnemyPlayer.getSpeed(createActions(12)));
  }

  @Test
  public void testGetSpeedEmptyQueueSuperClogged() {
    assertEquals(Configs.PLAYER_MOVE_SPEED * 3, EnemyPlayer.getSpeed(createActions(25)));
  }

  private Queue<EnemyPlayerAction> createActions(int numberOfActions) {
    Queue<EnemyPlayerAction> enemyPlayerActions = new ArrayDeque<>();
    for (int i = 0; i < numberOfActions; i++) {
      enemyPlayerActions.add(EnemyPlayerAction.builder().build());
    }
    return enemyPlayerActions;
  }

}
