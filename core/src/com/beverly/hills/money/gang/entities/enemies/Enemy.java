package com.beverly.hills.money.gang.entities.enemies;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.beverly.hills.money.gang.assets.managers.sound.SoundVolumeType;
import com.beverly.hills.money.gang.models.ModelInstanceBB;
import com.beverly.hills.money.gang.rect.RectanglePlus;
import com.beverly.hills.money.gang.entities.Entity;
import com.beverly.hills.money.gang.entities.player.Player;
import com.beverly.hills.money.gang.screens.GameScreen;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Consumer;

public abstract class Enemy extends Entity {

    @Getter
    private final Player player;

    private final Consumer<Enemy> onDeath;
    private final Consumer<Enemy> onGetShot;
    @Getter
    private final Consumer<Enemy> onShooting;

    @Getter
    private final Vector3 position;
    @Setter
    @Getter
    private ModelInstanceBB mdlInst;

    @Setter
    private RectanglePlus rect;
    @Getter
    private boolean isDead;

    public Enemy(final Vector3 position, final GameScreen screen, final Player player,
                 final Consumer<Enemy> onDeath, final Consumer<Enemy> onGetShot, final Consumer<Enemy> onShooting) {
        super(screen);
        this.player = player;
        this.position = position;
        this.onGetShot = onGetShot;
        this.onDeath = onDeath;
        this.onShooting = onShooting;
    }


    public SoundVolumeType getSFXVolume() {
        float distance = Vector2.dst2(player.getRect().x, player.getRect().y, getRect().x, getRect().y);
        if (distance < 2f) {
            return SoundVolumeType.VERY_LOUD;
        } else if (distance < 10f) {
            return SoundVolumeType.LOUD;
        } else if (distance < 30f) {
            return SoundVolumeType.MEDIUM;
        } else {
            return SoundVolumeType.QUITE;
        }
    }

    @Override
    public void destroy() {
        if (rect != null) {
            getScreen().getGame().getRectMan().removeRect(rect);
        }
        super.destroy(); // should be last.
    }

    public RectanglePlus getRect() {
        return rect;
    }


    @Override
    public void render3D(final ModelBatch mdlBatch, final Environment env, final float delta) {
        if (mdlInst != null) {
            mdlInst.setInFrustum(getScreen().frustumCull(getScreen().getCurrentCam(), mdlInst));
            if (mdlInst.isInFrustum()) {
                mdlBatch.render(mdlInst, env);
            }
        }
    }

    public void getShot() {
        onGetShot.accept(this);
    }

    public void die() {
        isDead = true;
        onDeath.accept(this);
    }
}
