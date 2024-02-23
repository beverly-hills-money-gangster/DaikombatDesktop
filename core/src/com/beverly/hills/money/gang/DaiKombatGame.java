package com.beverly.hills.money.gang;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.beverly.hills.money.gang.assets.managers.DaiKombatAssetsManager;
import com.beverly.hills.money.gang.filters.OverlapFilterManager;
import com.beverly.hills.money.gang.input.GameInputProcessor;
import com.beverly.hills.money.gang.maps.MapBuilder;
import com.beverly.hills.money.gang.models.ModelMaker;
import com.beverly.hills.money.gang.pref.UserPreference;
import com.beverly.hills.money.gang.rect.RectManager;
import com.beverly.hills.money.gang.screens.GameScreen;
import com.beverly.hills.money.gang.screens.MainMenuScreen;
import com.beverly.hills.money.gang.screens.ui.UserSettingsUISelection;
import com.beverly.hills.money.gang.utils.EntityManager;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

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
    private ModelMaker cellBuilder;
    private OverlapFilterManager overlapFilterMan;
    private MapBuilder mapBuilder;
    private float timeSinceLaunch;

    @Override
    public void create() {
        batch = new SpriteBatch();
        mdlBatch = new ModelBatch();
        createNewMainFbo(Constants.FBO_WIDTH_ORIGINAL, Constants.FBO_HEIGHT_ORIGINAL);

        assMan = new DaiKombatAssetsManager();
        assMan.finishLoading();

        overlapFilterMan = new OverlapFilterManager();

        cellBuilder = new ModelMaker(this); // builds models...

        entMan = new EntityManager();
        rectMan = new RectManager(this);

        mapBuilder = new MapBuilder(this);

        Gdx.input.setInputProcessor(new GameInputProcessor());
        // restore user configs
        UserPreference userPreference = new UserPreference();
        UserSettingsUISelection.MOUSE_SENS.getState().setSetting(userPreference.getMouseSensitivity());
        UserSettingsUISelection.SOUND.getState().setSetting(userPreference.getSoundVolume());

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
                .filter(currentScreen -> currentScreen instanceof GameScreen)
                .map(currentScreen -> (GameScreen) currentScreen).ifPresent(GameScreen::exit);

        super.setScreen(screen);
    }
}
