package com.beverly.hills.money.gang;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.beverly.hills.money.gang.assets.managers.DaiKombatAssetsManager;
import com.beverly.hills.money.gang.filters.OverlapFilterManager;
import com.beverly.hills.money.gang.maps.MapBuilder;
import com.beverly.hills.money.gang.models.ModelMaker;
import com.beverly.hills.money.gang.pref.UserPreference;
import com.beverly.hills.money.gang.rect.RectManager;
import com.beverly.hills.money.gang.screens.GameScreen;
import com.beverly.hills.money.gang.screens.menu.MainMenuScreen;
import com.beverly.hills.money.gang.screens.ui.selection.UserSettingsUISelection;
import com.beverly.hills.money.gang.utils.EntityManager;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.SystemUtils;

@Getter
public class DaiKombatGame extends Game {

  @Setter
  private boolean gameIsPaused;
  private SpriteBatch batch;
  private ModelBatch mdlBatch;
  private FrameBuffer fbo;
  private DaiKombatAssetsManager assMan;
  private EntityManager entMan;
  private RectManager rectMan;
  private OverlapFilterManager overlapFilterMan;
  private float timeSinceLaunch;

  @Override
  public void create() {
    batch = new SpriteBatch();
    mdlBatch = new ModelBatch();
    createNewMainFbo(Constants.FBO_WIDTH_ORIGINAL, Constants.FBO_HEIGHT_ORIGINAL);

    assMan = new DaiKombatAssetsManager();
    assMan.finishLoading();

    overlapFilterMan = new OverlapFilterManager();

    entMan = new EntityManager();
    rectMan = new RectManager(this);



    // restore user configs
    UserPreference userPreference = new UserPreference();
    UserSettingsUISelection.MOUSE_SENS.getState().setSetting(userPreference.getMouseSensitivity());
    UserSettingsUISelection.SOUND.getState().setSetting(userPreference.getSoundVolume());
    if (!Configs.DEV_MODE && SystemUtils.IS_OS_WINDOWS) {
      // support alt+tab for full screen
      Gdx.graphics.setUndecorated(true);
      Graphics.DisplayMode displayMode = Gdx.graphics.getDisplayMode();
      Gdx.graphics.setWindowedMode(displayMode.width, displayMode.height);
    }

    setScreen(new MainMenuScreen(this));

  }

  public void createNewMainFbo(final int width, final int height) {
    fbo = new FrameBuffer(Format.RGB888, width, height, true);
    fbo.getColorBufferTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
  }

  @Override
  public void dispose() {
    getScreen().dispose();
    batch.dispose();
    mdlBatch.dispose();
    fbo.dispose();

    assMan.dispose();
  }


  @Override
  public void render() {
    timeSinceLaunch += Gdx.graphics.getDeltaTime();

    Gdx.gl.glClearColor(1, 0, 0, 1);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    getScreen().render(Gdx.graphics.getDeltaTime());
  }

  public void setScreen(GameScreen screen) {
    Optional.ofNullable(getScreen())
        .filter(GameScreen.class::isInstance)
        .map(GameScreen.class::cast).ifPresent(GameScreen::exit);

    super.setScreen(screen);
  }
}
