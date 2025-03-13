package com.beverly.hills.money.gang.entities.ui;

import com.beverly.hills.money.gang.proto.ServerResponse;
import com.beverly.hills.money.gang.screens.ui.selection.GamePlayerClass;
import com.beverly.hills.money.gang.screens.ui.selection.SkinUISelection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@Setter
@ToString
// TODO put in another class
public class LeaderBoardPlayer {

  private String name;
  private int id;
  private int kills;
  private int deaths;
  private int ping;
  @NonNull
  private GamePlayerClass playerClass;
  @NonNull
  private SkinUISelection skinUISelection;

  public static List<LeaderBoardPlayer> createFromGameOver(
      ServerResponse.GameOver gameOver) {
    return gameOver.getLeaderBoard().getItemsList().stream()
        .map(leaderBoardItem -> LeaderBoardPlayer.builder()
            .name(leaderBoardItem.getPlayerName())
            .id(leaderBoardItem.getPlayerId())
            .ping(leaderBoardItem.getPingMls())
            .deaths(leaderBoardItem.getDeaths())
            .kills(leaderBoardItem.getKills())
            .skinUISelection(SkinUISelection.getSkinColor(leaderBoardItem.getSkinColor()))
            .playerClass(GamePlayerClass.createPlayerClass(leaderBoardItem.getPlayerClass()))
            .build())
        .collect(Collectors.toList());
  }

}
