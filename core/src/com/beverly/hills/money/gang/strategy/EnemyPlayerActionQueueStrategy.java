package com.beverly.hills.money.gang.strategy;

import com.badlogic.gdx.math.Vector2;
import com.beverly.hills.money.gang.entities.enemies.EnemyPlayerAction;
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

  protected static final int MAX_ACTION_QUEUE_CLOGGING = 30;

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
      final EnemyPlayerAction enemyPlayerAction, Vector2 currentPosition) {
    // if clogged
    if (actions.size() > MAX_ACTION_QUEUE_CLOGGING) {
      LOG.warn("The queue is totally clogged. Clear all events.");
      skipEventsAndTeleport(enemyPlayerAction);
    }
    onSpeedChange.accept(getSpeed(actions, this.defaultSpeed));
    // if out-of-order
    if (enemyPlayerAction.getEventSequenceId() < lastEventSequenceId) {
      switch (enemyPlayerAction.getEnemyPlayerActionType()) {
        case MOVE -> LOG.warn(
            "MOVE event is out of order. Last event sequence id {} but given {}. Skip event.",
            lastEventSequenceId, enemyPlayerAction.getEventSequenceId());
        case ATTACK -> {
          LOG.warn(
              "ATTACK event is out of order. Last event sequence id {} but given {}",
              lastEventSequenceId, enemyPlayerAction.getEventSequenceId());
          Optional.ofNullable(enemyPlayerAction.getOnComplete()).ifPresent(Runnable::run);
        }
      }
      return;
    }
    // if in order
    int sequenceDiff = enemyPlayerAction.getEventSequenceId() - lastEventSequenceId;
    if (sequenceDiff > 1 && enemyPlayerAction.getRoute().dst(currentPosition)
        > TOO_MUCH_DISTANCE_TRAVELLED) {
      LOG.warn("Too much distance travelled in one hop");
      skipEventsAndTeleport(enemyPlayerAction);
    } else {
      actions.add(enemyPlayerAction);
    }
    lastEventSequenceId = enemyPlayerAction.getEventSequenceId();
  }

  private void skipEventsAndTeleport(EnemyPlayerAction enemyPlayerAction) {
    onTeleport.accept(enemyPlayerAction);
    actions.forEach(
        action -> Optional.ofNullable(action.getOnComplete()).ifPresent(Runnable::run));
    actions.clear();
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
