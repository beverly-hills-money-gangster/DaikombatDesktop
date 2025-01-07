package com.beverly.hills.money.gang.entities.projectile;

import static com.beverly.hills.money.gang.Constants.ROCKET_SPEED;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.beverly.hills.money.gang.Constants;
import com.beverly.hills.money.gang.animation.Animation;
import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import com.beverly.hills.money.gang.assets.managers.registry.TexturesRegistry;
import com.beverly.hills.money.gang.assets.managers.sound.TimeLimitedSound;
import com.beverly.hills.money.gang.entities.player.Player;
import com.beverly.hills.money.gang.models.ModelInstanceBB;
import com.beverly.hills.money.gang.rect.RectanglePlus;
import com.beverly.hills.money.gang.rect.filters.RectanglePlusFilter;
import com.beverly.hills.money.gang.screens.GameScreen;
import com.beverly.hills.money.gang.screens.ui.weapon.WeaponProjectile;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.Setter;

public class RocketProjectile extends Projectile {

  @Getter
  private final Vector3 position;
  @Setter
  @Getter
  private ModelInstanceBB mdlInst;

  @Setter
  @Getter
  private RectanglePlus rect;

  private final Vector2 finishPosition;

  private final Player player;

  private final Consumer<Projectile> onBlowUp;

  private final Animation boomAnimation;

  private long destroyAtMls = Long.MAX_VALUE;

  private boolean stopMoving;


  public RocketProjectile(final Player player, final Vector3 startPosition,
      final Vector2 finishPosition,
      final GameScreen screen,
      final Consumer<Projectile> onBlowUp) {
    super(screen);
    this.onBlowUp = onBlowUp;
    this.finishPosition = finishPosition;
    this.position = startPosition.cpy();
    this.player = player;
    this.position.add(Constants.HALF_UNIT, 0, Constants.HALF_UNIT);

    mdlInst = new ModelInstanceBB(screen.getGame().getCellBuilder().getMdlEnemy());
    TextureRegion currentTexReg = screen.getGame().getAssMan()
        .getTextureRegion(TexturesRegistry.FIREBALL, 0, 0, 11, 11);
    currentTexReg.flip(true, false);
    mdlInst.materials.get(0).set(TextureAttribute.createDiffuse(currentTexReg));
    mdlInst.materials.get(0).set(new ColorAttribute(ColorAttribute.Diffuse, Color.WHITE));

    mdlInst.materials.get(0)
        .set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
    mdlInst.materials.get(0).set(new FloatAttribute(FloatAttribute.AlphaTest));

    final float rectWidth = Constants.HALF_UNIT;
    final float rectHeight = Constants.HALF_UNIT;
    rect = new RectanglePlus(this.position.x, this.position.z, rectWidth, rectHeight, getEntityId(),
        RectanglePlusFilter.PROJECTILE);
    rect.setPosition(this.position.x - rect.getWidth() / 2, this.position.z - rect.getHeight() / 2);
    screen.getGame().getRectMan().addRect(rect);

    rect.getOldPosition().set(rect.x, rect.y);
    rect.getNewPosition().set(rect.x, rect.y);
    boomAnimation = Animation.builder()
        .animationSteps(5).animationStepMls(50)
        .width(100).height(99).texturesRegistry(TexturesRegistry.BOOM_SPRITES).build();

  }


  @Override
  public void destroy() {
    if (rect != null) {
      getScreen().getGame().getRectMan().removeRect(rect);
    }
    super.destroy(); // should be last.
  }

  @Override
  protected Player getPlayer() {
    return player;
  }

  @Override
  public void render3D(final ModelBatch mdlBatch, final Environment env, final float delta) {
    mdlInst.transform.setToLookAt(
        getScreen().getCurrentCam().direction.cpy().rotate(Vector3.Z, 180f),
        Vector3.Y);
    mdlInst.transform.setTranslation(position.cpy().add(0, Constants.HALF_UNIT, 0));
    mdlInst.setInFrustum(getScreen().frustumCull(getScreen().getCurrentCam(), mdlInst));
    if (mdlInst.isInFrustum()) {
      mdlBatch.render(mdlInst, env);
    }
  }


  @Override
  public void tick(final float delta) {
    if (System.currentTimeMillis() > destroyAtMls) {
      destroy();
      return;
    }
    if (!stopMoving) {
      Vector2 rectDirection = new Vector2();
      rectDirection.x = finishPosition.x - getRect().x;
      rectDirection.y = finishPosition.y - getRect().y;
      rectDirection.nor().scl(ROCKET_SPEED * delta);
      getRect().getNewPosition().add(rectDirection.x, rectDirection.y);
      getRect().setX(getRect().getNewPosition().x);
      getRect().setY(getRect().getNewPosition().y);
      getPosition().set(getRect().x + getRect().getWidth() / 2, 0,
          getRect().y + getRect().getHeight() / 2);
      getRect().getOldPosition().set(getRect().x, getRect().y);

      if (isTooClose(getRect().getOldPosition(), finishPosition)) {
        onBlowUp.accept(this);
        boom();
        return;
      }
      for (final RectanglePlus otherRect : getScreen().getGame().getRectMan().getRects()) {
        if (otherRect != rect && rect.overlaps(otherRect) && (
            otherRect.getFilter() == RectanglePlusFilter.WALL
                || otherRect.getFilter() == RectanglePlusFilter.ENEMY)) {
          onBlowUp.accept(this);
          boom();
          break;
        }
      }
    } else {
      mdlInst.materials.get(0).set(TextureAttribute.createDiffuse(
          boomAnimation.getCurrentTextureRegion(getScreen().getGame().getAssMan())));
    }

  }

  private void boom() {
    stopMoving = true;
    destroyAtMls = System.currentTimeMillis() + 200;
    new TimeLimitedSound(
        getScreen().getGame().getAssMan().getUserSettingSound(SoundRegistry.ROCKET_BOOM)).play(
        getSFXVolume(), getSFXPan(), 500);
  }


  private boolean isTooClose(Vector2 vector1, Vector2 vector2) {
    return Vector2.dst(vector1.x, vector1.y, vector2.x,
        vector2.y) <= 0.1f;
  }

  @Override
  public WeaponProjectile getProjectileType() {
    return WeaponProjectile.ROCKET;
  }

  @Override
  public Vector2 currentPosition() {
    return new Vector2(getRect().x, getRect().y);
  }
}
