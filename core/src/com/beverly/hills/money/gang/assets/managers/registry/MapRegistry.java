package com.beverly.hills.money.gang.assets.managers.registry;

public enum MapRegistry {
  ONLINE_MAP("online_map.tmx");

  private final String fileName;

  MapRegistry(String fileName) {
    this.fileName = "maps/" + fileName;
  }

  public String getFileName() {
    return fileName;
  }
}
