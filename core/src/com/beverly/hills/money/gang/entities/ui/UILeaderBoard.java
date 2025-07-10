package com.beverly.hills.money.gang.entities.ui;

import java.util.List;
import java.util.function.Consumer;

public class UILeaderBoard extends LeaderBoardDataLayer {

  private final Runnable onTakenTheLead;
  private final Runnable onLostTheLead;
  private final Consumer<Integer> onFragsLeft;
  private int lastFragsLeft;

  private final int fragsToWin;


  public UILeaderBoard(
      final int myPlayerId,
      final List<LeaderBoardPlayer> leaderBoardItems,
      final int fragsToWin,
      final Runnable onTakenTheLead,
      final Runnable onLostTheLead,
      final Consumer<Integer> onFragsLeft) {
    super(myPlayerId, leaderBoardItems);
    this.onTakenTheLead = onTakenTheLead;
    this.onLostTheLead = onLostTheLead;
    this.onFragsLeft = onFragsLeft;
    this.fragsToWin = fragsToWin;
    setMyStats();
    getTopPlayer().ifPresent(topPlayer -> lastFragsLeft = fragsToWin - topPlayer.getKills());
  }

  public void registerKill(int killerPlayerId, int victimPlayerId) {
    int myOldPlace = myPlace;
    if (killerPlayerId != victimPlayerId) {
      leaderBoardItems.stream()
          .filter(leaderBoardPlayer -> leaderBoardPlayer.getId() == killerPlayerId)
          .findFirst()
          .ifPresent(
              leaderBoardPlayer -> leaderBoardPlayer.setKills(leaderBoardPlayer.getKills() + 1));
    }
    leaderBoardItems.stream()
        .filter(leaderBoardPlayer -> leaderBoardPlayer.getId() == victimPlayerId)
        .findFirst()
        .ifPresent(
            leaderBoardPlayer -> leaderBoardPlayer.setDeaths(leaderBoardPlayer.getDeaths() + 1));
    getTopPlayer().ifPresent(
        topPlayer -> {
          var fragsLeft = fragsToWin - topPlayer.getKills();
          if (fragsLeft < lastFragsLeft) {
            onFragsLeft.accept(fragsLeft);
          }
          lastFragsLeft = fragsLeft;
        });
    this.leaderBoardItems.sort(COMPARATOR);
    setMyStats();
    if (myPlace == 1 && myKills > 0 && myOldPlace > myPlace) {
      onTakenTheLead.run();
    } else if (myOldPlace == 1 && myKills > 0 && myPlace != 1) {
      onLostTheLead.run();
    }
  }

}
