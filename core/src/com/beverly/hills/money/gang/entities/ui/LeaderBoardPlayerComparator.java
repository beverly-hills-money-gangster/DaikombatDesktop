package com.beverly.hills.money.gang.entities.ui;

import java.util.Comparator;

public class LeaderBoardPlayerComparator implements Comparator<LeaderBoardPlayer> {

  @Override
  public int compare(LeaderBoardPlayer player1, LeaderBoardPlayer player2) {
    int killsCompare = -Integer.compare(player1.getKills(), player2.getKills());
    if (killsCompare == 0) {
      int deathCompare = Integer.compare(player1.getDeaths(), player2.getDeaths());
      if (deathCompare == 0) {
        return player1.getName().compareTo(player2.getName());
      } else {
        return deathCompare;
      }
    } else {
      return killsCompare;
    }
  }
}