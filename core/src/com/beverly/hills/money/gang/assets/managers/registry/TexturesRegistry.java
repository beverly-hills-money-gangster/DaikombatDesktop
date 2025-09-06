package com.beverly.hills.money.gang.assets.managers.registry;

import lombok.Getter;

public enum TexturesRegistry {

  BARREL("decorations/barrel.png"),
  COLUMN_CANDLE("decorations/column_candle.png"),
  COLUMN_BIG_CANDLE("decorations/column_big_candle.png"),
  CANDLES("decorations/candles.png"),

  FIRE_CANDLES("decorations/fire_candles.png"),
  FIRE_CANDLES_GREEN("decorations/fire_candles_green.png"),
  COLUMN("decorations/column.png"),
  COLUMN_MINI("decorations/column_mini.png"),
  COLUMN_BLEEDING("decorations/column_bleeding.png"),
  DEAD_GUY("decorations/dead_guy.png"),
  SKULL_COLUMN("decorations/skull_column.png"),
  SKULL_PILE("decorations/skull_pile.png"),
  SKULLS("decorations/skulls.png"),
  OVERLAY("overlay.png"),
  MINIGUN_IDLE("guns/minigun_idle.png"),
  DEMON_MINIGUN_IDLE("guns/demon_minigun_idle.png"),
  DEMON_MINIGUN_FIRE("guns/demon_minigun_fire.png"),
  MINIGUN_FIRE("guns/minigun_fire.png"),
  QUAD_DAMAGE_ORB("powerup/quad_damage_orb.png"),
  BEAST_ORB("powerup/beast_orb.png"),
  MED_KIT("powerup/medkit.png"),
  MEDIUM_AMMO("powerup/medium_ammo.png"),
  BIG_AMMO("powerup/big_ammo.png"),
  DEFENCE_ORB("powerup/defence_orb.png"),
  INVISIBILITY_ORB("powerup/invisibility_orb.png"),
  TELEPORT_SPRITES("teleport_sprites.png"),
  BOOM_SPRITES("guns/boom_sprites.png"),
  PLASMA_BOOM_SPRITES("guns/plasma_sprites.png"),
  GUN_IDLE("guns/gun_idle.png"),
  GUN_SHOOT("guns/gun_shot.png"),
  PLASMAGUN_IDLE("guns/plasmagun_idle.png"),
  PLASMAGUN_FIRE("guns/plasmagun_fire.png"),
  SKELETON_PLASMAGUN_IDLE("guns/skeleton_plasmagun_idle.png"),
  SKELETON_PLASMAGUN_FIRE("guns/skeleton_plasmagun_fire.png"),
  PUNCH("guns/punch.png"),
  DEMON_PUNCH("guns/demon_punch.png"),
  PUNCH_IDLE("guns/punch_idle.png"),
  DEMON_PUNCH_IDLE("guns/demon_punch_idle.png"),
  SKELETON_PUNCH("guns/skeleton_punch.png"),
  SKELETON_PUNCH_IDLE("guns/skeleton_punch_idle.png"),
  RAILGUN_IDLE("guns/railgun_idle.png"),
  RAILGUN_SHOOTING("guns/railgun_shooting.png"),

  SKELETON_RAILGUN_IDLE("guns/skeleton_railgun_idle.png"),
  SKELETON_RAILGUN_SHOOTING("guns/skeleton_railgun_shooting.png"),
  ROCKET_LAUNCHER_IDLE("guns/rocket_launcher_idle.png"),
  DEMON_ROCKET_LAUNCHER_IDLE("guns/demon_rocket_launcher_idle.png"),
  LOGO("logo.png"),
  MIC("mic.png"),
  MAIN_MENU_BG("main_menu_bg.png"),

  FIREBALL("guns/fireball.png"),
  PLASMA("guns/plasma.png"),
  MESSAGE("message.png"),
  VOICE("voice.png"),
  SKY("sky.png");

  @Getter
  private final String fileName;

  TexturesRegistry(String fileName) {
    this.fileName = "textures/" + fileName;
  }

}
