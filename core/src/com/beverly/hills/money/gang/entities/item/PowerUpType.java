package com.beverly.hills.money.gang.entities.item;

import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import com.beverly.hills.money.gang.assets.managers.registry.TexturesRegistry;
import com.beverly.hills.money.gang.proto.GamePowerUpType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PowerUpType {
  QUAD_DAMAGE(TexturesRegistry.QUAD_DAMAGE_ORB, GamePowerUpType.QUAD_DAMAGE,
      SoundRegistry.QUAD_DAMAGE_PICK, SoundRegistry.ENEMY_QUAD_DAMAGE_PICK),

  BEAST(TexturesRegistry.BEAST_ORB, GamePowerUpType.BEAST, SoundRegistry.BEAST_PICK,
      SoundRegistry.ENEMY_BEAST_PICK),
  DEFENCE(TexturesRegistry.DEFENCE_ORB, GamePowerUpType.DEFENCE, SoundRegistry.DEFENCE_PICK,
      SoundRegistry.ENEMY_DEFENCE_PICK),

  HEALTH(TexturesRegistry.MED_KIT, GamePowerUpType.HEALTH, SoundRegistry.REGENERATION_PICK,
      SoundRegistry.ENEMY_REGENERATION_PICK),

  INVISIBILITY(TexturesRegistry.INVISIBILITY_ORB, GamePowerUpType.INVISIBILITY,
      SoundRegistry.INVISIBILITY_PICK, SoundRegistry.ENEMY_INVISIBILITY_PICK),

  MEDIUM_AMMO(TexturesRegistry.MEDIUM_AMMO, GamePowerUpType.MEDIUM_AMMO,
      SoundRegistry.AMMO_PICK, SoundRegistry.ENEMY_AMMO_PICK),
  BIG_AMMO(TexturesRegistry.BIG_AMMO, GamePowerUpType.BIG_AMMO,
      SoundRegistry.AMMO_PICK, SoundRegistry.ENEMY_AMMO_PICK);

  @Getter
  private final TexturesRegistry texture;

  @Getter
  private final GamePowerUpType pickType;

  @Getter
  private final SoundRegistry playerPickSound;

  @Getter
  private final SoundRegistry enemyPickSound;

  // TODO test it
  public String getCanonicalName() {
    return this.name().replace("_", " ");
  }

}
