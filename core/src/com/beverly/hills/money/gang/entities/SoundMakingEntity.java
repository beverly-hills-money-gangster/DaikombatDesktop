package com.beverly.hills.money.gang.entities;

import com.badlogic.gdx.math.Vector2;
import com.beverly.hills.money.gang.Constants;
import com.beverly.hills.money.gang.assets.managers.sound.SoundVolumeType;
import com.beverly.hills.money.gang.entities.player.Player;
import com.beverly.hills.money.gang.rect.RectanglePlus;
import com.beverly.hills.money.gang.screens.GameScreen;
import com.beverly.hills.money.gang.screens.game.PlayScreen;

public abstract class SoundMakingEntity extends Entity {

  public SoundMakingEntity(PlayScreen screen) {
    super(screen);
  }

  protected abstract Player getPlayer();

  protected abstract RectanglePlus getRect();

  public SoundVolumeType getSFXVolume() {
    float distance = Vector2.dst2(getPlayer().getRect().x, getPlayer().getRect().y, getRect().x,
        getRect().y);
    if (distance < 5f) {
      return SoundVolumeType.HIGH_LOUD;
    } else if (distance < 10) {
      return SoundVolumeType.LOUD;
    } else if (distance < 20) {
      return SoundVolumeType.LOW_LOUD;
    } else if (distance < 35f) {
      return SoundVolumeType.HIGH_NORMAL;
    } else if (distance < 55) {
      return SoundVolumeType.NORMAL;
    } else if (distance < 75) {
      return SoundVolumeType.LOW_NORMAL;
    } else if (distance < 115) {
      return SoundVolumeType.HIGH_QUIET;
    } else if (distance < 135) {
      return SoundVolumeType.QUIET;
    } else if (distance < 170) {
      return SoundVolumeType.LOW_QUIET;
    } else {
      return SoundVolumeType.MUTE;
    }
  }


  public final float getSFXPan() {
    var camDir2D = new Vector2(getPlayer().getPlayerCam().direction.x,
        getPlayer().getPlayerCam().direction.z);
    float angle = camDir2D.angleDeg(
        getRect().getPosition(new Vector2()).cpy().sub(getPlayer().getCurrent2DPosition()));
    if (Constants.LEFT_RANGE.contains(angle)) {
      return -0.65f;
    } else if (Constants.RIGHT_RANGE.contains(angle)) {
      return 0.65f;
    }
    return 0;
  }

}
