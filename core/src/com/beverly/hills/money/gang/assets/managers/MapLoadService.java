package com.beverly.hills.money.gang.assets.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.LocalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.beverly.hills.money.gang.proto.MapAssets;
import lombok.Getter;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapLoadService {

  private static final Logger LOG = LoggerFactory.getLogger(MapLoadService.class);

  private final AssetManager localAssetManager = new AssetManager(new LocalFileHandleResolver());

  public MapLoadService() {
    localAssetManager.setLoader(TiledMap.class, new TmxMapLoader(new LocalFileHandleResolver()));
  }

  public boolean exists(String name, String hash) {
    return new FileHandles(name, hash).exists();
  }

  public void saveMap(
      @NonNull final String name,
      @NonNull final String hash,
      @NonNull final MapAssets mapAssets) {
    new FileHandles(name, hash).save(mapAssets);
  }

  public TiledMap loadMap(
      @NonNull final String name,
      @NonNull final String hash) {
    String path = new FileHandles(name, hash).onlineMapTMX.path();
    localAssetManager.load(path, TiledMap.class);
    localAssetManager.finishLoading();
    return localAssetManager.get(path, TiledMap.class);
  }

  public Texture loadAtlas(
      @NonNull final String name,
      @NonNull final String hash) {
    String path = new FileHandles(name, hash).atlasPNG.path();
    localAssetManager.load(path, Texture.class);
    localAssetManager.finishLoading();
    return localAssetManager.get(path, Texture.class);
  }

  @Getter
  private static class FileHandles {

    private final FileHandle onlineMapTMX;
    private final FileHandle atlasPNG;
    private final FileHandle atlasTSX;

    public FileHandles(
        @NonNull final String name,
        @NonNull final String hash) {

      onlineMapTMX = Gdx.files.local(
          String.format("maps/%s/%s/online_map.tmx", name, hash));
      atlasPNG = Gdx.files.local(String.format("maps/%s/%s/atlas.png", name, hash));
      atlasTSX = Gdx.files.local(String.format("maps/%s/%s/atlas.tsx", name, hash));
      LOG.info("Create files {}, {}, {}",
          onlineMapTMX.file().getAbsolutePath(),
          atlasPNG.file().getAbsolutePath(),
          atlasTSX.file().getAbsolutePath());
    }

    public boolean exists() {
      return onlineMapTMX.exists() && atlasPNG.exists() && atlasTSX.exists();
    }

    public void save(@NonNull final MapAssets mapAssets) {
      onlineMapTMX.writeBytes(mapAssets.getOnlineMapTmx().toByteArray(), false);
      atlasPNG.writeBytes(mapAssets.getAtlasPng().toByteArray(), false);
      atlasTSX.writeBytes(mapAssets.getAtlasTsx().toByteArray(), false);
    }
  }

}
