package com.beverly.hills.money.gang.screens.data;

import com.badlogic.gdx.math.Vector2;
import com.beverly.hills.money.gang.proto.ServerResponse;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@Builder
@ToString
public class PlayerContextData {
    private final PlayerServerInfoContextData playerServerInfoContextData;
    private final Vector2 spawn;
    private final Vector2 direction;
    private final int playerId;
    private final List<ServerResponse.LeaderBoardItem> leaderBoardItemList;
    private final int playersOnline;

}
