package com.beverly.hills.money.gang.entities.enemies;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.beverly.hills.money.gang.Configs;
import com.beverly.hills.money.gang.Constants;
import com.beverly.hills.money.gang.entities.player.Player;
import com.beverly.hills.money.gang.models.ModelInstanceBB;
import com.beverly.hills.money.gang.rect.RectanglePlus;
import com.beverly.hills.money.gang.rect.filters.RectanglePlusFilter;
import com.beverly.hills.money.gang.screens.GameScreen;
import lombok.Getter;

import java.util.ArrayDeque;
import java.util.Queue;

public class EnemyPlayer extends Enemy {

    private final EnemyTextures enemyTextures;

    private long dieAnimationEndMls = Long.MAX_VALUE;

    private final Queue<EnemyPlayerAction> actions = new ArrayDeque<>();

    @Getter
    private final String name;
    private long redUntil;
    private long movingAnimationUntil;

    private long shootingAnimationUntil;
    private int currentStep;
    private final float speed;
    private Vector2 lastDirection;
    private boolean isIdle = true;

    @Getter
    private final int enemyPlayerId;

    public EnemyPlayer(final Player player,
                       final int enemyPlayerId,
                       final Vector3 position, Vector2 direction, final GameScreen screen, final String name, EnemyListeners enemyListeners
    ) {

        super(position, screen, player, enemyListeners);
        this.enemyPlayerId = enemyPlayerId;
        lastDirection = direction;
        enemyTextures = new EnemyTextures(screen.getGame().getAssMan());
        speed = Configs.PLAYER_MOVE_SPEED;
        this.name = name;
        super.setMdlInst(new ModelInstanceBB(screen.getGame().getCellBuilder().getMdlEnemy()));
        Attributes attributes = getMdlInst().materials.get(0);
        attributes.set(TextureAttribute.createDiffuse(enemyTextures.getEnemyPlayerTextureRegion(EnemyTextureRegistry.IDLETEXFRONTREG)));
        attributes.set(new ColorAttribute(ColorAttribute.Diffuse, Color.WHITE));
        attributes.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
        attributes.set(new FloatAttribute(FloatAttribute.AlphaTest));

        setRect(new RectanglePlus(this.getPosition().x, this.getPosition().z, Constants.PLAYER_RECT_SIZE, Constants.PLAYER_RECT_SIZE, getEntityId(),
                RectanglePlusFilter.ENEMY));
        getRect().setPosition(getRect().x, getRect().y);
        screen.getGame().getRectMan().addRect(getRect());
        getRect().getOldPosition().set(getRect().x, getRect().y);
        getRect().getNewPosition().set(getRect().x, getRect().y);
    }


    public void queueAction(EnemyPlayerAction enemyPlayerAction) {
        actions.add(enemyPlayerAction);
    }


    private void flashRedIfHit() {
        final ColorAttribute colorAttribute = (ColorAttribute) getMdlInst().materials.get(0).get(ColorAttribute.Diffuse);
        if (System.currentTimeMillis() < redUntil) {
            colorAttribute.color.set(Color.WHITE.cpy().lerp(Color.RED, 1));
        } else {
            colorAttribute.color.set(Color.WHITE.cpy().lerp(Color.RED, 0));
        }
    }

    public void shoot() {
        shootingAnimationUntil = System.currentTimeMillis() + 100;
        getEnemyListeners().getOnShooting().accept(this);
    }

    public void punch() {
        getEnemyListeners().getOnPunching().accept(this);
    }

    @Override
    public void render3D(final ModelBatch mdlBatch, final Environment env, final float delta) {
        getMdlInst().transform.setToLookAt(getScreen().getCurrentCam().direction.cpy().rotate(Vector3.Z, 180f), Vector3.Y);
        getMdlInst().transform.setTranslation(getPosition().cpy().add(0, Constants.HALF_UNIT, 0));
        // otherwise, it the enemy is too "fat"
        getMdlInst().transform.scale(0.8f, 1f, 1);
        super.render3D(mdlBatch, env, delta);
    }

    @Override
    public void getHit() {
        super.getHit();
        redUntil = getAnimationTimeoutMls();
    }

    @Override
    public void die() {
        super.die();
        dieAnimationEndMls = getAnimationTimeoutMls();
    }

    @Override
    public void tick(final float delta) {
        if (isDead()) {
            if (System.currentTimeMillis() < dieAnimationEndMls) {
                getMdlInst().materials.get(0).set(TextureAttribute
                        .createDiffuse(enemyTextures.getEnemyPlayerTextureRegion(EnemyTextureRegistry.DEATHTEXREG)));
            } else {
                destroy();
            }
            return;
        }
        EnemyPlayerAction action = actions.peek();

        if (action != null) {
            Vector2 targetPosition = action.getRoute();
            lastDirection = action.getDirection();
            Vector2 rectDirection = new Vector2();
            rectDirection.x = targetPosition.x - getRect().x;
            rectDirection.y = targetPosition.y - getRect().y;
            rectDirection.nor().scl(speed * delta);
            isIdle = false;
            getRect().getNewPosition().add(rectDirection.x, rectDirection.y);
            if (isTooClose(getRect().getOldPosition(), targetPosition)) {
                // if we are close to the target destination then we are here
                actions.remove();

                switch (action.getEnemyPlayerActionType()) {
                    case SHOOT -> shoot();
                    case PUNCH -> punch();
                }

            }
        } else {
            isIdle = true;
        }
        getRect().setX(getRect().getNewPosition().x);
        getRect().setY(getRect().getNewPosition().y);
        getPosition().set(getRect().x + getRect().getWidth() / 2, 0, getRect().y + getRect().getHeight() / 2);
        getRect().getOldPosition().set(getRect().x, getRect().y);
        flashRedIfHit();
        rotateEnemyAnimation();
    }

    private void rotateEnemyAnimation() {
        if (lastDirection == null) {
            return;
        }
        TextureRegion textureRegionToUse = getTextureToUse();
        if (textureRegionToUse != null) {
            getMdlInst().materials.get(0).set(TextureAttribute.createDiffuse(textureRegionToUse));
        }
    }

    private TextureRegion getTextureToUse() {
        Vector2 playerDirection = new Vector2(
                getPlayer().getPlayerCam().direction.x,
                getPlayer().getPlayerCam().direction.z);
        float angle = playerDirection.angleDeg(lastDirection);

        if (Constants.FRONT_RANGE.contains(angle)) {
            if (keepShootingAnimation()) {
                return enemyTextures.getEnemyPlayerTextureRegion(EnemyTextureRegistry.SHOOTINGTEXFRONTREG);
            } else if (isIdle) {
                return enemyTextures.getEnemyPlayerTextureRegion(EnemyTextureRegistry.IDLETEXFRONTREG);
            } else if (keepMovingAnimation()) {
                movingAnimationUntil = getAnimationTimeoutMls();
                currentStep++;
                return enemyTextures.getEnemyPlayerMoveFrontTextureRegion(currentStep);
            }
        } else if (Constants.LEFT_RANGE.contains(angle)) {
            if (keepShootingAnimation()) {
                return enemyTextures.getEnemyPlayerTextureRegion(EnemyTextureRegistry.SHOOTINGTEXLEFTREG);
            } else if (isIdle) {
                return enemyTextures.getEnemyPlayerTextureRegion(EnemyTextureRegistry.IDLETEXLEFTREG);
            } else if (keepMovingAnimation()) {
                movingAnimationUntil = getAnimationTimeoutMls();
                currentStep++;
                return enemyTextures.getEnemyPlayerMoveLeftTextureRegion(currentStep);
            }
        } else if (Constants.RIGHT_RANGE.contains(angle)) {
            if (keepShootingAnimation()) {
                return enemyTextures.getEnemyPlayerTextureRegion(EnemyTextureRegistry.SHOOTINGTEXRIGHTREG);
            } else if (isIdle) {
                return enemyTextures.getEnemyPlayerTextureRegion(EnemyTextureRegistry.IDLETEXRIGHTREG);
            } else if (keepMovingAnimation()) {
                movingAnimationUntil = getAnimationTimeoutMls();
                currentStep++;
                return enemyTextures.getEnemyPlayerMoveRightTextureRegion(currentStep);
            }
        } else {
            if (keepShootingAnimation()) {
                return enemyTextures.getEnemyPlayerTextureRegion(EnemyTextureRegistry.SHOOTINGTEXBACKTREG);
            } else if (isIdle) {
                return enemyTextures.getEnemyPlayerTextureRegion(EnemyTextureRegistry.IDLETEXBACKREG);
            } else if (keepMovingAnimation()) {
                movingAnimationUntil = getAnimationTimeoutMls();
                currentStep += 1;
                return enemyTextures.getEnemyPlayerMoveBackTextureRegion(currentStep);
            }
        }
        return null;
    }

    private boolean keepShootingAnimation() {
        return System.currentTimeMillis() <= shootingAnimationUntil;
    }

    private boolean keepMovingAnimation() {
        return System.currentTimeMillis() >= movingAnimationUntil;
    }

    private long getAnimationTimeoutMls() {
        return System.currentTimeMillis() + 100;
    }


    private boolean isTooClose(Vector2 vector1, Vector2 vector2) {
        return Vector2.dst(vector1.x, vector1.y, vector2.x,
                vector2.y) <= 0.1f;
    }
}
