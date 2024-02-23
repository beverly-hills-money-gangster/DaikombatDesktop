package com.beverly.hills.money.gang.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.beverly.hills.money.gang.Constants;
import com.beverly.hills.money.gang.DaiKombatGame;
import com.beverly.hills.money.gang.assets.managers.registry.FontRegistry;
import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import com.beverly.hills.money.gang.assets.managers.sound.UserSettingSound;
import com.beverly.hills.money.gang.screens.ui.MainMenuUISelection;
import com.beverly.hills.money.gang.screens.ui.UISelection;

public class MainMenuScreen extends AbstractMainMenuScreen {

    private final BitmapFont guiFont64;
    private final UserSettingSound boomSound1;
    private final UserSettingSound dingSound1;

    private final UISelection<MainMenuUISelection> menuSelection
            = new UISelection<>(MainMenuUISelection.values());


    public MainMenuScreen(final DaiKombatGame game) {
        super(game);
        guiFont64 = game.getAssMan().getFont(FontRegistry.FONT_64);

        boomSound1 = game.getAssMan().getSound(SoundRegistry.BOOM_1);
        dingSound1 = game.getAssMan().getSound(SoundRegistry.DING_1);
    }

    @Override
    public void handleInput(final float delta) {

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            boomSound1.play(Constants.DEFAULT_SFX_VOLUME);
            switch (menuSelection.getSelectedOption()) {
                case PLAY -> {
                    removeAllEntities();
                    getGame().setScreen(new EnterYourNameScreen(getGame()));
                }
                case CONTROLS -> {
                    removeAllEntities();
                    getGame().setScreen(new ControlsScreen(getGame()));
                }
                case SETTINGS -> {
                    removeAllEntities();
                    getGame().setScreen(new SettingsScreen(getGame()));
                }
                default -> Gdx.app.exit();

            }
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            menuSelection.up();
            dingSound1.play(Constants.DEFAULT_SFX_VOLUME);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            menuSelection.down();
            dingSound1.play(Constants.DEFAULT_SFX_VOLUME);
        }
    }


    @Override
    public void render(final float delta) {
        super.render(delta);
        getGame().getBatch().begin();
        menuSelection.render(guiFont64, this, Constants.LOGO_INDENT);
        getGame().getBatch().end();
    }

}
