package com.beverly.hills.money.gang.screens.data;

import com.badlogic.gdx.math.Vector2;
import com.beverly.hills.money.gang.proto.ServerResponse;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@Builder
@ToString
public class PlayerConnectionContextData {

  private final JoinGameData joinGameData;
  private final Vector2 spawn;
  private final Vector2 direction;
  @NonNull
  private final Integer playerId;
  private final List<ServerResponse.LeaderBoardItem> leaderBoardItemList;
  private final int playersOnline;
  @NonNull
  private final Integer fragsToWin;
  @NonNull
  private final Integer movesUpdateFreqMls;
  @NonNull
  private final Integer speed;

}
