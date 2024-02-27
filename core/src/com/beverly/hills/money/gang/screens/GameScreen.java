package com.beverly.hills.money.gang.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.beverly.hills.money.gang.DaiKombatGame;
import com.beverly.hills.money.gang.entities.Entity;
import com.beverly.hills.money.gang.entities.player.Player;
import com.beverly.hills.money.gang.models.ModelInstanceBB;
import com.beverly.hills.money.gang.rect.RectanglePlus;
import lombok.Getter;
import lombok.Setter;

public abstract class GameScreen implements Screen {

    @Getter
    private final DaiKombatGame game;
    @Getter
    private final Vector3 currentSpherePos = new Vector3();
    @Getter
    @Setter
    private Camera currentCam;
    @Getter
    private final Viewport viewport;
    @Getter
    @Setter
    private Player player;

    @Getter
    private boolean exiting;

    public GameScreen(final DaiKombatGame game, final Viewport viewport) {
        this.game = game;
        this.viewport = viewport;
        this.game.setGameIsPaused(false);
        game.getEntMan().setScreen(this);
    }


    public void checkOverlaps(final RectanglePlus rect) {
        checkOverlapX(rect);
        checkOverlapY(rect);

        rect.setOverlapX(false);
        rect.setOverlapY(false);
    }

    /**
     * Check for overlap in angle X.
     */
    private void checkOverlapX(final RectanglePlus rect) {
        rect.setX(rect.getNewPosition().x);

        rect.setOverlapX(game.getRectMan().checkCollision(rect));

        if (rect.isOverlapX()) {
            rect.getNewPosition().x = rect.getOldPosition().x;
        }

        rect.setX(rect.getNewPosition().x);
    }

    /**
     * Check for overlap in angle Y.
     */
    private void checkOverlapY(final RectanglePlus rect) {
        rect.setY(rect.getNewPosition().y);

        rect.setOverlapY(game.getRectMan().checkCollision(rect));

        if (rect.isOverlapY()) {
            rect.getNewPosition().y = rect.getOldPosition().y;
        }
        rect.setY(rect.getNewPosition().y);
    }

    @Override
    public void dispose() {

    }

    public boolean frustumCull(final Camera cam, final ModelInstanceBB modelInst) {
        modelInst.calculateBoundingBox(modelInst.getRenderBox());
        modelInst.getRenderBox().mul(modelInst.transform.cpy());

        modelInst.transform.getTranslation(currentSpherePos);
        currentSpherePos.add(modelInst.getCenter());

        return cam.frustum.sphereInFrustum(currentSpherePos, modelInst.getRadius());
    }

    public abstract void handleInput(final float delta);

    @Override
    public void hide() {

    }

    @Override
    public void pause() {

    }

    protected void removeAllEntities() {
        for (final Entity ent : game.getEntMan().entities) {
            ent.destroy();
        }

        game.getEntMan().entities.clear(); // Removes cell3Ds and doors.
        game.getRectMan().getRects().clear(); // remove rect walls too.
    }

    @Override
    public void render(final float delta) {
        if (exiting) {
            return;
        }
        handleInput(delta);
        tick(delta);
    }

    @Override
    public void resize(final int width, final int height) {
        if (viewport != null) {
            viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }
    }

    @Override
    public void resume() {

    }

    @Override
    public void show() {

    }

    public void tick(final float delta) {
        if (!game.isGameIsPaused()) {
            game.getEntMan().tickAllEntities(delta);
        }
    }

    public final void exit() {
        exiting = true;
        onExitScreen();
    }

    public void onExitScreen() {
        // do nothing by default
    }

}
