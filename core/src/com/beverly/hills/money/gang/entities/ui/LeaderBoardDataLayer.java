package com.beverly.hills.money.gang.entities.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@RequiredArgsConstructor
public class LeaderBoardDataLayer {

  protected static final LeaderBoardPlayerComparator COMPARATOR = new LeaderBoardPlayerComparator();

  protected final int myPlayerId;

  @Getter
  protected int myPlace;
  protected int myKills;
  protected int myDeaths;

  protected final List<LeaderBoardPlayer> leaderBoardItems = new ArrayList<>();

  public LeaderBoardDataLayer(
      final int myPlayerId,
      final List<LeaderBoardPlayer> leaderBoardItems) {
    this.leaderBoardItems.addAll(leaderBoardItems);
    this.leaderBoardItems.sort(COMPARATOR);
    this.myPlayerId = myPlayerId;
    setMyStats();
  }

  public void set(final List<LeaderBoardPlayer> leaderBoardItems) {
    this.leaderBoardItems.clear();
    this.leaderBoardItems.addAll(leaderBoardItems);
    this.leaderBoardItems.sort(COMPARATOR);
    setMyStats();
  }

  public String getFirstPlaceString() {
    if (leaderBoardItems.isEmpty()) {
      return "";
    }
    var leader = leaderBoardItems.get(0);
    return leader.getName() + " | " + getFirstPlaceStats();
  }

  private LeaderBoardPlayer getLeader() {
    return leaderBoardItems.get(0);
  }

  public String getFirstPlaceStats() {
    var leader = getLeader();
    return getKillsMessage(leader.getKills()) + " | " + getDeathsMessage(
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

  protected void setMyStats() {
    for (int i = 0; i < leaderBoardItems.size(); i++) {
      var item = leaderBoardItems.get(i);
      if (item.getId() != myPlayerId) {
        continue;
      }
      myKills = item.getKills();
      myDeaths = item.getDeaths();
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

  public Optional<LeaderBoardPlayer> getTopPlayer() {
    return leaderBoardItems.stream()
        .max(Comparator.comparingInt(LeaderBoardPlayer::getKills));
  }

  public void addNewPlayer(LeaderBoardPlayer newLeaderBoardPlayer) {
    if (leaderBoardItems.stream()
        .anyMatch(leaderBoardPlayer -> leaderBoardPlayer.getId() == newLeaderBoardPlayer.getId())) {
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
          .append(StringUtils.rightPad(getPingMessage(item.getPing()), 13, " "))
          .append(item.getName());
      if (item.getId() == myPlayerId) {
        sb.append("  < YOU");
      }
      sb.append("\n");
      place++;
    }
    return sb.toString().toUpperCase(Locale.ENGLISH);
  }

  public int size() {
    return leaderBoardItems.size();
  }


}
