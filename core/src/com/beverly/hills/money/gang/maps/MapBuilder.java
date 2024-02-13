package com.beverly.hills.money.gang.maps;


import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.beverly.hills.money.gang.assets.managers.registry.TexturesRegistry;
import com.beverly.hills.money.gang.cell.Cell3D;
import com.beverly.hills.money.gang.rect.RectanglePlus;
import com.beverly.hills.money.gang.rect.filters.RectanglePlusFilter;
import com.beverly.hills.money.gang.Constants;
import com.beverly.hills.money.gang.DaiKombatGame;

public class MapBuilder {

    private static final int TILE_DEV_FLOOR_ID = 17 + 1;
    private static final int TILE_FLOOR_01_ID = 3 + 1;
    private static final int TILE_FLOOR_02_ID = 5 + 1;
    private static final int TILE_DEV_CEILING_ID = 1 + 1;
    private static final float TILE_SIZE = 16f;
    // TODO probably not needed
    public final Vector2 mapLoadSpawnPosition = new Vector2();
    private final DaiKombatGame game;

    public MapBuilder(final DaiKombatGame game) {
        this.game = game;
    }

    public void buildMap(final TiledMap map) {
        final MapProperties props = map.getProperties();
        final int currentMapWidth = props.get("width", Integer.class);
        final int currentMapHeight = props.get("height", Integer.class);
        final float halfCurrentMapWidth = currentMapWidth / 2f;
        final float halfCurrentMapHeight = currentMapHeight / 2f;

        final MapLayers mapLayers = map.getLayers();
        final TiledMapTileLayer floorLayer = (TiledMapTileLayer) mapLayers.get("floor");
        final TiledMapTileLayer ceilingLayer = (TiledMapTileLayer) mapLayers.get("ceiling");
        final MapObjects rects = mapLayers.get("rects").getObjects();
        final MapObjects teleports = mapLayers.get("teleports").getObjects();

        final Array<Cell3D> cell3DsForWorld = new Array<>();


        for (int x = 0; x < currentMapWidth; x++) {
            for (int z = 0; z < currentMapHeight; z++) {
                Cell currentCell = floorLayer.getCell(x, z);
                Cell3D currentCell3D = null;

                if (currentCell != null) {
                    switch (currentCell.getTile().getId()) {
                        case TILE_DEV_FLOOR_ID ->
                                currentCell3D = new Cell3D(new Vector3(x, 0, z), game.getEntMan().getScreen());
                        case TILE_FLOOR_01_ID -> {
                            currentCell3D = new Cell3D(new Vector3(x, 0, z), game.getEntMan().getScreen());
                            // TODO fix this mess


//						This should be setup somewhere else but i dont have time!
                            currentCell3D.texRegNorth = game.getAssMan().getTextureRegion(TexturesRegistry.ATLAS, 32, 0, 16, 16);
                            currentCell3D.texRegSouth = game.getAssMan().getTextureRegion(TexturesRegistry.ATLAS, 32, 0, 16, 16);
                            currentCell3D.texRegWest = game.getAssMan().getTextureRegion(TexturesRegistry.ATLAS, 32, 0, 16, 16);
                            currentCell3D.texRegEast = game.getAssMan().getTextureRegion(TexturesRegistry.ATLAS, 32, 0, 16, 16);
                            currentCell3D.texRegCeiling = game.getAssMan().getTextureRegion(TexturesRegistry.ATLAS, 64, 0, 16, 16);
                            currentCell3D.texRegFloor = game.getAssMan().getTextureRegion(TexturesRegistry.ATLAS, 48, 0, 16, 16);
                        }
                        case TILE_FLOOR_02_ID -> {
                            currentCell3D = new Cell3D(new Vector3(x, 0, z), game.getEntMan().getScreen());
                            currentCell3D.texRegNorth = game.getAssMan().getTextureRegion(TexturesRegistry.ATLAS, 96, 0, 16, 16);
                            currentCell3D.texRegSouth = game.getAssMan().getTextureRegion(TexturesRegistry.ATLAS, 96, 0, 16, 16);
                            currentCell3D.texRegWest = game.getAssMan().getTextureRegion(TexturesRegistry.ATLAS, 96, 0, 16, 16);
                            currentCell3D.texRegEast = game.getAssMan().getTextureRegion(TexturesRegistry.ATLAS, 96, 0, 16, 16);
                            currentCell3D.texRegCeiling = game.getAssMan().getTextureRegion(TexturesRegistry.ATLAS, 48, 0, 16, 16);
                            currentCell3D.texRegFloor = game.getAssMan().getTextureRegion(TexturesRegistry.ATLAS, 80, 0, 16, 16);
                        }
                        default -> currentCell3D = new Cell3D(new Vector3(x, 0, z), game.getEntMan().getScreen());
                    }

                    currentCell3D.hasFloor = true;
                }

                currentCell = ceilingLayer.getCell(x, z);

                if (currentCell != null && currentCell.getTile().getId() == TILE_DEV_CEILING_ID) {
                    if (currentCell3D == null) {
                        currentCell3D = new Cell3D(new Vector3(x, 0, z), game.getEntMan().getScreen());
                    }
                    currentCell3D.hasCeiling = true;
                }

                if (currentCell3D != null) {
                    cell3DsForWorld.add(currentCell3D);
                }
            }
        }

//		Check for walls
//		FIXME: Cant have ceiling alone. But floor is ok.
        for (int i = 0; i < cell3DsForWorld.size; i++) {
            final Cell3D currentCell3D = cell3DsForWorld.get(i);

            for (int j = 0; j < cell3DsForWorld.size; j++) {
                final Cell3D otherCell3D = cell3DsForWorld.get(j);

                if (otherCell3D.position.x == currentCell3D.position.x - 1
                        && otherCell3D.position.z == currentCell3D.position.z) {
                    currentCell3D.hasWallEast = false;
                }

                if (otherCell3D.position.x == currentCell3D.position.x + 1
                        && otherCell3D.position.z == currentCell3D.position.z) {
                    currentCell3D.hasWallWest = false;
                }

                if (otherCell3D.position.x == currentCell3D.position.x
                        && otherCell3D.position.z == currentCell3D.position.z - 1) {
                    currentCell3D.hasWallNorth = false;
                }

                if (otherCell3D.position.x == currentCell3D.position.x
                        && otherCell3D.position.z == currentCell3D.position.z + 1) {
                    currentCell3D.hasWallSouth = false;
                }
            }
        }

//		Build rects - the real walls
        for (final MapObject rectObj : rects) {
            final RectanglePlus rect = new RectanglePlus((float) rectObj.getProperties().get("x") / TILE_SIZE,
                    (float) rectObj.getProperties().get("y") / TILE_SIZE,
                    (float) rectObj.getProperties().get("width") / TILE_SIZE,
                    (float) rectObj.getProperties().get("height") / TILE_SIZE, -1, RectanglePlusFilter.WALL);

            rect.x = rect.x - halfCurrentMapWidth;
            rect.y = rect.y - halfCurrentMapHeight;

            game.getRectMan().addRect(rect);
        }

        for (final Cell3D cell3d : cell3DsForWorld) {
            cell3d.position.add(-halfCurrentMapWidth, 0, -halfCurrentMapHeight).add(Constants.HALF_UNIT, 0, Constants.HALF_UNIT);
            cell3d.buildCell();
            game.getEntMan().addEntity(cell3d);
        }

        cell3DsForWorld.clear();


        for (final MapObject teleportObj : teleports) {
            final boolean spawnOnMapLoad = teleportObj.getProperties().get("MapSpawn", Boolean.class);

            if (spawnOnMapLoad) {
                mapLoadSpawnPosition.set((float) teleportObj.getProperties().get("x") / TILE_SIZE - halfCurrentMapWidth,
                        (float) teleportObj.getProperties().get("y") / TILE_SIZE - halfCurrentMapHeight);
            }
        }

    }
}
