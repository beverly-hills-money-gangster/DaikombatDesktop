package com.beverly.hills.money.gang.strategy;

import com.badlogic.gdx.math.Vector2;
import com.beverly.hills.money.gang.entities.enemies.EnemyPlayerAction;
import com.beverly.hills.money.gang.entities.enemies.EnemyPlayerActionType;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Consumer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequiredArgsConstructor
public class EnemyPlayerActionQueueStrategy {

  private static final Logger LOG = LoggerFactory.getLogger(EnemyPlayerActionQueueStrategy.class);

  protected static final int MAX_ACTION_QUEUE_CLOGGING = 15;

  private static final double TOO_MUCH_DISTANCE_TRAVELLED = 5;

  private int lastEventSequenceId = -1;

  @NonNull
  private final Queue<EnemyPlayerAction> actions;

  @NonNull
  private final Consumer<EnemyPlayerAction> onTeleport;

  @NonNull
  private final Consumer<Float> onSpeedChange;

  private final float defaultSpeed;

  public void enqueue(
      final EnemyPlayerAction enemyPlayerAction, Vector2 currentPosition, boolean visible) {
    boolean inOrder = enemyPlayerAction.getEventSequenceId() > lastEventSequenceId;
    if (enemyPlayerAction.getEnemyPlayerActionType() == EnemyPlayerActionType.TELEPORT) {
      skipEventsAndTeleport(enemyPlayerAction);
    } else if (inOrder && !visible) {
      skipEventsAndTeleport(enemyPlayerAction);
    } else if (inOrder && actions.size() > MAX_ACTION_QUEUE_CLOGGING) {
      skipEventsAndTeleport(enemyPlayerAction);
    } else if (inOrder && enemyPlayerAction.getRoute().dst(currentPosition)
        > TOO_MUCH_DISTANCE_TRAVELLED) {
      skipEventsAndTeleport(enemyPlayerAction);
    } else {
      onSpeedChange.accept(getSpeed(actions, this.defaultSpeed));
      // if out-of-order
      if (!inOrder) {
        switch (enemyPlayerAction.getEnemyPlayerActionType()) {
          case MOVE -> {
            LOG.warn(
                "MOVE event is out of order. Last event sequence id {} but given {}. Skip event.",
                lastEventSequenceId, enemyPlayerAction.getEventSequenceId());
            Optional.ofNullable(enemyPlayerAction.getOnComplete()).ifPresent(Runnable::run);
          }
          case ATTACK -> {
            LOG.warn(
                "ATTACK event is out of order. Last event sequence id {} but given {}",
                lastEventSequenceId, enemyPlayerAction.getEventSequenceId());
            Optional.ofNullable(enemyPlayerAction.getOnComplete()).ifPresent(Runnable::run);
          }
        }
        return;
      }
      actions.add(enemyPlayerAction);
    }
    lastEventSequenceId = enemyPlayerAction.getEventSequenceId();
  }

  private void skipEventsAndTeleport(EnemyPlayerAction enemyPlayerAction) {
    onTeleport.accept(enemyPlayerAction);
    actions.forEach(
        action -> Optional.ofNullable(action.getOnComplete()).ifPresent(Runnable::run));
    actions.clear();
    Optional.ofNullable(enemyPlayerAction.getOnComplete()).ifPresent(Runnable::run);
  }

  protected static float getSpeed(final Queue<EnemyPlayerAction> actions,
      final float defaultSpeed) {
    if (actions.size() > 20) {
      LOG.warn("Action queue is super clogged. Size {}", actions.size());
      return defaultSpeed * 4f;
    } else if (actions.size() > 15) {
      LOG.warn("Action queue is super clogged. Size {}", actions.size());
      return defaultSpeed * 3f;
    } else if (actions.size() > 10) {
      LOG.warn("Action queue is very clogged. Size {}", actions.size());
      return defaultSpeed * 2f;
    } else if (actions.stream().anyMatch(
        enemyPlayerAction -> enemyPlayerAction.getEnemyPlayerActionType()
            == EnemyPlayerActionType.ATTACK)) {
      return defaultSpeed * 2f;
    } else if (actions.size() >= 5) {
      LOG.warn("Action queue is clogged. Size {}", actions.size());
      return defaultSpeed * 1.25f;
    } else if (actions.size() > 2) {
      return defaultSpeed * 1.15f;
    } else {
      return defaultSpeed;
    }
  }

}
