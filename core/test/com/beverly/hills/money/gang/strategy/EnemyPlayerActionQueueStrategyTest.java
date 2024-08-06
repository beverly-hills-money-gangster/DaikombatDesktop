package com.beverly.hills.money.gang.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.badlogic.gdx.math.Vector2;
import com.beverly.hills.money.gang.entities.enemies.EnemyPlayerAction;
import com.beverly.hills.money.gang.entities.enemies.EnemyPlayerActionType;
import com.beverly.hills.money.gang.screens.ui.weapon.Weapon;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EnemyPlayerActionQueueStrategyTest {

  private int sequence = 0;

  private final int defaultSpeed = 5;

  private Consumer<Float> onSpeedChange;

  private Consumer<EnemyPlayerAction> onTooMuchDistanceTravelled;

  @BeforeEach
  public void setUp() {
    onSpeedChange = mock(Consumer.class);
    onTooMuchDistanceTravelled = mock(Consumer.class);
  }

  @Test
  public void testEnqueueOverLimit() {
    Deque<EnemyPlayerAction> enemyPlayerActions = new ArrayDeque<>();
    EnemyPlayerActionQueueStrategy enemyPlayerActionQueueStrategy = new EnemyPlayerActionQueueStrategy(
        enemyPlayerActions, onTooMuchDistanceTravelled, onSpeedChange, defaultSpeed);
    var currentPosition = new Vector2(0, 0);

    for (int i = 0; i <= EnemyPlayerActionQueueStrategy.MAX_ACTION_QUEUE_CLOGGING; i++) {
      var someAction = EnemyPlayerAction.builder()
          .enemyPlayerActionType(EnemyPlayerActionType.MOVE)
          .direction(new Vector2(0, 1))
          .route(new Vector2(0, 1))
          .eventSequenceId(i).build();
      enemyPlayerActionQueueStrategy.enqueue(someAction, currentPosition);
    }
    var someOverTheLimitAction = EnemyPlayerAction.builder()
        .enemyPlayerActionType(EnemyPlayerActionType.MOVE)
        .direction(new Vector2(0, 1))
        .route(new Vector2(0, 1))
        .eventSequenceId(EnemyPlayerActionQueueStrategy.MAX_ACTION_QUEUE_CLOGGING + 1).build();
    // over the limit
    assertThrows(IllegalStateException.class,
        () -> enemyPlayerActionQueueStrategy.enqueue(someOverTheLimitAction, currentPosition));
  }

  @Test
  public void testEnqueueInOrder() {
    Deque<EnemyPlayerAction> enemyPlayerActions = new ArrayDeque<>();
    EnemyPlayerActionQueueStrategy enemyPlayerActionQueueStrategy = new EnemyPlayerActionQueueStrategy(
        enemyPlayerActions, onTooMuchDistanceTravelled, onSpeedChange, defaultSpeed);
    var currentPosition = new Vector2(0, 0);
    var firstAction = EnemyPlayerAction.builder()
        .enemyPlayerActionType(EnemyPlayerActionType.MOVE)
        .direction(new Vector2(0, 1))
        .route(new Vector2(0, 1))
        .eventSequenceId(0).build();
    var secondAction = EnemyPlayerAction.builder()
        .enemyPlayerActionType(EnemyPlayerActionType.MOVE)
        .direction(new Vector2(0, 1))
        .route(new Vector2(0, 2))
        .eventSequenceId(2).build();
    enemyPlayerActionQueueStrategy.enqueue(firstAction, currentPosition);
    enemyPlayerActionQueueStrategy.enqueue(secondAction, currentPosition);
    assertEquals(2, enemyPlayerActions.size());
  }

  @Test
  public void testEnqueueOutOfOrderPunch() {
    Runnable onPunchComplete = spy(Runnable.class);
    Deque<EnemyPlayerAction> enemyPlayerActions = new ArrayDeque<>();
    EnemyPlayerActionQueueStrategy enemyPlayerActionQueueStrategy = new EnemyPlayerActionQueueStrategy(
        enemyPlayerActions, onTooMuchDistanceTravelled, onSpeedChange, defaultSpeed);
    var currentPosition = new Vector2(0, 0);
    var punch = EnemyPlayerAction.builder()
        .enemyPlayerActionType(EnemyPlayerActionType.ATTACK)
        .weapon(Weapon.GAUNTLET)
        .direction(new Vector2(0, 1))
        .route(new Vector2(0, 2))
        .onComplete(onPunchComplete)
        .eventSequenceId(0).build();
    var move = EnemyPlayerAction.builder()
        .enemyPlayerActionType(EnemyPlayerActionType.MOVE)
        .direction(new Vector2(0, 1))
        .route(new Vector2(0, 1))
        .eventSequenceId(1).build();

    enemyPlayerActionQueueStrategy.enqueue(move, currentPosition);
    assertEquals(move, enemyPlayerActions.getLast());
    enemyPlayerActionQueueStrategy.enqueue(punch, new Vector2(0, 1));
    assertEquals(move, enemyPlayerActions.getLast());
    assertEquals(1, enemyPlayerActions.size(),
        "Should be just 1 MOVE event because PUNCH is out-of-order");
    // the callback should execute though. otherwise, the player might not get attacked
    verify(onPunchComplete).run();
  }

  @Test
  public void testEnqueueOutOfOrderShoot() {
    Runnable onShootComplete = spy(Runnable.class);
    Deque<EnemyPlayerAction> enemyPlayerActions = new ArrayDeque<>();
    EnemyPlayerActionQueueStrategy enemyPlayerActionQueueStrategy = new EnemyPlayerActionQueueStrategy(
        enemyPlayerActions, onTooMuchDistanceTravelled, onSpeedChange, defaultSpeed);
    var currentPosition = new Vector2(0, 0);
    var shoot = EnemyPlayerAction.builder()
        .enemyPlayerActionType(EnemyPlayerActionType.ATTACK)
        .direction(new Vector2(0, 1))
        .weapon(Weapon.SHOTGUN)
        .route(new Vector2(0, 2))
        .onComplete(onShootComplete)
        .eventSequenceId(0).build();
    var move = EnemyPlayerAction.builder()
        .enemyPlayerActionType(EnemyPlayerActionType.MOVE)
        .direction(new Vector2(0, 1))
        .route(new Vector2(0, 1))
        .eventSequenceId(1).build();

    enemyPlayerActionQueueStrategy.enqueue(move, currentPosition);
    assertEquals(move, enemyPlayerActions.getLast());
    enemyPlayerActionQueueStrategy.enqueue(shoot, new Vector2(0, 1));
    assertEquals(move, enemyPlayerActions.getLast());
    assertEquals(1, enemyPlayerActions.size(),
        "Should be just 1 MOVE event because SHOOT is out-of-order");
    // the callback should execute though. otherwise, the player might not get attacked
    verify(onShootComplete).run();
  }

  @Test
  public void testEnqueueOutOfOrderMoves() {
    Deque<EnemyPlayerAction> enemyPlayerActions = new ArrayDeque<>();
    EnemyPlayerActionQueueStrategy enemyPlayerActionQueueStrategy = new EnemyPlayerActionQueueStrategy(
        enemyPlayerActions, onTooMuchDistanceTravelled, onSpeedChange, defaultSpeed);
    var currentPosition = new Vector2(0, 0);
    var firstAction = EnemyPlayerAction.builder()
        .enemyPlayerActionType(EnemyPlayerActionType.MOVE)
        .direction(new Vector2(0, 1))
        .route(new Vector2(0, 1))
        .eventSequenceId(0).build();
    var secondAction = EnemyPlayerAction.builder()
        .enemyPlayerActionType(EnemyPlayerActionType.MOVE)
        .direction(new Vector2(0, 1))
        .route(new Vector2(0, 2))
        .eventSequenceId(2).build();
    // out of order
    enemyPlayerActionQueueStrategy.enqueue(secondAction, currentPosition);
    enemyPlayerActionQueueStrategy.enqueue(firstAction, currentPosition);

    assertEquals(1, enemyPlayerActions.size(),
        "Only one action is expected to be enqueued because they are out of order");
    assertEquals(secondAction, enemyPlayerActions.getLast());
  }

  @Test
  public void testEnqueueOutOfOrderTeleport2Move1Move3() {
    Deque<EnemyPlayerAction> enemyPlayerActions = new ArrayDeque<>();
    EnemyPlayerActionQueueStrategy enemyPlayerActionQueueStrategy = new EnemyPlayerActionQueueStrategy(
        enemyPlayerActions, onTooMuchDistanceTravelled, onSpeedChange, defaultSpeed);

    var move1 = EnemyPlayerAction.builder()
        .enemyPlayerActionType(EnemyPlayerActionType.MOVE)
        .direction(new Vector2(0, 1))
        .route(new Vector2(0, 1))
        .eventSequenceId(1).build();
    var teleport2 = EnemyPlayerAction.builder()
        .enemyPlayerActionType(EnemyPlayerActionType.MOVE)
        .direction(new Vector2(0, 1))
        .route(new Vector2(0, 2))
        .eventSequenceId(2).build();
    var move3 = EnemyPlayerAction.builder()
        .enemyPlayerActionType(EnemyPlayerActionType.MOVE)
        .direction(new Vector2(0, 1))
        .route(new Vector2(0, 101))
        .eventSequenceId(3).build();

    enemyPlayerActionQueueStrategy.enqueue(teleport2, new Vector2(0, 0));
    assertEquals(teleport2, enemyPlayerActions.getLast());
    enemyPlayerActionQueueStrategy.enqueue(move1, new Vector2(0, 100));
    assertEquals(teleport2, enemyPlayerActions.getLast());
    enemyPlayerActionQueueStrategy.enqueue(move3, new Vector2(0, 100));
    assertEquals(move3, enemyPlayerActions.getLast());
    verifyNoInteractions(onTooMuchDistanceTravelled);

    assertEquals(2, enemyPlayerActions.size(),
        "Move 1 should be ignored because it's out of order");
  }

  @Test
  public void testEnqueueOutOfOrderTeleport2Move3Move1() {
    Deque<EnemyPlayerAction> enemyPlayerActions = new ArrayDeque<>();
    EnemyPlayerActionQueueStrategy enemyPlayerActionQueueStrategy = new EnemyPlayerActionQueueStrategy(
        enemyPlayerActions, onTooMuchDistanceTravelled, onSpeedChange, defaultSpeed);

    var move1 = EnemyPlayerAction.builder()
        .enemyPlayerActionType(EnemyPlayerActionType.MOVE)
        .direction(new Vector2(0, 1))
        .route(new Vector2(0, 1))
        .eventSequenceId(1).build();
    var teleport2 = EnemyPlayerAction.builder()
        .enemyPlayerActionType(EnemyPlayerActionType.MOVE)
        .direction(new Vector2(0, 1))
        .route(new Vector2(0, 2))
        .eventSequenceId(2).build();
    var move3 = EnemyPlayerAction.builder()
        .enemyPlayerActionType(EnemyPlayerActionType.MOVE)
        .direction(new Vector2(0, 1))
        .route(new Vector2(0, 101))
        .eventSequenceId(3).build();

    enemyPlayerActionQueueStrategy.enqueue(teleport2, new Vector2(0, 0));
    assertEquals(teleport2, enemyPlayerActions.getLast());
    enemyPlayerActionQueueStrategy.enqueue(move3, new Vector2(0, 100));
    assertEquals(move3, enemyPlayerActions.getLast());
    enemyPlayerActionQueueStrategy.enqueue(move1, new Vector2(0, 101));
    assertEquals(move3, enemyPlayerActions.getLast());

    verifyNoInteractions(onTooMuchDistanceTravelled);

    assertEquals(2, enemyPlayerActions.size(),
        "Move 1 should be ignored because it's out of order");
  }

  @Test
  public void testEnqueueOutOfOrderMove1Move3Teleport2() {
    Deque<EnemyPlayerAction> enemyPlayerActions = new ArrayDeque<>();
    EnemyPlayerActionQueueStrategy enemyPlayerActionQueueStrategy = new EnemyPlayerActionQueueStrategy(
        enemyPlayerActions, onTooMuchDistanceTravelled, onSpeedChange, defaultSpeed);

    var move1 = EnemyPlayerAction.builder()
        .enemyPlayerActionType(EnemyPlayerActionType.MOVE)
        .direction(new Vector2(0, 1))
        .route(new Vector2(0, 1))
        .eventSequenceId(1).build();
    var teleport2 = EnemyPlayerAction.builder()
        .enemyPlayerActionType(EnemyPlayerActionType.MOVE)
        .direction(new Vector2(0, 1))
        .route(new Vector2(0, 2))
        .eventSequenceId(2).build();
    var move3 = EnemyPlayerAction.builder()
        .enemyPlayerActionType(EnemyPlayerActionType.MOVE)
        .direction(new Vector2(0, 1))
        .route(new Vector2(0, 101))
        .eventSequenceId(3).build();

    enemyPlayerActionQueueStrategy.enqueue(move1, new Vector2(0, 0));
    assertEquals(move1, enemyPlayerActions.getLast());

    enemyPlayerActionQueueStrategy.enqueue(move3, new Vector2(0, 1));
    verify(onTooMuchDistanceTravelled).accept(move3);
    assertTrue(enemyPlayerActions.isEmpty(), "Should be empty because it was a big leap");

    enemyPlayerActionQueueStrategy.enqueue(teleport2, new Vector2(0, 101));
    assertTrue(enemyPlayerActions.isEmpty(), "Should be empty because it's out of order");
  }

  @Test
  public void testEnqueueOutOfOrderMove1Teleport2Move3() {
    Deque<EnemyPlayerAction> enemyPlayerActions = new ArrayDeque<>();
    EnemyPlayerActionQueueStrategy enemyPlayerActionQueueStrategy = new EnemyPlayerActionQueueStrategy(
        enemyPlayerActions, onTooMuchDistanceTravelled, onSpeedChange, defaultSpeed);

    var move1 = EnemyPlayerAction.builder()
        .enemyPlayerActionType(EnemyPlayerActionType.MOVE)
        .direction(new Vector2(0, 1))
        .route(new Vector2(0, 1))
        .eventSequenceId(1).build();
    var teleport2 = EnemyPlayerAction.builder()
        .enemyPlayerActionType(EnemyPlayerActionType.MOVE)
        .direction(new Vector2(0, 1))
        .route(new Vector2(0, 2))
        .eventSequenceId(2).build();
    var move3 = EnemyPlayerAction.builder()
        .enemyPlayerActionType(EnemyPlayerActionType.MOVE)
        .direction(new Vector2(0, 1))
        .route(new Vector2(0, 101))
        .eventSequenceId(3).build();

    enemyPlayerActionQueueStrategy.enqueue(move1, new Vector2(0, 0));
    enemyPlayerActionQueueStrategy.enqueue(teleport2, new Vector2(0, 1));
    enemyPlayerActionQueueStrategy.enqueue(move3, new Vector2(0, 100));
    verifyNoInteractions(onTooMuchDistanceTravelled);

    assertEquals(3, enemyPlayerActions.size());
  }

  @Test
  public void testEnqueueOutOfOrderMove3Move1Teleport2() {
    Deque<EnemyPlayerAction> enemyPlayerActions = new ArrayDeque<>();
    EnemyPlayerActionQueueStrategy enemyPlayerActionQueueStrategy = new EnemyPlayerActionQueueStrategy(
        enemyPlayerActions, onTooMuchDistanceTravelled, onSpeedChange, defaultSpeed);

    var move1 = EnemyPlayerAction.builder()
        .enemyPlayerActionType(EnemyPlayerActionType.MOVE)
        .direction(new Vector2(0, 1))
        .route(new Vector2(0, 1))
        .eventSequenceId(1).build();
    var teleport2 = EnemyPlayerAction.builder()
        .enemyPlayerActionType(EnemyPlayerActionType.MOVE)
        .direction(new Vector2(0, 1))
        .route(new Vector2(0, 2))
        .eventSequenceId(2).build();
    var move3 = EnemyPlayerAction.builder()
        .enemyPlayerActionType(EnemyPlayerActionType.MOVE)
        .direction(new Vector2(0, 1))
        .route(new Vector2(0, 101))
        .eventSequenceId(3).build();

    enemyPlayerActionQueueStrategy.enqueue(move3, new Vector2(0, 0));
    assertTrue(enemyPlayerActions.isEmpty(), "Should be empty because it's a very big leap");
    verify(onTooMuchDistanceTravelled).accept(move3);

    enemyPlayerActionQueueStrategy.enqueue(move1, new Vector2(0, 101));
    assertTrue(enemyPlayerActions.isEmpty(), "Should be empty because it's out of order");

    enemyPlayerActionQueueStrategy.enqueue(teleport2, new Vector2(0, 0));
    assertTrue(enemyPlayerActions.isEmpty(), "Should be empty because it's out of order");
  }

  @Test
  public void testEnqueueOutOfOrderMove3Teleport2Move1() {
    Deque<EnemyPlayerAction> enemyPlayerActions = new ArrayDeque<>();
    EnemyPlayerActionQueueStrategy enemyPlayerActionQueueStrategy = new EnemyPlayerActionQueueStrategy(
        enemyPlayerActions, onTooMuchDistanceTravelled, onSpeedChange, defaultSpeed);

    var move1 = EnemyPlayerAction.builder()
        .enemyPlayerActionType(EnemyPlayerActionType.MOVE)
        .direction(new Vector2(0, 1))
        .route(new Vector2(0, 1))
        .eventSequenceId(1).build();
    var teleport2 = EnemyPlayerAction.builder()
        .enemyPlayerActionType(EnemyPlayerActionType.MOVE)
        .direction(new Vector2(0, 1))
        .route(new Vector2(0, 2))
        .eventSequenceId(2).build();
    var move3 = EnemyPlayerAction.builder()
        .enemyPlayerActionType(EnemyPlayerActionType.MOVE)
        .direction(new Vector2(0, 1))
        .route(new Vector2(0, 101))
        .eventSequenceId(3).build();

    enemyPlayerActionQueueStrategy.enqueue(move3, new Vector2(0, 0));
    assertTrue(enemyPlayerActions.isEmpty(), "Should be empty because it's a very big leap");
    verify(onTooMuchDistanceTravelled).accept(move3);

    enemyPlayerActionQueueStrategy.enqueue(teleport2, new Vector2(0, 101));
    assertTrue(enemyPlayerActions.isEmpty(), "Should be empty because it's out of order");

    enemyPlayerActionQueueStrategy.enqueue(move1, new Vector2(0, 101));
    assertTrue(enemyPlayerActions.isEmpty(), "Should be empty because it's out of order");

  }

  @Test
  public void testGetSpeedEmptyQueue() {
    assertEquals(defaultSpeed,
        EnemyPlayerActionQueueStrategy.getSpeed(createActions(0), defaultSpeed));
  }

  @Test
  public void testGetSpeedEmptyQueueOneAction() {
    assertEquals(defaultSpeed,
        EnemyPlayerActionQueueStrategy.getSpeed(createActions(1), defaultSpeed));
  }

  @Test
  public void testGetSpeedEmptyQueueSlightlyClogged() {
    assertEquals(defaultSpeed * 1.15,
        EnemyPlayerActionQueueStrategy.getSpeed(createActions(3), defaultSpeed));
  }

  @Test
  public void testGetSpeedEmptyQueueClogged() {
    assertEquals(defaultSpeed * 1.25,
        EnemyPlayerActionQueueStrategy.getSpeed(createActions(5), defaultSpeed));
  }

  @Test
  public void testGetSpeedEmptyQueueVeryClogged() {
    assertEquals(defaultSpeed * 2,
        EnemyPlayerActionQueueStrategy.getSpeed(createActions(12), defaultSpeed));
  }

  @Test
  public void testGetSpeedEmptyQueueSuperClogged() {
    assertEquals(defaultSpeed * 3,
        EnemyPlayerActionQueueStrategy.getSpeed(createActions(25), defaultSpeed));
  }

  private Deque<EnemyPlayerAction> createActions(int numberOfActions) {
    Deque<EnemyPlayerAction> enemyPlayerActions = new ArrayDeque<>();
    for (int i = 0; i < numberOfActions; i++) {
      enemyPlayerActions.add(EnemyPlayerAction.builder().eventSequenceId(sequence++).build());
    }
    return enemyPlayerActions;
  }

}
