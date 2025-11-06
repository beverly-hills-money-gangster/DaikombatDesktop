package com.beverly.hills.money.gang.rect;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
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

  // TODO override so I don't have to  x-width/2
  public RectanglePlus(final float x, final float y, final float width, final float height) {
    super(x, y, width, height);
  }

  public RectanglePlus(final float x, final float y, final float width, final float height,
      final int connectedEntityId, final RectanglePlusFilter filter) {
    this(x, y, width, height);

    this.connectedEntityId = connectedEntityId;
    this.filter = filter;
  }

  public boolean isTooClose(Vector2 targetPosition) {
    return isTooClose(getOldPosition().cpy()
        .add(width / 2, height / 2), targetPosition);
  }

  public Vector3 center(Vector2 targetPosition) {
    getNewPosition().set(targetPosition.x - getWidth() / 2, targetPosition.y - getHeight() / 2);
    setX(getNewPosition().x);
    setY(getNewPosition().y);
    getOldPosition().set(x, y);
    return new Vector3(x + getWidth() / 2, 0, y + getHeight() / 2);
  }

  public Vector3 moveToDirection(Vector2 targetPosition, float delta, float speed) {
    Vector2 rectDirection = new Vector2();
    Vector2 targetRect = new Vector2(targetPosition.x - getWidth() / 2,
        targetPosition.y - getHeight() / 2);
    rectDirection.x = targetRect.x - x;
    rectDirection.y = targetRect.y - y;
    rectDirection.nor().scl(speed * delta);
    getNewPosition().add(rectDirection.x, rectDirection.y);

    setX(getNewPosition().x);
    setY(getNewPosition().y);
    getOldPosition().set(x, y);
    return new Vector3(x + getWidth() / 2, 0, y + getHeight() / 2);
  }

  public Vector2 getCenter() {
    return new Vector2(x + getWidth() / 2, y + getHeight() / 2);
  }

  private boolean isTooClose(Vector2 vector1, Vector2 vector2) {
    return Vector2.dst(vector1.x, vector1.y, vector2.x,
        vector2.y) <= 0.05f;
  }

}
