package com.beverly.hills.money.gang.entities.effect;

import com.badlogic.gdx.math.Vector3;
import com.beverly.hills.money.gang.assets.managers.registry.TexturesRegistry;
import com.beverly.hills.money.gang.screens.GameScreen;

public class EnemyPlayerVoiceChatEffect extends AbstractEnemyPlayerTalkingEffect {


  public EnemyPlayerVoiceChatEffect(Vector3 position, GameScreen screen) {
    super(position, screen, TexturesRegistry.VOICE, 150);
  }
}