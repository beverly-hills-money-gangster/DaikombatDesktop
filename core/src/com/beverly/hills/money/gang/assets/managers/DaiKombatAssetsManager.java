package com.beverly.hills.money.gang.assets.managers;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.BitmapFontLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.beverly.hills.money.gang.assets.managers.registry.FontRegistry;
import com.beverly.hills.money.gang.assets.managers.registry.SkinTextureTemplateRegistry;
import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import com.beverly.hills.money.gang.assets.managers.registry.TexturesRegistry;
import com.beverly.hills.money.gang.assets.managers.sound.UserSettingSound;
import com.beverly.hills.money.gang.proto.MapAssets;
import com.beverly.hills.money.gang.screens.ui.selection.GamePlayerClass;
import com.beverly.hills.money.gang.screens.ui.selection.SkinUISelection;
import java.util.Arrays;

public class DaiKombatAssetsManager {

  private final AssetManager assetManager = new AssetManager();

  private final MapLoadService mapLoadService = new MapLoadService();

  public DaiKombatAssetsManager() {
    loadTextures();
    loadSkins();
    loadSounds();
    loadFonts();
  }

  public void finishLoading() {
    assetManager.finishLoading();
  }

  public void dispose() {
    assetManager.dispose();
  }

  public TextureRegion getTextureRegion(final TexturesRegistry texturesRegistry, int x, int y,
      int width, int height) {
    return new TextureRegion((Texture) assetManager.get(texturesRegistry.getFileName()),
        x, y, width, height);
  }

  public Texture getTexture(final TexturesRegistry texturesRegistry) {
    return assetManager.get(texturesRegistry.getFileName());
  }

  public TextureRegion getTextureRegion(final Texture texture, int x, int y,
      int width, int height) {
    return new TextureRegion(texture,
        x, y, width, height);
  }

  public TextureRegion getTextureRegionFlipped(
      final Texture texture, int x, int y, int width, int height) {
    var tr = getTextureRegion(texture, x, y, width, height);
    tr.flip(true, false);
    return tr;
  }

  public Texture getMapAtlas(final String mapName, final String mapHash) {
    return mapLoadService.loadAtlas(mapName, mapHash);
  }

  public TextureRegion getTextureRegion(final String fileName, int x, int y,
      int width, int height) {
    return new TextureRegion((Texture) assetManager.get(fileName), x, y,
        width, height);
  }

  public TextureRegion getTextureRegion(final TexturesRegistry texturesRegistry) {
    return new TextureRegion((Texture) assetManager.get(texturesRegistry.getFileName()));
  }

  public BitmapFont getFont(final FontRegistry fontRegistry) {
    return assetManager.get(fontRegistry.getFileName());
  }

  public UserSettingSound getUserSettingSound(final SoundRegistry soundRegistry) {
    return new UserSettingSound(assetManager.get(soundRegistry.getFileName()));
  }

  public TiledMap getMap(String name, String hash) {
    return mapLoadService.loadMap(name, hash);
  }

  public boolean mapExists(String name, String hash) {
    return mapLoadService.exists(name, hash);
  }

  public void saveMap(String name, String hash, MapAssets mapAssets) {
    mapLoadService.saveMap(name, hash, mapAssets);
  }

  public Sound getSound(final SoundRegistry soundRegistry) {
    return assetManager.get(soundRegistry.getFileName());
  }

  public void loadFonts() {
    final FileHandleResolver resolver = new InternalFileHandleResolver();
    assetManager.setLoader(BitmapFont.class,
        new BitmapFontLoader(resolver)); // Tile atlas should be in same folder.

    Arrays.stream(FontRegistry.values())
        .forEach(fontRegistry -> assetManager.load(fontRegistry.getFileName(),
            BitmapFont.class));

  }

  public void loadSounds() {
    Arrays.stream(SoundRegistry.values())
        .forEach(
            soundRegistry -> assetManager.load(soundRegistry.getFileName(), Sound.class));
  }

  public void loadTextures() {
    Arrays.stream(TexturesRegistry.values()).forEach(texturesRegistry
        -> assetManager.load(texturesRegistry.getFileName(), Texture.class));
  }

  public void loadSkins() {
    Arrays.stream(GamePlayerClass.values()).forEach(
        playerClassUISelection -> Arrays.stream(SkinUISelection.values()).forEach(skinUISelection
            -> assetManager.load(
            SkinTextureTemplateRegistry
                .getTextureFileNameForClass(playerClassUISelection, skinUISelection),
            Texture.class)));
  }
}
