package com.beverly.hills.money.gang.entities.ui;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class UILeaderBoard {

    private final int myPlayerId;

    @Getter
    private int myPlace;

    private int myKills;

    private final Runnable onTakenTheLead;

    private final Runnable onLostTheLead;

    private static final PlayerComparator COMPARATOR = new PlayerComparator();
    private String cachedToString = "";

    private boolean needRefresh;

    private final List<LeaderBoardPlayer> leaderBoardItems = new ArrayList<>();

    public UILeaderBoard(final int myPlayerId,
                         final List<LeaderBoardPlayer> leaderBoardItems,
                         final Runnable onTakenTheLead,
                         final Runnable onLostTheLead) {
        this.leaderBoardItems.addAll(leaderBoardItems);
        this.leaderBoardItems.sort(COMPARATOR);
        this.myPlayerId = myPlayerId;
        this.onTakenTheLead = onTakenTheLead;
        this.onLostTheLead = onLostTheLead;
        setMyStats();
        needRefresh = true;
    }

    public int getMyKills() {
        return myKills;
    }

    public String getMyKillsMessage() {
        return getKillsMessage(myKills);
    }

    // TODO cover with tests
    public String getMyStatsMessage() {
        if (getMyPlace() > 0) {
            return this.getMyKillsMessage() + " | " + getMyPlace() + " PLACE";
        } else {
            return this.getMyKillsMessage();
        }
    }

    // TODO render it somehow
    public String getTopOtherPlayersString() {
        return constructToString(leaderBoardItems.stream()
                .filter(leaderBoardPlayer -> leaderBoardPlayer.getId() != myPlayerId)
                .limit(3).collect(Collectors.toList()));
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


    private void setMyStats() {
        int oldPlace = myPlace;
        for (int i = 0; i < leaderBoardItems.size(); i++) {
            var item = leaderBoardItems.get(i);
            if (item.id == myPlayerId) {
                myKills = item.kills;
                myPlace = i + 1;
                if (myPlace == 1 && myKills > 0 && oldPlace > myPlace) {
                    onTakenTheLead.run();
                } else if (oldPlace == 1 && myKills > 0 && myPlace != 1) {
                    onLostTheLead.run();
                }
                return;
            }
        }
    }

    public void removePlayer(int playerId) {
        leaderBoardItems.removeIf(leaderBoardPlayer -> leaderBoardPlayer.getId() == playerId);
        setMyStats();
        needRefresh = true;
    }

    public void registerKill(int killerPlayerId, int victimPlayerId) {
        removePlayer(victimPlayerId);
        leaderBoardItems.stream().filter(leaderBoardPlayer -> leaderBoardPlayer.getId() == killerPlayerId)
                .findFirst().ifPresent(leaderBoardPlayer -> {
                    removePlayer(leaderBoardPlayer.id);
                    addNewPlayer(LeaderBoardPlayer.builder()
                            .id(leaderBoardPlayer.id)
                            .kills(leaderBoardPlayer.kills + 1)
                            .name(leaderBoardPlayer.name).build());
                });
        if (victimPlayerId == myPlayerId) {
            myKills = 0;
            myPlace = 0;
        } else {
            setMyStats();
        }
        needRefresh = true;
    }

    public void addNewPlayer(LeaderBoardPlayer newLeaderBoardPlayer) {
        if (leaderBoardItems.stream().anyMatch(leaderBoardPlayer -> leaderBoardPlayer.id == newLeaderBoardPlayer.id)) {
            return;
        }
        leaderBoardItems.add(newLeaderBoardPlayer);
        leaderBoardItems.sort(COMPARATOR);
        setMyStats();
        needRefresh = true;
    }

    @Override
    public String toString() {
        if (!needRefresh) {
            return cachedToString;
        }
        cachedToString = constructToString(leaderBoardItems);
        needRefresh = false;
        return cachedToString;
    }

    private String constructToString(List<LeaderBoardPlayer> leaderBoard) {
        StringBuilder sb = new StringBuilder();
        int place = 1;
        for (LeaderBoardPlayer leaderBoardItem : leaderBoard) {
            sb.append("# ")
                    .append(place).append("    ")
                    .append(getKillsMessage(leaderBoardItem.getKills())).append("    ")
                    .append(leaderBoardItem.getName());
            if (leaderBoardItem.id == myPlayerId) {
                sb.append("  < YOU");
            }
            sb.append("\n");
            place++;
        }
        return sb.toString().toUpperCase(Locale.ENGLISH);
    }


    @Builder
    @Getter
    @ToString
    public static class LeaderBoardPlayer {
        private final String name;
        private final int id;
        private final int kills;

    }

    private static class PlayerComparator implements Comparator<LeaderBoardPlayer> {
        @Override
        public int compare(LeaderBoardPlayer player1, LeaderBoardPlayer player2) {
            return -Integer.compare(player1.getKills(), player2.getKills());
        }
    }

}
