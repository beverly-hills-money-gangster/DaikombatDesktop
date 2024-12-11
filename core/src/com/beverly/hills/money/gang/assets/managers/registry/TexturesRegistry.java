package com.beverly.hills.money.gang.assets.managers.registry;

import lombok.Getter;

public enum TexturesRegistry {
  MINIGUN_IDLE("minigun_idle.png"),
  MINIGUN_FIRE("minigun_fire.png"),
  QUAD_DAMAGE_ORB("quad_damage_orb.png"),
  MED_KIT("medkit.png"),
  DEFENCE_ORB("defence_orb.png"),
  INVISIBILITY_ORB("invisibility_orb.png"),
  TELEPORT_SPRITES("teleport_sprites.png"),
  GUN_IDLE("gun_idle.png"),
  GUN_SHOOT("gun_shot.png"),
  PUNCH("punch.png"),
  PUNCH_IDLE("punch_idle.png"),
  RAILGUN_IDLE("railgun_idle.png"),
  RAILGUN_SHOOTING("railgun_shooting.png"),
  LOGO("logo.png"),
  MAIN_MENU_BG("main_menu_bg.png"),
  ATLAS("atlas.png"),
  SKY("sky.png");

  @Getter
  private final String fileName;

  TexturesRegistry(String fileName) {
    this.fileName = "textures/" + fileName;
  }

}
