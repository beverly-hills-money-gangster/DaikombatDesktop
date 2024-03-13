package com.beverly.hills.money.gang.screens.data;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class PlayerServerInfoContextData {
    private final String serverHost;
    private final int serverPort;
    private final String serverPassword;
    private final String playerName;
}
