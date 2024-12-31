package com.beverly.hills.money.gang.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.beverly.hills.money.gang.Constants;
import com.beverly.hills.money.gang.DaiKombatGame;
import com.beverly.hills.money.gang.assets.managers.registry.FontRegistry;
import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import com.beverly.hills.money.gang.assets.managers.registry.TexturesRegistry;
import com.beverly.hills.money.gang.assets.managers.sound.LoopingUserSettingSound;
import com.beverly.hills.money.gang.config.ClientConfig;
import java.util.Locale;

public abstract class AbstractMainMenuScreen extends GameScreen {

  private final String networkClientVersion = ("Network client ver: "
      + ClientConfig.VERSION).toUpperCase(Locale.ENGLISH);

  private final GlyphLayout glyphLayoutNetworkClient;
  private final BitmapFont guiFont32;

  protected boolean showLogo = true;

  private static LoopingUserSettingSound MUSIC_BACKGROUND;
  private final TextureRegion skyBg;
  private final TextureRegion guiTitle;

  private final float logoHeight;

  private final float logoWidht;

  protected AbstractMainMenuScreen(final DaiKombatGame game) {
    super(game, new StretchViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
    Environment env = new Environment();
    env.set(new ColorAttribute(ColorAttribute.AmbientLight, 1, 1, 1, 1f));
    env.set(new ColorAttribute(ColorAttribute.Fog, Constants.FOG_COLOR));

    skyBg = getGame().getAssMan().getTextureRegion(TexturesRegistry.MAIN_MENU_BG);
    guiTitle = getGame().getAssMan().getTextureRegion(TexturesRegistry.LOGO);
    guiFont32 = game.getAssMan().getFont(FontRegistry.FONT_32);
    glyphLayoutNetworkClient = new GlyphLayout(guiFont32, networkClientVersion);

    startBgMusic();

    Gdx.input.setCursorCatched(true);

    float logoResizeCoefficient = (getViewport().getWorldHeight() / 2) / guiTitle.getRegionHeight();

    logoWidht = guiTitle.getRegionWidth() * logoResizeCoefficient;
    logoHeight = guiTitle.getRegionHeight() * logoResizeCoefficient;
  }

  private void startBgMusic() {
    if (MUSIC_BACKGROUND == null) {
      MUSIC_BACKGROUND = new LoopingUserSettingSound(
          getGame().getAssMan().getSound(SoundRegistry.MAIN_MENU));
      MUSIC_BACKGROUND.loop(Constants.DEFAULT_MUSIC_VOLUME);
    }
  }

  protected void refreshBgMusicVolume() {
    if (MUSIC_BACKGROUND != null) {
      MUSIC_BACKGROUND.refreshVolume();
    }
  }

  protected void stopBgMusic() {
    MUSIC_BACKGROUND.stop();
    MUSIC_BACKGROUND = null;
  }


  @Override
  public void render(final float delta) {
    super.render(delta);
    getGame().getFbo().begin();
    getGame().getBatch().setColor(1, 1, 1, 1);
    Gdx.gl.glClearColor(Constants.FOG_COLOR.r, Constants.FOG_COLOR.g, Constants.FOG_COLOR.b,
        Constants.FOG_COLOR.a);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
    getGame().getBatch().begin();
    getGame().getBatch()
        .draw(skyBg, 0, 0, getViewport().getWorldWidth(),
            getViewport().getWorldHeight());
    getGame().getBatch().end();
    getGame().getFbo().end();

    getGame().getBatch().begin();
    getGame().getBatch()
        .draw(getGame().getFbo().getColorBufferTexture(), 0, 0, getViewport().getWorldWidth(),
            getViewport().getWorldHeight());
    guiFont32.draw(getGame().getBatch(), networkClientVersion,
        getViewport().getWorldWidth() / 2f - glyphLayoutNetworkClient.width / 2f,
        getViewport().getWorldHeight() - Constants.MENU_OPTION_INDENT);
    getGame().getBatch().end();

    if (showLogo) {
      getGame().getBatch().begin();
      getGame().getBatch().draw(guiTitle,
          getViewport().getWorldWidth() / 2f - logoWidht / 2f,
          getViewport().getWorldHeight() / 2f - logoHeight / 2f + (float) (
              Math.sin(getGame().getTimeSinceLaunch()) * 15f), logoWidht, logoHeight);
      getGame().getBatch().end();
    }
  }

}
