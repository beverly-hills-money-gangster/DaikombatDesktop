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
  BOOM_SPRITES("boom_sprites.png"),
  PLASMA_BOOM_SPRITES("plasma_sprites.png"),
  GUN_IDLE("gun_idle.png"),
  GUN_SHOOT("gun_shot.png"),
  PLASMAGUN_IDLE("plasmagun_idle.png"),
  PLASMAGUN_FIRE("plasmagun_fire.png"),
  PUNCH("punch.png"),
  PUNCH_IDLE("punch_idle.png"),
  RAILGUN_IDLE("railgun_idle.png"),
  RAILGUN_SHOOTING("railgun_shooting.png"),
  ROCKET_LAUNCHER_IDLE("rocket_launcher_idle.png"),
  ROCKET_LAUNCHER_SHOOTING("rocket_launcher_fire.png"),
  LOGO("logo.png"),
  MIC("mic.png"),
  MAIN_MENU_BG("main_menu_bg.png"),
  ATLAS("atlas.png"),

  FIREBALL("fireball.png"),
  PLASMA("plasma.png"),
  MESSAGE("message.png"),
  VOICE("voice.png"),
  SKY("sky.png");

  @Getter
  private final String fileName;

  TexturesRegistry(String fileName) {
    this.fileName = "textures/" + fileName;
  }

}
