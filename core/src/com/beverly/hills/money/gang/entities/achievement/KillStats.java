package com.beverly.hills.money.gang.entities.achievement;

import java.util.ArrayList;
import java.util.List;

public class KillStats {

  private final List<Long> kills = new ArrayList<>();

  public void registerKill() {
    kills.add(System.currentTimeMillis());
  }

  public int getTotalKills() {
    return kills.size();
  }

  public int countKillsInTimeWindow(int timeWindowMls) {
    int count = 0;
    long currentTimeWindowMls = System.currentTimeMillis() - timeWindowMls;
    for (Long killTime : kills) {
      if (killTime > currentTimeWindowMls) {
        count++;
      }
    }
    return count;
  }

}
