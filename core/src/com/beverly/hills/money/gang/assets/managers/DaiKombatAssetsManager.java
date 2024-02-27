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
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.beverly.hills.money.gang.assets.managers.registry.FontRegistry;
import com.beverly.hills.money.gang.assets.managers.registry.MapRegistry;
import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import com.beverly.hills.money.gang.assets.managers.registry.TexturesRegistry;
import com.beverly.hills.money.gang.assets.managers.sound.UserSettingSound;

import java.util.Arrays;

public class DaiKombatAssetsManager {

    private final AssetManager assetManager = new AssetManager();


    public DaiKombatAssetsManager() {
        loadTextures();
        loadSounds();
        loadFonts();
        loadMaps();
    }

    public void finishLoading() {
        assetManager.finishLoading();
    }

    public void dispose() {
        assetManager.dispose();
    }

    public TextureRegion getTextureRegion(final TexturesRegistry texturesRegistry, int x, int y, int width, int height) {
        return new TextureRegion((Texture) assetManager.get(texturesRegistry.getFileName()), x, y, width, height);
    }

    public TextureRegion getTextureRegion(final TexturesRegistry texturesRegistry) {
        return new TextureRegion((Texture) assetManager.get(texturesRegistry.getFileName()));
    }

    public BitmapFont getFont(final FontRegistry fontRegistry) {
        return assetManager.get(fontRegistry.getFileName());
    }

    public TiledMap getMap(final MapRegistry mapRegistry) {
        return assetManager.get(mapRegistry.getFileName());
    }

    public UserSettingSound getUserSettingSound(final SoundRegistry soundRegistry) {
        return new UserSettingSound(assetManager.get(soundRegistry.getFileName()));
    }

    public Sound getSound(final SoundRegistry soundRegistry) {
        return assetManager.get(soundRegistry.getFileName());
    }

    public void loadFonts() {
        final FileHandleResolver resolver = new InternalFileHandleResolver();
        assetManager.setLoader(BitmapFont.class, new BitmapFontLoader(resolver)); // Tile atlas should be in same folder.

        Arrays.stream(FontRegistry.values())
                .forEach(fontRegistry -> assetManager.load(fontRegistry.getFileName(), BitmapFont.class));

    }

    public void loadMaps() {
        assetManager.setLoader(TiledMap.class, new TmxMapLoader()); // Tile atlas should be in same folder.
        Arrays.stream(MapRegistry.values())
                .forEach(mapRegistry -> assetManager.load(mapRegistry.getFileName(), TiledMap.class));
    }

    public void loadSounds() {
        Arrays.stream(SoundRegistry.values())
                .forEach(soundRegistry -> assetManager.load(soundRegistry.getFileName(), Sound.class));
    }

    public void loadTextures() {
        Arrays.stream(TexturesRegistry.values()).forEach(texturesRegistry
                -> assetManager.load(texturesRegistry.getFileName(), Texture.class));
    }
}
