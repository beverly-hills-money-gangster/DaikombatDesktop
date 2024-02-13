package com.beverly.hills.money.gang.rect;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.beverly.hills.money.gang.rect.filters.RectanglePlusFilter;
import lombok.Getter;
import lombok.Setter;

public class RectanglePlus extends Rectangle {

    @Getter
    private final Vector2 oldPosition = new Vector2();
    @Getter
    private final Vector2 newPosition = new Vector2();

    @Setter
    @Getter
    private boolean overlapX;
    @Setter
    @Getter
    private boolean overlapY;
    @Getter
    private RectanglePlusFilter filter = RectanglePlusFilter.NONE;

    @Getter
    private int connectedEntityId;

    public RectanglePlus(final float x, final float y, final float width, final float height) {
        super(x, y, width, height);
    }

    public RectanglePlus(final float x, final float y, final float width, final float height,
                         final int connectedEntityId, final RectanglePlusFilter filter) {
        this(x, y, width, height);

        this.connectedEntityId = connectedEntityId;
        this.filter = filter;
    }


}
