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
import com.beverly.hills.money.gang.animation.Animation;
import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import com.beverly.hills.money.gang.assets.managers.sound.TimeLimitedSound;
import com.beverly.hills.money.gang.assets.managers.sound.TimeLimitedSound.TimeLimitSoundConf;
import com.beverly.hills.money.gang.configs.Constants;
import com.beverly.hills.money.gang.configs.EnvConfigs;
import com.beverly.hills.money.gang.entities.enemies.EnemyPlayer;
import com.beverly.hills.money.gang.entities.player.Player;
import com.beverly.hills.money.gang.entities.player.Player.ProjectileEnemy;
import com.beverly.hills.money.gang.models.ModelInstanceBB;
import com.beverly.hills.money.gang.rect.RectanglePlus;
import com.beverly.hills.money.gang.rect.filters.RectanglePlusFilter;
import com.beverly.hills.money.gang.screens.game.PlayScreen;
import com.beverly.hills.money.gang.screens.ui.weapon.WeaponProjectile;
import com.beverly.hills.money.gang.screens.ui.weapon.WeaponState;
import java.util.Set;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.Setter;

public class AbstractPlayerProjectile extends Projectile {

  private static final Set<RectanglePlusFilter> ENEMY_COLLISION_FILTERS
      = Set.of(RectanglePlusFilter.ENEMY, RectanglePlusFilter.WALL);

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

  private static final int VISIBLE_AFTER_MLS = 45;
  private final long createdAtMls = System.currentTimeMillis();

  private boolean stopMoving;

  private final WeaponProjectile projectileType;

  private final SoundRegistry boomSound;


  public AbstractPlayerProjectile(final Player player, final Vector3 startPosition,
      final Vector2 finishPosition,
      final PlayScreen screen,
      final TextureRegion flyingProjectileTextureRegion,
      final Animation blowUpAnimation,
      final WeaponState weaponState,
      final WeaponProjectile projectileType,
      final SoundRegistry boomSound) {
    super(screen);
    this.boomSound = boomSound;
    this.projectileType = projectileType;
    this.onBlowUp = projectile -> {
      var enemiesInRange = player.getEnemiesRegistry()
          .getVisibleEnemiesInRange(projectile.currentPosition(), // TODO is that correct position?
              weaponState.getProjectileRadius());
      player.getOnProjectileAttackHit().accept(
          ProjectileEnemy.builder().enemyPlayers(enemiesInRange).projectile(projectile)
              .player(player)
              .build());
    };
    this.finishPosition = finishPosition;
    this.position = startPosition.cpy();
    this.player = player;

    mdlInst = new ModelInstanceBB(screen.getCellBuilder().getMdlEnemy());
    flyingProjectileTextureRegion.flip(true, false);
    mdlInst.materials.get(0).set(TextureAttribute.createDiffuse(flyingProjectileTextureRegion));
    mdlInst.materials.get(0).set(new ColorAttribute(ColorAttribute.Diffuse, Color.WHITE));

    mdlInst.materials.get(0)
        .set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
    mdlInst.materials.get(0).set(new FloatAttribute(FloatAttribute.AlphaTest));

    final float rectWidth = Constants.HALF_UNIT;
    final float rectHeight = Constants.HALF_UNIT;
    rect = new RectanglePlus(
        this.position.x - rectWidth / 2,
        this.position.z - rectHeight / 2,
        rectWidth, rectHeight, getEntityId(),
        RectanglePlusFilter.PROJECTILE);
    screen.getGame().getRectMan().addRect(rect);

    rect.getOldPosition().set(rect.x, rect.y);
    rect.getNewPosition().set(rect.x, rect.y);
    boomAnimation = blowUpAnimation;

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
    final ColorAttribute colorAttribute = (ColorAttribute) getMdlInst().materials.get(0)
        .get(ColorAttribute.Diffuse);
    if (!stopMoving) {
      if (System.currentTimeMillis() < createdAtMls + VISIBLE_AFTER_MLS) {
        colorAttribute.color.set(new Color(1, 1, 1, 0));
      } else {
        colorAttribute.color.set(new Color(1, 1, 1, 1));
      }

      getPosition().set(getRect().moveToDirection(finishPosition, delta, PROJECTILE_SPEED));
      if (getRect().isTooClose(finishPosition)) {
        onBlowUp.accept(this);
        boom();
        return;
      }
      for (final RectanglePlus otherRect : getScreen().getGame().getRectMan().getRects()) {
        if (otherRect != rect && ENEMY_COLLISION_FILTERS.contains(otherRect.getFilter())
            && rect.overlaps(otherRect)) {
          if (otherRect.getFilter() == RectanglePlusFilter.ENEMY) {
            var enemyPlayer = (EnemyPlayer) getScreen().getGame().getEntMan()
                .getEntityFromId(otherRect.getConnectedEntityId());
            if (!enemyPlayer.isVisible()) {
              continue;
            }
          } else if (otherRect.getFilter() == RectanglePlusFilter.WALL
              && EnvConfigs.SHOOT_THRU_WALLS) {
            continue;
          }
          onBlowUp.accept(this);
          boom();
          break;
        }
      }
    } else {
      colorAttribute.color.set(new Color(1, 1, 1, 1));
      mdlInst.materials.get(0).set(TextureAttribute.createDiffuse(
          boomAnimation.getCurrentTextureRegion(getScreen().getGame().getAssMan())));
    }

  }

  private void boom() {
    stopMoving = true;
    destroyAtMls = System.currentTimeMillis() + 200;
    new TimeLimitedSound(
        getScreen().getGame().getAssMan().getUserSettingSound(boomSound)).play(
        TimeLimitSoundConf.builder()
            .soundVolumeType(getSFXVolume()).pan(getSFXPan()).frequencyMls(250)
            .build());
  }


  @Override
  public WeaponProjectile getProjectileType() {
    return projectileType;
  }

  @Override
  public Vector2 currentPosition() {
    return getRect().getCenter();
  }
}
