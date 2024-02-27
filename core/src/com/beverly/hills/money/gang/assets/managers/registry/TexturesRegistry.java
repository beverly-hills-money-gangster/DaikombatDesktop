package com.beverly.hills.money.gang.assets.managers.registry;

import lombok.Getter;

public enum TexturesRegistry {
    ENEMY_PLAYER_SPRITES_GREEN("enemy_player_sprites_green.png"),
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
