package com.beverly.hills.money.gang.screens.data;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Builder(toBuilder = true)
@Getter
public class CompleteJoinGameData {

  @NonNull
  private final ConnectServerData connectServerData;
  @NonNull
  private final JoinGameData joinGameData;
  @NonNull
  private final Integer gameRoomId;
  @NonNull
  private final String mapName;
  @NonNull
  private final String mapHash;

}
