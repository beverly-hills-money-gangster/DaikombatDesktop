package com.beverly.hills.money.gang.registry;

import com.badlogic.gdx.math.Vector2;
import com.beverly.hills.money.gang.entities.enemies.EnemyPlayer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class EnemiesRegistry {

  private final Map<Integer, EnemyPlayer> enemyPlayers = new HashMap<>();

  public void addEnemy(int playerId, EnemyPlayer enemyPlayer) {
    enemyPlayers.put(playerId, enemyPlayer);
  }

  public boolean exists(int playerId) {
    return enemyPlayers.containsKey(playerId);
  }

  public Optional<EnemyPlayer> getEnemy(int playerId) {
    return Optional.ofNullable(enemyPlayers.get(playerId));
  }

  public List<EnemyPlayer> getEnemiesInRange(final Vector2 position, final float range) {
    return enemyPlayers.values().stream().filter(
        enemyPlayer ->
            new Vector2(enemyPlayer.getPosition().x, enemyPlayer.getPosition().z).dst(position)
                <= range).collect(Collectors.toList());
  }

  public Optional<EnemyPlayer> removeEnemy(int playerId) {
    return Optional.ofNullable(enemyPlayers.remove(playerId));
  }
}
