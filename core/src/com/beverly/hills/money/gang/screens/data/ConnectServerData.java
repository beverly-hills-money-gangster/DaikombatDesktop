package com.beverly.hills.money.gang.screens.data;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder(toBuilder = true)
@ToString
public class ConnectServerData {

  private final String serverHost;
  private final int serverPort;
}
