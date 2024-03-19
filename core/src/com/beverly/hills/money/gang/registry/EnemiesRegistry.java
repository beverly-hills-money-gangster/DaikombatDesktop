package com.beverly.hills.money.gang.registry;

import com.beverly.hills.money.gang.entities.enemies.EnemyPlayer;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

  public Optional<EnemyPlayer> removeEnemy(int playerId) {
    return Optional.ofNullable(enemyPlayers.remove(playerId));
  }
}
