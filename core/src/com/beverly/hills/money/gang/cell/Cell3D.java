package com.beverly.hills.money.gang.cell;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.math.Vector3;
import com.beverly.hills.money.gang.models.ModelInstanceBB;
import com.beverly.hills.money.gang.entities.Entity;
import com.beverly.hills.money.gang.screens.GameScreen;

public class Cell3D extends Entity {
    public final Vector3 position = new Vector3();
    public boolean hasWallNorth = true;
    public boolean hasWallSouth = true;
    public boolean hasWallWest = true;
    public boolean hasWallEast = true;
    public boolean hasFloor = false;
    public boolean hasCeiling = false;
    public TextureRegion texRegNorth, texRegSouth, texRegWest, texRegEast, texRegFloor, texRegCeiling;
    private ModelInstanceBB mdlInstWallNorth;
    private ModelInstanceBB mdlInstWallSouth;
    private ModelInstanceBB mdlInstWallWest;
    private ModelInstanceBB mdlInstWallEast;
    private ModelInstanceBB mdlInstFloor;
    private ModelInstanceBB mdlInstCeiling;

    public Cell3D(final Vector3 position, final GameScreen screen) {
        super(screen);
        this.position.set(position.cpy().add(0, 0.5f, 0));
    }

    public void buildCell() {
        if (hasWallNorth) {
            mdlInstWallNorth = new ModelInstanceBB(getScreen().getGame().getCellBuilder().getMdlWallNorth());

            if (texRegNorth != null) {
                final TextureAttribute ta = (TextureAttribute) mdlInstWallNorth.materials.get(0)
                        .get(TextureAttribute.Diffuse);
                ta.set(texRegNorth);
            }

            mdlInstWallNorth.transform.setToTranslation(this.position.cpy().add(new Vector3(0, 0, -0.5f)));
        }

        if (hasWallSouth) {
            mdlInstWallSouth = new ModelInstanceBB(getScreen().getGame().getCellBuilder().getMdlWallSouth());

            if (texRegSouth != null) {
                final TextureAttribute ta = (TextureAttribute) mdlInstWallSouth.materials.get(0)
                        .get(TextureAttribute.Diffuse);
                ta.set(texRegSouth);
            }

            mdlInstWallSouth.transform.setToTranslation(this.position.cpy().add(new Vector3(0, 0, 0.5f)));
        }

        if (hasWallWest) {
            mdlInstWallWest = new ModelInstanceBB(getScreen().getGame().getCellBuilder().getMdlWallWest());

            if (texRegWest != null) {
                final TextureAttribute ta = (TextureAttribute) mdlInstWallWest.materials.get(0)
                        .get(TextureAttribute.Diffuse);
                ta.set(texRegWest);
            }

            mdlInstWallWest.transform.setToTranslation(this.position.cpy().add(new Vector3(0.5f, 0, 0)));
        }

        if (hasWallEast) {
            mdlInstWallEast = new ModelInstanceBB(getScreen().getGame().getCellBuilder().getMdlWallEast());

            if (texRegEast != null) {
                final TextureAttribute ta = (TextureAttribute) mdlInstWallEast.materials.get(0)
                        .get(TextureAttribute.Diffuse);
                ta.set(texRegEast);
            }

            mdlInstWallEast.transform.setToTranslation(this.position.cpy().add(new Vector3(-0.5f, 0, 0)));
        }

        if (hasFloor) {
            mdlInstFloor = new ModelInstanceBB(getScreen().getGame().getCellBuilder().getMdlFloor());

            if (texRegFloor != null) {
                final TextureAttribute ta = (TextureAttribute) mdlInstFloor.materials.get(0)
                        .get(TextureAttribute.Diffuse);
                ta.set(texRegFloor);
            }

            mdlInstFloor.transform.setToTranslation(this.position.cpy().add(new Vector3(0, 0.5f, 0)));
        }

        if (hasCeiling) {
            mdlInstCeiling = new ModelInstanceBB(getScreen().getGame().getCellBuilder().getMdlCeiling());

            if (texRegCeiling != null) {
                final TextureAttribute ta = (TextureAttribute) mdlInstCeiling.materials.get(0)
                        .get(TextureAttribute.Diffuse);
                ta.set(texRegCeiling);
            }

            mdlInstCeiling.transform.setToTranslation(this.position.cpy().add(new Vector3(0, -0.5f, 0)));
        }
    }

    @Override
    public void render3D(final ModelBatch mdlBatch, final Environment env, final float delta) {
        if (hasWallNorth) {
            mdlInstWallNorth.setInFrustum(getScreen().frustumCull(getScreen().getCurrentCam(), mdlInstWallNorth));
            if (mdlInstWallNorth.isInFrustum()) {
                mdlBatch.render(mdlInstWallNorth, env);
            }
        }

        if (hasWallSouth) {
            mdlInstWallSouth.setInFrustum(getScreen().frustumCull(getScreen().getCurrentCam(), mdlInstWallSouth));
            if (mdlInstWallSouth.isInFrustum()) {
                mdlBatch.render(mdlInstWallSouth, env);
            }
        }

        if (hasWallWest) {
            mdlInstWallWest.setInFrustum(getScreen().frustumCull(getScreen().getCurrentCam(), mdlInstWallWest));
            if (mdlInstWallWest.isInFrustum()) {
                mdlBatch.render(mdlInstWallWest, env);
            }
        }

        if (hasWallEast) {
            mdlInstWallEast.setInFrustum(getScreen().frustumCull(getScreen().getCurrentCam(), mdlInstWallEast));
            if (mdlInstWallEast.isInFrustum()) {
                mdlBatch.render(mdlInstWallEast, env);
            }
        }

        if (hasFloor) {
            mdlInstFloor.setInFrustum(getScreen().frustumCull(getScreen().getCurrentCam(), mdlInstFloor));
            if (mdlInstFloor.isInFrustum()) {
                mdlBatch.render(mdlInstFloor, env);
            }
        }

        if (hasCeiling) {
            mdlInstCeiling.setInFrustum(getScreen().frustumCull(getScreen().getCurrentCam(), mdlInstCeiling));
            if (mdlInstCeiling.isInFrustum()) {
                mdlBatch.render(mdlInstCeiling, env);
            }
        }
    }

}
