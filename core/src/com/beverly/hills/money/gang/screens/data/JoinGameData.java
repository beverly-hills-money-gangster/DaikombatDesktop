package com.beverly.hills.money.gang.screens.data;

import com.beverly.hills.money.gang.screens.ui.selection.SkinUISelection;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class JoinGameData {

  private final String serverHost;
  private final int serverPort;
  private final String serverPassword;
  private final String playerName;
  private final SkinUISelection skinUISelection;
}
