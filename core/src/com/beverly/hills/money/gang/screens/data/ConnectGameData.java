package com.beverly.hills.money.gang.screens.data;

import com.beverly.hills.money.gang.screens.ui.selection.SkinUISelection;
import io.micrometer.common.lang.Nullable;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Builder
@ToString
@Setter
public class ConnectGameData {

  private String serverHost;
  private int serverPort;
  private String playerName;
  @Nullable
  private Integer playerIdToRecover;
  private SkinUISelection skinUISelection;
}
