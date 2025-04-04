package com.beverly.hills.money.gang.screens.data;

import com.badlogic.gdx.math.Vector2;
import com.beverly.hills.money.gang.proto.ServerResponse;
import com.beverly.hills.money.gang.screens.ui.weapon.Weapon;
import com.beverly.hills.money.gang.screens.ui.weapon.WeaponStats;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@Builder(toBuilder = true)
@ToString
public class PlayerConnectionContextData {

  private final ConnectGameData connectGameData;
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
  private final Float speed;
  @NonNull
  private final Map<Weapon, WeaponStats> weaponStats;
  @NonNull
  private final Integer maxVisibility;
  @NonNull
  private final Integer matchId;

  @NonNull
  private final Integer audioSamplingRate;

  private final boolean recordAudio;

}
