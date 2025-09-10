package com.beverly.hills.money.gang.models;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.beverly.hills.money.gang.configs.Constants;
import com.beverly.hills.money.gang.DaiKombatGame;
import lombok.Getter;


@Getter
public class ModelMaker {

  private final DaiKombatGame game;

  private final ModelBuilder mdlBuilder;

  private Model mdlGrid = new Model();

  private Model mdlWallNorth = new Model();
  private Model mdlWallSouth = new Model();
  private Model mdlWallWest = new Model();
  private Model mdlWallEast = new Model();
  private Model mdlFloor = new Model();
  private Model mdlCeiling = new Model();

  private final Texture atlas;

  private Model mdlDoor = new Model();

  @Getter
  private Model mdlEnemy = new Model();

  public ModelMaker(final DaiKombatGame game, final Texture atlas) {
    this.game = game;
    this.atlas = atlas;

    mdlBuilder = new ModelBuilder();

    buildModels();
  }

  private void buildModels() {
    mdlGrid = mdlBuilder.createLineGrid(10, 10, 1, 1, new Material(),
        Usage.Position | Usage.Normal);

//		ENEMY

    final TextureAttribute taEnemy = new TextureAttribute(TextureAttribute.Diffuse);
    taEnemy.textureDescription.minFilter = TextureFilter.Nearest;
    taEnemy.textureDescription.magFilter = TextureFilter.Nearest;

    final Material matEnemy = new Material();
    matEnemy.set(taEnemy);

    mdlEnemy = mdlBuilder.createRect(Constants.HALF_UNIT, Constants.HALF_UNIT, 0,
        -Constants.HALF_UNIT,
        Constants.HALF_UNIT, 0, -Constants.HALF_UNIT, -Constants.HALF_UNIT, 0, Constants.HALF_UNIT,
        -Constants.HALF_UNIT, 0, 0, 0, -1, matEnemy,
        Usage.Position | Usage.Normal | Usage.TextureCoordinates);

//	NORTH WALL

    final TextureRegion texRegNorth = game.getAssMan().getTextureRegion(atlas,
        32, 16, -16, 16);

    final TextureAttribute taNorth = new TextureAttribute(TextureAttribute.Diffuse, texRegNorth);
    taNorth.textureDescription.minFilter = TextureFilter.Nearest;
    taNorth.textureDescription.magFilter = TextureFilter.Nearest;

    final Material matNorth = new Material();
    matNorth.set(taNorth);

    mdlWallNorth = mdlBuilder.createRect(Constants.HALF_UNIT, Constants.HALF_UNIT, 0,
        -Constants.HALF_UNIT,
        Constants.HALF_UNIT, 0, -Constants.HALF_UNIT, -Constants.HALF_UNIT, 0, Constants.HALF_UNIT,
        -Constants.HALF_UNIT, 0, 0, 0, -1, matNorth,
        Usage.Position | Usage.Normal | Usage.TextureCoordinates);

//	SOUTH WALL
    final TextureRegion texRegSouth = game.getAssMan().getTextureRegion(atlas,
        64, 16, -16, 16); // flip x

    final TextureAttribute taSouth = new TextureAttribute(TextureAttribute.Diffuse, texRegSouth);
    taSouth.textureDescription.minFilter = TextureFilter.Nearest;
    taSouth.textureDescription.magFilter = TextureFilter.Nearest;

    final Material matSouth = new Material();
    matSouth.set(taSouth);

    mdlWallSouth = mdlBuilder.createRect(Constants.HALF_UNIT, Constants.HALF_UNIT, 0,
        -Constants.HALF_UNIT,
        Constants.HALF_UNIT, 0, -Constants.HALF_UNIT, -Constants.HALF_UNIT, 0, Constants.HALF_UNIT,
        -Constants.HALF_UNIT, 0, 0, 0, -1, matSouth,
        Usage.Position | Usage.Normal | Usage.TextureCoordinates);
    mdlWallSouth.nodes.get(0).rotation.set(Vector3.Y, 180f);

//	WEST WALL
    final TextureRegion texRegWest = game.getAssMan().getTextureRegion(atlas, 0,
        16, 16, 16);

    final TextureAttribute taWest = new TextureAttribute(TextureAttribute.Diffuse, texRegWest);
    taWest.textureDescription.minFilter = TextureFilter.Nearest;
    taWest.textureDescription.magFilter = TextureFilter.Nearest;

    final Material matWest = new Material();
    matWest.set(taWest);

    mdlWallWest = mdlBuilder.createRect(Constants.HALF_UNIT, Constants.HALF_UNIT, 0,
        -Constants.HALF_UNIT,
        Constants.HALF_UNIT, 0, -Constants.HALF_UNIT, -Constants.HALF_UNIT, 0, Constants.HALF_UNIT,
        -Constants.HALF_UNIT, 0, 0, 0, -1, matWest,
        Usage.Position | Usage.Normal | Usage.TextureCoordinates);
    mdlWallWest.nodes.get(0).rotation.set(Vector3.Y, -90f);

//	EAST WALL
    final TextureRegion texRegEast = game.getAssMan().getTextureRegion(atlas, 48,
        16, -16, 16); // flip y

    final TextureAttribute taEast = new TextureAttribute(TextureAttribute.Diffuse, texRegEast);
    taEast.textureDescription.minFilter = TextureFilter.Nearest;
    taEast.textureDescription.magFilter = TextureFilter.Nearest;

    final Material matEast = new Material();
    matEast.set(taEast);

    mdlWallEast = mdlBuilder.createRect(Constants.HALF_UNIT, Constants.HALF_UNIT, 0,
        -Constants.HALF_UNIT,
        Constants.HALF_UNIT, 0, -Constants.HALF_UNIT, -Constants.HALF_UNIT, 0, Constants.HALF_UNIT,
        -Constants.HALF_UNIT, 0, 0, 0, -1, matEast,
        Usage.Position | Usage.Normal | Usage.TextureCoordinates);
    mdlWallEast.nodes.get(0).rotation.set(Vector3.Y, 90f);

//	FLOOR
    final TextureRegion texRegFloor = game.getAssMan().getTextureRegion(atlas,
        32, 32, -16, 16); // flip x

    final TextureAttribute taFloor = new TextureAttribute(TextureAttribute.Diffuse, texRegFloor);
    taFloor.textureDescription.minFilter = TextureFilter.Nearest;
    taFloor.textureDescription.magFilter = TextureFilter.Nearest;

    final Material matFloor = new Material();
    matFloor.set(taFloor);

    mdlFloor = mdlBuilder.createRect(Constants.HALF_UNIT, Constants.HALF_UNIT, 0,
        -Constants.HALF_UNIT,
        Constants.HALF_UNIT, 0, -Constants.HALF_UNIT, -Constants.HALF_UNIT, 0, Constants.HALF_UNIT,
        -Constants.HALF_UNIT, 0, 0, 1, 0, matFloor,
        Usage.Position | Usage.Normal | Usage.TextureCoordinates);
    mdlFloor.nodes.get(0).rotation.set(Vector3.X, 90f);

//		CEILING
    final TextureRegion texRegCeiling = game.getAssMan().getTextureRegion(atlas,
        32, 0, -16, 16); // flip x

    final TextureAttribute taCeiling = new TextureAttribute(TextureAttribute.Diffuse,
        texRegCeiling);
    taCeiling.textureDescription.minFilter = TextureFilter.Nearest;
    taCeiling.textureDescription.magFilter = TextureFilter.Nearest;

    final Material matCeiling = new Material();
    matCeiling.set(taCeiling);

    mdlCeiling = mdlBuilder.createRect(Constants.HALF_UNIT, Constants.HALF_UNIT, 0,
        -Constants.HALF_UNIT,
        Constants.HALF_UNIT, 0, -Constants.HALF_UNIT, -Constants.HALF_UNIT, 0, Constants.HALF_UNIT,
        -Constants.HALF_UNIT, 0, 0, -1, 0, matCeiling,
        Usage.Position | Usage.Normal | Usage.TextureCoordinates);
    mdlCeiling.nodes.get(0).rotation.set(Vector3.X,
        -90f); // not totally correct. Should flip Y. Maybe not needed.

//	DOOR
    final TextureRegion texRegDoorNorth = game.getAssMan()
        .getTextureRegionFlipped(atlas, 96, 48, -16, 16);
    final TextureRegion texRegDoorSouth = game.getAssMan()
        .getTextureRegionFlipped(atlas, 80, 48, 16, 16);
    final TextureRegion texRegDoorMiddle = game.getAssMan()
        .getTextureRegionFlipped(atlas, 100, 48, -4, 16);

    final TextureAttribute taDoorNorth = new TextureAttribute(TextureAttribute.Diffuse,
        texRegDoorNorth);
    taDoorNorth.textureDescription.minFilter = TextureFilter.Nearest;
    taDoorNorth.textureDescription.magFilter = TextureFilter.Nearest;

    final TextureAttribute taDoorSouth = new TextureAttribute(TextureAttribute.Diffuse,
        texRegDoorSouth);
    taDoorSouth.textureDescription.minFilter = TextureFilter.Nearest;
    taDoorSouth.textureDescription.magFilter = TextureFilter.Nearest;

    final TextureAttribute taDoorMiddle = new TextureAttribute(TextureAttribute.Diffuse,
        texRegDoorMiddle);
    taDoorMiddle.textureDescription.minFilter = TextureFilter.Nearest;
    taDoorMiddle.textureDescription.magFilter = TextureFilter.Nearest;

    final Material matDoorNorth = new Material();
    matDoorNorth.set(taDoorNorth);
    final Material matDoorSouth = new Material();
    matDoorSouth.set(taDoorSouth);
    final Material matDoorMiddle = new Material();
    matDoorMiddle.set(taDoorMiddle);

    mdlBuilder.begin();
    MeshPartBuilder meshBuilder;
    final Node node0 = mdlBuilder.node();
    meshBuilder = mdlBuilder.part("northSide", GL20.GL_TRIANGLES,
        Usage.Position | Usage.Normal | Usage.TextureCoordinates, matDoorNorth);
    meshBuilder.rect(new Vector3(Constants.HALF_UNIT, Constants.HALF_UNIT, 0),
        new Vector3(-Constants.HALF_UNIT, Constants.HALF_UNIT, 0),
        new Vector3(-Constants.HALF_UNIT, -Constants.HALF_UNIT, 0),
        new Vector3(Constants.HALF_UNIT, -Constants.HALF_UNIT, 0), new Vector3(0, 0, -1));
    node0.translation.set(0, 0, Constants.PPU * 2);

    final Node node1 = mdlBuilder.node();
    meshBuilder = mdlBuilder.part("southSide", GL20.GL_TRIANGLES,
        Usage.Position | Usage.Normal | Usage.TextureCoordinates, matDoorSouth);
    meshBuilder.rect(new Vector3(Constants.HALF_UNIT, Constants.HALF_UNIT, 0),
        new Vector3(-Constants.HALF_UNIT, Constants.HALF_UNIT, 0),
        new Vector3(-Constants.HALF_UNIT, -Constants.HALF_UNIT, 0),
        new Vector3(Constants.HALF_UNIT, -Constants.HALF_UNIT, 0), new Vector3(0, 0, 1));

    node1.rotation.set(Vector3.Y, 180f);
    node1.translation.set(0, 0, Constants.PPU * -2);

    mdlDoor = mdlBuilder.end();
  }
}
