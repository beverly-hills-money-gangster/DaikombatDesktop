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
public class GameBootstrapData {

  @NonNull
  private final CompleteJoinGameData completeJoinGameData;
  @NonNull
  private final Vector2 spawn;
  @NonNull
  private final Vector2 direction;
  @NonNull
  private final Integer playerId;
  @NonNull
  private final List<ServerResponse.LeaderBoardItem> leaderBoardItemList;
  @NonNull
  private final Integer playersOnline;
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
  private final Weapon lastWeapon;

}
