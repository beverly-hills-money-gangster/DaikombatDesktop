package com.beverly.hills.money.gang.assets.managers.registry;

import lombok.Getter;

public enum TexturesRegistry {
  QUAD_DAMAGE_ORB("quad_damage_orb.png"),
  DEFENCE_ORB("defence_orb.png"),
  INVISIBILITY_ORB("invisibility_orb.png"),
  ENEMY_PLAYER_SPRITES_GREEN("enemy_player_sprites_green.png"),
  ENEMY_PLAYER_SPRITES_BLUE("enemy_player_sprites_blue.png"),
  ENEMY_PLAYER_SPRITES_ORANGE("enemy_player_sprites_orange.png"),
  ENEMY_PLAYER_SPRITES_PURPLE("enemy_player_sprites_purple.png"),
  ENEMY_PLAYER_SPRITES_YELLOW("enemy_player_sprites_yellow.png"),
  ENEMY_PLAYER_SPRITES_PINK("enemy_player_sprites_pink.png"),
  GUN_IDLE("gun_idle.png"),
  GUN_SHOOT("gun_shot.png"),
  PUNCH("punch.png"),
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
