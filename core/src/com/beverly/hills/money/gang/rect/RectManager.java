package com.beverly.hills.money.gang.rect;

import com.badlogic.gdx.utils.Array;
import com.beverly.hills.money.gang.DaiKombatGame;
import com.beverly.hills.money.gang.entities.Entity;
import com.beverly.hills.money.gang.rect.filters.RectanglePlusFilter;
import lombok.Getter;

public class RectManager {

  @Getter
  private final Array<RectanglePlus> rects = new Array<>();

  private final DaiKombatGame game;

  public RectManager(final DaiKombatGame game) {
    this.game = game;
  }

  public void addRect(final RectanglePlus rect) {
    rects.add(rect);
  }

  public boolean checkCollision(final RectanglePlus rect) {
    for (final RectanglePlus otherRect : rects) {
      if (game.getOverlapFilterMan().doesFiltersOverlap(rect.getFilter(), otherRect.getFilter())
          && otherRect != rect
          && RectanglePlusFilter.ENEMY != otherRect.getFilter()
          && RectanglePlusFilter.ITEM != otherRect.getFilter()
          && RectanglePlusFilter.PROJECTILE != otherRect.getFilter()
          && RectanglePlusFilter.DOOR != otherRect.getFilter()
          && rect.overlaps(otherRect)) {
        return true;
      }
    }
    return false;
  }

  public void onCollisionWithPlayer(final RectanglePlus playerRect) {
    for (final RectanglePlus otherRect : rects) {
      if (game.getOverlapFilterMan()
          .doesFiltersOverlap(playerRect.getFilter(), otherRect.getFilter())
          && otherRect != playerRect
          && (RectanglePlusFilter.ITEM == otherRect.getFilter()
          || RectanglePlusFilter.DOOR == otherRect.getFilter())
          && playerRect.overlaps(otherRect)) {
        Entity item = game.getEntMan()
            .getEntityFromId(otherRect.getConnectedEntityId());
        item.onCollisionWithPlayer();
      }
    }
  }

  public void removeRect(final RectanglePlus rect) {
    for (int i = 0; i < rects.size; i++) {
      if (rect == rects.get(i)) {
        rects.removeIndex(i);
        break;
      }
    }
  }
}
