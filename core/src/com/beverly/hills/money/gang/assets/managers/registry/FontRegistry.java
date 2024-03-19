package com.beverly.hills.money.gang.assets.managers.registry;

public enum FontRegistry {

  FONT_64("font03_64.fnt"),
  FONT_32("font03_32.fnt");

  private final String fileName;

  FontRegistry(String fileName) {
    this.fileName = "fonts/" + fileName;
  }

  public String getFileName() {
    return fileName;
  }
}
