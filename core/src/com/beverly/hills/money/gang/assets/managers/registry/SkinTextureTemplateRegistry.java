package com.beverly.hills.money.gang.assets.managers.registry;

import com.beverly.hills.money.gang.screens.ui.selection.GamePlayerClass;
import com.beverly.hills.money.gang.screens.ui.selection.SkinUISelection;
import java.util.Locale;

public class SkinTextureTemplateRegistry {

  private static final String FILE_TEMPLATE = "textures/%s/enemy_%s_sprites_%s.png";

  public static String getTextureFileNameForClass(GamePlayerClass gamePlayerClass,
      SkinUISelection skinUISelection) {
    String playerClass = gamePlayerClass.name().toLowerCase(Locale.ENGLISH);
    return String.format(FILE_TEMPLATE, playerClass, playerClass,
        skinUISelection.name().toLowerCase(Locale.ENGLISH));
  }

}
