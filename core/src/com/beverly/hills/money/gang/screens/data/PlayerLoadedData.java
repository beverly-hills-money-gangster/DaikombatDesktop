package com.beverly.hills.money.gang.screens.data;

import com.badlogic.gdx.math.Vector2;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class PlayerLoadedData {
    private final String playerName;
    private final Vector2 spawn;
    private final Vector2 direction;
    private final int playerId;

}
