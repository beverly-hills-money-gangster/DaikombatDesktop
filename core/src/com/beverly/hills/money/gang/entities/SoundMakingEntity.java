package com.beverly.hills.money.gang.entities;

import com.badlogic.gdx.math.Vector2;
import com.beverly.hills.money.gang.Constants;
import com.beverly.hills.money.gang.assets.managers.sound.SoundVolumeType;
import com.beverly.hills.money.gang.entities.player.Player;
import com.beverly.hills.money.gang.rect.RectanglePlus;
import com.beverly.hills.money.gang.screens.GameScreen;

public abstract class SoundMakingEntity extends Entity {

  public SoundMakingEntity(GameScreen screen) {
    super(screen);
  }

  protected abstract Player getPlayer();

  protected abstract RectanglePlus getRect();


  public SoundVolumeType getSFXVolume() {
    float distance = Vector2.dst2(getPlayer().getRect().x, getPlayer().getRect().y, getRect().x,
        getRect().y);
    if (distance < 3f) {
      return SoundVolumeType.VERY_LOUD;
    } else if (distance < 20) {
      return SoundVolumeType.LOUD;
    } else if (distance < 50) {
      return SoundVolumeType.NORMAL;
    } else if (distance < 100) {
      return SoundVolumeType.MEDIUM;
    } else if (distance < 250) {
      return SoundVolumeType.QUITE;
    } else {
      return SoundVolumeType.MUTE;
    }
  }


  public final float getSFXPan() {
    var camDir2D = new Vector2(getPlayer().getPlayerCam().direction.x,
        getPlayer().getPlayerCam().direction.z);
    float angle = camDir2D.angleDeg(
        getRect().getNewPosition().cpy().sub(getPlayer().getCurrent2DPosition()));
    if (Constants.LEFT_RANGE.contains(angle)) {
      return -0.65f;
    } else if (Constants.RIGHT_RANGE.contains(angle)) {
      return 0.65f;
    }
    return 0;
  }

}
