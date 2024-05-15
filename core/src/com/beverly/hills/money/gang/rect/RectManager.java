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
          && rect.overlaps(otherRect)) {
        if (RectanglePlusFilter.ITEM == otherRect.getFilter()) {
          Entity item = game.getEntMan()
              .getEntityFromId(otherRect.getConnectedEntityId());
          item.onCollision();
          continue;
        }
        return true;
      }
    }
    return false;
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
