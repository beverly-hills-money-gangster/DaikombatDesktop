package com.beverly.hills.money.gang.entities.item;

import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import com.beverly.hills.money.gang.assets.managers.registry.TexturesRegistry;
import com.beverly.hills.money.gang.proto.PushGameEventCommand.GameEventType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PowerUpType {
  QUAD_DAMAGE(TexturesRegistry.QUAD_DAMAGE_ORB, GameEventType.QUAD_DAMAGE_POWER_UP,
      SoundRegistry.QUAD_DAMAGE_PICK, SoundRegistry.ENEMY_QUAD_DAMAGE_PICK),
  DEFENCE(TexturesRegistry.DEFENCE_ORB, GameEventType.DEFENCE_POWER_UP, SoundRegistry.DEFENCE_PICK,
      SoundRegistry.ENEMY_DEFENCE_PICK),
  INVISIBILITY(TexturesRegistry.INVISIBILITY_ORB, GameEventType.INVISIBILITY_POWER_UP,
      SoundRegistry.INVISIBILITY_PICK, SoundRegistry.ENEMY_INVISIBILITY_PICK);

  @Getter
  private final TexturesRegistry texture;

  @Getter
  private final GameEventType pickType;

  @Getter
  private final SoundRegistry playerPickSound;

  @Getter
  private final SoundRegistry enemyPickSound;

}
