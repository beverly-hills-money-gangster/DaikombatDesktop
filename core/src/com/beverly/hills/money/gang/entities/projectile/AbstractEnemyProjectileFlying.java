package com.beverly.hills.money.gang.entities.projectile;

import static com.beverly.hills.money.gang.configs.Constants.PROJECTILE_SPEED;

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
import com.beverly.hills.money.gang.configs.Constants;
import com.beverly.hills.money.gang.entities.enemies.EnemyPlayer;
import com.beverly.hills.money.gang.entities.player.Player;
import com.beverly.hills.money.gang.models.ModelInstanceBB;
import com.beverly.hills.money.gang.rect.RectanglePlus;
import com.beverly.hills.money.gang.rect.filters.RectanglePlusFilter;
import com.beverly.hills.money.gang.screens.ui.weapon.WeaponProjectile;
import lombok.Getter;
import lombok.Setter;

public class AbstractEnemyProjectileFlying extends Projectile {


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

  private static final int LIVES_FOR_MLS = 350;

  private final long destroyAtMls = System.currentTimeMillis() + LIVES_FOR_MLS;

  public AbstractEnemyProjectileFlying(final Vector3 startPosition,
      final Vector2 finishPosition,
      final Player player,
      final TextureRegion flyingProjectileTextureRegion) {
    super(player.getScreen());
    this.player = player;
    this.finishPosition = finishPosition;
    this.position = startPosition;

    mdlInst = new ModelInstanceBB(player.getScreen().getCellBuilder().getMdlEnemy());
    flyingProjectileTextureRegion.flip(true, false);
    mdlInst.materials.get(0).set(TextureAttribute.createDiffuse(flyingProjectileTextureRegion));
    mdlInst.materials.get(0).set(new ColorAttribute(ColorAttribute.Diffuse, Color.WHITE));

    mdlInst.materials.get(0)
        .set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
    mdlInst.materials.get(0).set(new FloatAttribute(FloatAttribute.AlphaTest));

    rect = new RectanglePlus(this.position.x - Constants.HALF_UNIT / 2,
        this.position.z - Constants.HALF_UNIT / 2, Constants.HALF_UNIT,
        Constants.HALF_UNIT, getEntityId(),
        RectanglePlusFilter.PROJECTILE);
    player.getScreen().getGame().getRectMan().addRect(rect);

    rect.getOldPosition().set(rect.x, rect.y);
    rect.getNewPosition().set(rect.x, rect.y);

  }


  @Override
  public void destroy() {
    if (rect != null) {
      getScreen().getGame().getRectMan().removeRect(rect);
    }
    super.destroy(); // should be last.
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

    getPosition().set(getRect().moveToDirection(finishPosition, delta, PROJECTILE_SPEED));
    if (getRect().isTooClose(finishPosition)) {
      destroy();
      return;
    }
    for (final RectanglePlus otherRect : getScreen().getGame().getRectMan().getRects()) {
      if (otherRect != rect && rect.overlaps(otherRect) && (
          otherRect.getFilter() == RectanglePlusFilter.WALL
              || otherRect.getFilter() == RectanglePlusFilter.ENEMY)) {

        if (otherRect.getFilter() == RectanglePlusFilter.ENEMY) {
          var enemyPlayer = (EnemyPlayer) getScreen().getGame().getEntMan()
              .getEntityFromId(otherRect.getConnectedEntityId());
          if (!enemyPlayer.isVisible()) {
            continue;
          }
        }
        destroy();
        break;
      }
    }
  }


  @Override
  public WeaponProjectile getProjectileType() {
    return WeaponProjectile.ROCKET;
  }

  @Override
  public Vector2 currentPosition() {
    return getRect().getCenter();
  }

  @Override
  protected Player getPlayer() {
    return player;
  }
}
