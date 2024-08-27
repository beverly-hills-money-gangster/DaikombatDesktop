package com.beverly.hills.money.gang.entities.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

@RequiredArgsConstructor
public class UILeaderBoard {

  private final int myPlayerId;

  @Getter
  private int myPlace;
  private int myKills;
  private int myDeaths;

  private final Runnable onTakenTheLead;
  private final Runnable onLostTheLead;
  private final Consumer<Integer> onFragsLeft;
  private int lastFragsLeft;

  private static final PlayerComparator COMPARATOR = new PlayerComparator();


  private final int fragsToWin;

  private final List<LeaderBoardPlayer> leaderBoardItems = new ArrayList<>();

  public UILeaderBoard(
      final int myPlayerId,
      final List<LeaderBoardPlayer> leaderBoardItems,
      final int fragsToWin,
      final Runnable onTakenTheLead,
      final Runnable onLostTheLead,
      final Consumer<Integer> onFragsLeft) {
    this.leaderBoardItems.addAll(leaderBoardItems);
    this.leaderBoardItems.sort(COMPARATOR);
    this.myPlayerId = myPlayerId;
    this.onTakenTheLead = onTakenTheLead;
    this.onLostTheLead = onLostTheLead;
    this.onFragsLeft = onFragsLeft;
    this.fragsToWin = fragsToWin;
    setMyStats();
    getTopPlayer().ifPresent(topPlayer -> lastFragsLeft = fragsToWin - topPlayer.kills);
  }

  public void set(final List<LeaderBoardPlayer> leaderBoardItems) {
    this.leaderBoardItems.clear();
    this.leaderBoardItems.addAll(leaderBoardItems);
    this.leaderBoardItems.sort(COMPARATOR);
    setMyStats();
  }

  public String getFirstPlaceStats() {
    if (leaderBoardItems.isEmpty()) {
      return "";
    }
    var leader = leaderBoardItems.get(0);
    return leader.name + " | " + getKillsMessage(leader.kills) + " | " + getDeathsMessage(
        leader.getDeaths());
  }

  public int getMyKills() {
    return myKills;
  }

  public String getMyKillsMessage() {
    return getKillsMessage(myKills);
  }

  public String getMyDeathsMessage() {
    return getDeathsMessage(myDeaths);
  }

  public String getMyStatsMessage(int fragsToWin) {
    StringBuilder statsMessage = new StringBuilder();
    if (getMyPlace() > 0) {
      statsMessage.append(getMyKillsMessage())
          .append(" OUT OF ").append(fragsToWin)
          .append(" | ").append(getMyDeathsMessage())
          .append(" | ").append(getMyPlace()).append(" PLACE");
    } else {
      statsMessage.append(getMyKillsMessage()).append(" | ").append(getMyDeathsMessage());
    }
    return statsMessage.toString();
  }


  private String getKillsMessage(int kills) {
    if (kills == 0) {
      return "0 KILL";
    }
    if (kills == 1) {
      return "1 KILL";
    }
    return kills + " KILLS";
  }

  private String getDeathsMessage(int deaths) {
    if (deaths == 0) {
      return "0 DEATH";
    }
    if (deaths == 1) {
      return "1 DEATH";
    }
    return deaths + " DEATHS";
  }

  private String getPingMessage(int pingMls) {
    return (pingMls > 0 ? String.valueOf(pingMls) : "-") + " MS PING";
  }

  private void setMyStats() {
    for (int i = 0; i < leaderBoardItems.size(); i++) {
      var item = leaderBoardItems.get(i);
      if (item.id != myPlayerId) {
        continue;
      }
      myKills = item.kills;
      myDeaths = item.deaths;
      myPlace = i + 1;
      return;
    }
  }

  public void removePlayer(int playerId) {
    leaderBoardItems.removeIf(leaderBoardPlayer -> leaderBoardPlayer.getId() == playerId);
    setMyStats();
  }

  public void setPing(int playerId, int pingMls) {
    if (pingMls <= 0) {
      return;
    }
    leaderBoardItems.stream()
        .filter(leaderBoardPlayer -> leaderBoardPlayer.getId() == playerId)
        .findFirst()
        .ifPresent(leaderBoardPlayer -> leaderBoardPlayer.setPing(pingMls));
  }

  public void registerKill(int killerPlayerId, int victimPlayerId) {
    int myOldPlace = myPlace;
    leaderBoardItems.stream()
        .filter(leaderBoardPlayer -> leaderBoardPlayer.getId() == killerPlayerId)
        .findFirst()
        .ifPresent(leaderBoardPlayer -> leaderBoardPlayer.setKills(leaderBoardPlayer.kills + 1));

    leaderBoardItems.stream()
        .filter(leaderBoardPlayer -> leaderBoardPlayer.getId() == victimPlayerId)
        .findFirst()
        .ifPresent(leaderBoardPlayer -> leaderBoardPlayer.setDeaths(leaderBoardPlayer.deaths + 1));
    getTopPlayer().ifPresent(
        topPlayer -> {
          var fragsLeft = fragsToWin - topPlayer.kills;
          if (fragsLeft < lastFragsLeft) {
            onFragsLeft.accept(fragsLeft);
          }
          lastFragsLeft = fragsLeft;
        });

    if (myPlace == 1 && myKills > 0 && myOldPlace > myPlace) {
      onTakenTheLead.run();
    } else if (myOldPlace == 1 && myKills > 0 && myPlace != 1) {
      onLostTheLead.run();
    }
  }

  private Optional<LeaderBoardPlayer> getTopPlayer() {
    return leaderBoardItems.stream()
        .max(Comparator.comparingInt(LeaderBoardPlayer::getKills));
  }

  public void addNewPlayer(LeaderBoardPlayer newLeaderBoardPlayer) {
    if (leaderBoardItems.stream()
        .anyMatch(leaderBoardPlayer -> leaderBoardPlayer.id == newLeaderBoardPlayer.id)) {
      return;
    }
    leaderBoardItems.add(newLeaderBoardPlayer);
    leaderBoardItems.sort(COMPARATOR);
    setMyStats();
  }

  @Override
  public String toString() {
    return constructToString(leaderBoardItems);
  }

  private String constructToString(List<LeaderBoardPlayer> leaderBoard) {
    StringBuilder sb = new StringBuilder();
    int place = 1;
    for (LeaderBoardPlayer item : leaderBoard) {
      sb.append("# ")
          .append(StringUtils.rightPad(String.valueOf(place), 5, " "))
          .append(StringUtils.rightPad(getKillsMessage(item.getKills()), 10, " "))
          .append(StringUtils.rightPad(getDeathsMessage(item.getDeaths()), 10, " "))
          .append(StringUtils.rightPad(getPingMessage(item.ping), 13, " "))
          .append(item.getName());
      if (item.id == myPlayerId) {
        sb.append("  < YOU");
      }
      sb.append("\n");
      place++;
    }
    return sb.toString().toUpperCase(Locale.ENGLISH);
  }


  @Builder
  @Getter
  @Setter
  @ToString
  public static class LeaderBoardPlayer {

    private String name;
    private int id;
    private int kills;
    private int deaths;
    private int ping;
  }

  private static class PlayerComparator implements Comparator<LeaderBoardPlayer> {

    @Override
    public int compare(LeaderBoardPlayer player1, LeaderBoardPlayer player2) {
      int killsCompare = -Integer.compare(player1.getKills(), player2.getKills());
      if (killsCompare == 0) {
        int deathCompare = Integer.compare(player1.getDeaths(), player2.getDeaths());
        if (deathCompare == 0) {
          return player1.getName().compareTo(player2.name);
        } else {
          return deathCompare;
        }
      } else {
        return killsCompare;
      }
    }
  }

}
