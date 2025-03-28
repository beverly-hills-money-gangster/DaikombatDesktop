package com.beverly.hills.money.gang.entities.enemies;

import static com.beverly.hills.money.gang.Constants.DEFAULT_ENEMY_Y;
import static com.beverly.hills.money.gang.Constants.MAX_ENEMY_IDLE_TIME_MLS;
import static com.beverly.hills.money.gang.Constants.SPAWN_ANIMATION_MLS;

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
import com.beverly.hills.money.gang.Constants;
import com.beverly.hills.money.gang.entities.effect.EnemyPlayerVoiceChatEffect;
import com.beverly.hills.money.gang.entities.item.PowerUpType;
import com.beverly.hills.money.gang.entities.player.Player;
import com.beverly.hills.money.gang.models.ModelInstanceBB;
import com.beverly.hills.money.gang.rect.RectanglePlus;
import com.beverly.hills.money.gang.rect.filters.RectanglePlusFilter;
import com.beverly.hills.money.gang.screens.GameScreen;
import com.beverly.hills.money.gang.screens.ui.selection.GamePlayerClass;
import com.beverly.hills.money.gang.screens.ui.selection.SkinUISelection;
import com.beverly.hills.money.gang.screens.ui.weapon.Weapon;
import com.beverly.hills.money.gang.strategy.EnemyPlayerActionQueueStrategy;
import java.util.ArrayDeque;
import java.util.Queue;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnemyPlayer extends Enemy {

  private static final Logger LOG = LoggerFactory.getLogger(EnemyPlayer.class);

  private final EnemyTextures enemyTextures;

  private final Queue<EnemyPlayerAction> actions = new ArrayDeque<>();

  private final EnemyPlayerActionQueueStrategy enemyPlayerActionQueueStrategy;

  @Getter
  private final EnemyPlayerVoiceChatEffect enemyPlayerVoiceChatEffect;

  @Getter
  private final String name;


  @Getter
  private final SkinUISelection skinUISelection;

  @Getter
  @Setter
  private int hp;

  private long movingAnimationUntil;

  private long shootingAnimationUntil;
  private int currentStep;

  @Setter
  private float currentSpeed;
  @Getter
  private Vector2 lastDirection;
  private boolean isIdle = true;

  @Getter
  private boolean visible = true;

  @Getter
  private final int enemyPlayerId;

  private final int maxVisibility;

  private long lastActionReceivedTimeMls;

  public EnemyPlayer(
      final Player player,
      final int enemyPlayerId,
      final Vector3 position,
      final Vector2 direction,
      final GameScreen screen,
      final String name,
      final SkinUISelection skinUISelection,
      final EnemyListeners enemyListeners,
      final float speed,
      final int hp,
      final GamePlayerClass enemyClass,
      final int maxVisibility) {
    super(position, screen, player, enemyClass, enemyListeners);
    enemyPlayerVoiceChatEffect = new EnemyPlayerVoiceChatEffect(position, screen);
    this.maxVisibility = maxVisibility;
    this.hp = hp;
    this.skinUISelection = skinUISelection;
    this.enemyPlayerId = enemyPlayerId;
    lastDirection = direction;
    enemyTextures = new EnemyTextures(screen.getGame().getAssMan(), enemyClass, skinUISelection);
    this.currentSpeed = speed;
    this.name = name;
    super.setMdlInst(new ModelInstanceBB(screen.getGame().getCellBuilder().getMdlEnemy()));
    Attributes attributes = getMdlInst().materials.get(0);
    attributes.set(TextureAttribute.createDiffuse(
        enemyTextures.getEnemyPlayerTextureRegion(EnemyTextureRegistry.IDLETEXFRONTREG)));
    attributes.set(new ColorAttribute(ColorAttribute.Diffuse, Color.WHITE));
    attributes.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
    attributes.set(new FloatAttribute(FloatAttribute.AlphaTest));
    setRect();
    screen.getGame().getRectMan().addRect(getRect());
    getEnemyEffects().beingSpawned(System.currentTimeMillis() + SPAWN_ANIMATION_MLS);
    enemyPlayerActionQueueStrategy = new EnemyPlayerActionQueueStrategy(
        actions,
        enemyPlayerAction ->
            teleport(new Vector3(enemyPlayerAction.getRoute().x, DEFAULT_ENEMY_Y,
                    enemyPlayerAction.getRoute().y),
                enemyPlayerAction.getDirection()),
        this::setCurrentSpeed,
        speed);
    lastActionReceivedTimeMls = System.currentTimeMillis();
  }

  public void talking(float avgAmpl) {
    if (!getEnemyEffects().isPowerUpActive(PowerUpType.INVISIBILITY)) {
      enemyPlayerVoiceChatEffect.makeVisible();
    }
  }


  public void teleport(Vector3 position, Vector2 direction) {
    getEnemyEffects().beingSpawned(System.currentTimeMillis() + SPAWN_ANIMATION_MLS);
    this.getPosition().set(position);
    this.lastDirection = direction;
    getScreen().getGame().getRectMan().removeRect(getRect());
    setRect();
    getScreen().getGame().getRectMan().addRect(getRect());
    rotateEnemyAnimation();
    visible = false;
  }

  private void setRect() {
    setRect(
        new RectanglePlus(this.getPosition().x, this.getPosition().z, Constants.PLAYER_RECT_SIZE,
            Constants.PLAYER_RECT_SIZE, getEntityId(),
            RectanglePlusFilter.ENEMY));
    getRect().setPosition(getRect().x, getRect().y);
    getRect().getOldPosition().set(getRect().x, getRect().y);
    getRect().getNewPosition().set(getRect().x, getRect().y);
  }

  public void queueAction(EnemyPlayerAction enemyPlayerAction) {
    enemyPlayerActionQueueStrategy.enqueue(enemyPlayerAction, getRect().getOldPosition(), visible);
    if (!isInFog()) {
      visible = true;
    }
    lastActionReceivedTimeMls = System.currentTimeMillis();
  }

  public void attack(Weapon weapon, boolean attackingPlayer) {
    if (weapon != Weapon.GAUNTLET) {
      shootingAnimationUntil = System.currentTimeMillis() + 100;
    }
    getEnemyListeners().getOnAttack()
        .accept(EnemyWeapon.builder().weapon(weapon).attackingPlayer(attackingPlayer)
            .enemy(this).build());
  }

  @Override
  public void render3D(final ModelBatch mdlBatch, final Environment env, final float delta) {
    getMdlInst().transform.setToLookAt(
        getScreen().getCurrentCam().direction.cpy().rotate(Vector3.Z, 180f), Vector3.Y);
    getMdlInst().transform.setTranslation(getPosition().cpy().add(0, Constants.HALF_UNIT, 0));
    // otherwise, the enemy is too "fat"
    getMdlInst().transform.scale(0.8f, 1f, 1);
    super.render3D(mdlBatch, env, delta);
  }


  @Override
  public void tick(final float delta) {
    if (isDead()) {
      if (System.currentTimeMillis() < getDieAnimationEndMls()) {
        getMdlInst().materials.get(0).set(TextureAttribute
            .createDiffuse(
                enemyTextures.getEnemyPlayerTextureRegion(EnemyTextureRegistry.DEATHTEXREG)));
      } else {
        destroy();
      }
      return;
    }

    final ColorAttribute colorAttribute = (ColorAttribute) getMdlInst().materials.get(0)
        .get(ColorAttribute.Diffuse);
    long noActivityMls = System.currentTimeMillis() - lastActionReceivedTimeMls;
    if (visible && noActivityMls > MAX_ENEMY_IDLE_TIME_MLS) {
      LOG.info("No activity for enemy {}. Last received {}. Current mls {}. No activity for {} mls",
          enemyPlayerId, lastActionReceivedTimeMls, System.currentTimeMillis(), noActivityMls);
      visible = false;
    }

    EnemyPlayerAction action = actions.peek();

    if (action != null) {
      Vector2 targetPosition = action.getRoute();
      lastDirection = action.getDirection();
      if (isTooClose(getRect().getOldPosition(), targetPosition)) {
        // if we are close to the target destination then we are here
        actions.remove();
        action.getOnComplete().run();
      } else {
        Vector2 rectDirection = new Vector2();
        rectDirection.x = targetPosition.x - getRect().x;
        rectDirection.y = targetPosition.y - getRect().y;
        rectDirection.nor().scl(currentSpeed * delta);
        isIdle = false;
        getRect().getNewPosition().add(rectDirection.x, rectDirection.y);
      }
    } else if (System.currentTimeMillis() >= movingAnimationUntil) {
      isIdle = true;
    }
    enemyPlayerVoiceChatEffect.setPosition(getRect().getNewPosition().x,
        getRect().getNewPosition().y);
    getRect().setX(getRect().getNewPosition().x);
    getRect().setY(getRect().getNewPosition().y);
    getPosition().set(getRect().x + getRect().getWidth() / 2, 0,
        getRect().y + getRect().getHeight() / 2);
    getRect().getOldPosition().set(getRect().x, getRect().y);

    if (visible && isInFog()) {
      visible = false;
    }
    if (visible) {
      colorEffects();
      rotateEnemyAnimation();
    } else {
      colorAttribute.color.set(1, 1, 1, 0);
    }
  }

  private boolean isInFog() {
    return getPlayer().getCurrent2DPosition().dst(new Vector2(getPosition().x, getPosition().z))
        > maxVisibility * 0.90;
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
      if (isShootingAnimation()) {
        return enemyTextures.getEnemyPlayerTextureRegion(EnemyTextureRegistry.SHOOTINGTEXFRONTREG);
      } else if (isIdle) {
        return enemyTextures.getEnemyPlayerTextureRegion(EnemyTextureRegistry.IDLETEXFRONTREG);
      } else if (isNextStepAnimation()) {
        movingAnimationUntil = getAnimationTimeoutMls();
        currentStep++;
        return enemyTextures.getEnemyPlayerMoveFrontTextureRegion(currentStep);
      }
    } else if (Constants.LEFT_RANGE.contains(angle)) {
      if (isShootingAnimation()) {
        return enemyTextures.getEnemyPlayerTextureRegion(EnemyTextureRegistry.SHOOTINGTEXLEFTREG);
      } else if (isIdle) {
        return enemyTextures.getEnemyPlayerTextureRegion(EnemyTextureRegistry.IDLETEXLEFTREG);
      } else if (isNextStepAnimation()) {
        movingAnimationUntil = getAnimationTimeoutMls();
        currentStep++;
        return enemyTextures.getEnemyPlayerMoveLeftTextureRegion(currentStep);
      }
    } else if (Constants.RIGHT_RANGE.contains(angle)) {
      if (isShootingAnimation()) {
        return enemyTextures.getEnemyPlayerTextureRegion(EnemyTextureRegistry.SHOOTINGTEXRIGHTREG);
      } else if (isIdle) {
        return enemyTextures.getEnemyPlayerTextureRegion(EnemyTextureRegistry.IDLETEXRIGHTREG);
      } else if (isNextStepAnimation()) {
        movingAnimationUntil = getAnimationTimeoutMls();
        currentStep++;
        return enemyTextures.getEnemyPlayerMoveRightTextureRegion(currentStep);
      }
    } else {
      if (isShootingAnimation()) {
        return enemyTextures.getEnemyPlayerTextureRegion(EnemyTextureRegistry.SHOOTINGTEXBACKTREG);
      } else if (isIdle) {
        return enemyTextures.getEnemyPlayerTextureRegion(EnemyTextureRegistry.IDLETEXBACKREG);
      } else if (isNextStepAnimation()) {
        movingAnimationUntil = getAnimationTimeoutMls();
        currentStep += 1;
        return enemyTextures.getEnemyPlayerMoveBackTextureRegion(currentStep);
      }
    }
    return null;
  }

  private boolean isShootingAnimation() {
    return System.currentTimeMillis() <= shootingAnimationUntil;
  }

  private boolean isNextStepAnimation() {
    return System.currentTimeMillis() >= movingAnimationUntil;
  }

  @Override
  public void destroy() {
    super.destroy();
    enemyPlayerVoiceChatEffect.destroy();
  }


  private boolean isTooClose(Vector2 vector1, Vector2 vector2) {
    return Vector2.dst(vector1.x, vector1.y, vector2.x,
        vector2.y) <= 0.225f;
  }
}
