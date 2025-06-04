package com.beverly.hills.money.gang.screens.data;

import com.beverly.hills.money.gang.screens.ui.selection.GamePlayerClass;
import com.beverly.hills.money.gang.screens.ui.selection.SkinUISelection;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder(toBuilder = true)
@ToString
public class JoinGameData {

  private final Integer playerIdToRecover;
  private final String playerName;
  private final SkinUISelection skinUISelection;
  private final GamePlayerClass gamePlayerClass;
}
