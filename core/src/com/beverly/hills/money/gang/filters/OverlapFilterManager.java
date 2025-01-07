package com.beverly.hills.money.gang.filters;

import com.beverly.hills.money.gang.rect.filters.RectanglePlusFilter;

public class OverlapFilterManager {

  public final int[][] overlapFilters = new int[8][8];

  public OverlapFilterManager() {
    setupFilters();
  }

  public boolean doesFiltersOverlap(final RectanglePlusFilter thisFilter,
      final RectanglePlusFilter otherFilter) {

//		Having issues with overlaps? Did you register the filter first?

    for (int x = 0; x < overlapFilters.length; x++) {
      if (x == thisFilter.getValue()) {
        for (int y = 1; y < overlapFilters[x].length; y++) { // y != 0!
          if (overlapFilters[x][y] == otherFilter.getValue()) {
            return true;
          }
        }
      }
    }
    return false;
  }

  private void setupFilters() {
//		Setup filters
    overlapFilters[0][0] = RectanglePlusFilter.NONE.getValue(); // Don't touch me.

    overlapFilters[1][0] = RectanglePlusFilter.WALL.getValue();
    overlapFilters[2][0] = RectanglePlusFilter.DOOR.getValue();
    overlapFilters[3][0] = RectanglePlusFilter.PLAYER.getValue();
    overlapFilters[4][0] = RectanglePlusFilter.ENEMY.getValue();
    overlapFilters[5][0] = RectanglePlusFilter.PROJECTILE.getValue();
    overlapFilters[6][0] = RectanglePlusFilter.ITEM.getValue();

//		Door
    overlapFilters[2][1] = RectanglePlusFilter.PLAYER.getValue();
    overlapFilters[2][2] = RectanglePlusFilter.ENEMY.getValue();
    overlapFilters[2][3] = RectanglePlusFilter.PROJECTILE.getValue();

//		Player
    overlapFilters[3][1] = RectanglePlusFilter.WALL.getValue();
    overlapFilters[3][2] = RectanglePlusFilter.DOOR.getValue();
//		overlapFilters[3][3] = RectanglePlusFilter.PLAYER.getValue();
    overlapFilters[3][4] = RectanglePlusFilter.ENEMY.getValue();
    overlapFilters[3][5] = RectanglePlusFilter.PROJECTILE.getValue();
    overlapFilters[3][6] = RectanglePlusFilter.ITEM.getValue();

//		Enemy
    overlapFilters[4][1] = RectanglePlusFilter.WALL.getValue();
    overlapFilters[4][2] = RectanglePlusFilter.DOOR.getValue();
    overlapFilters[4][3] = RectanglePlusFilter.PLAYER.getValue();
    overlapFilters[4][4] = RectanglePlusFilter.ENEMY.getValue();

//		Projectile
    overlapFilters[5][1] = RectanglePlusFilter.WALL.getValue();
    overlapFilters[5][2] = RectanglePlusFilter.DOOR.getValue();
    overlapFilters[5][3] = RectanglePlusFilter.PLAYER.getValue();
//		overlapFilters[5][4] = RectanglePlusFilter.ENEMY.getValue();

//		Items
    overlapFilters[6][1] = RectanglePlusFilter.PLAYER.getValue();
  }
}
