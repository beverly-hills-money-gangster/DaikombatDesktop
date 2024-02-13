package com.beverly.hills.money.gang.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.beverly.hills.money.gang.Constants;
import com.beverly.hills.money.gang.DaiKombatGame;
import com.beverly.hills.money.gang.assets.managers.registry.FontRegistry;
import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;

public class MainMenuScreen extends AbstractMainMenuScreen {

    private final BitmapFont guiFont64;

    private final GlyphLayout glyphLayoutOptionStartGame;
    private final GlyphLayout glyphLayoutOptionQuitGame;

    private final Sound boomSound1;
    private final Sound dingSound1;

    private int selectedOption = 0;


    public MainMenuScreen(final DaiKombatGame game) {
        super(game);
        guiFont64 = game.getAssMan().getFont(FontRegistry.FONT_64);
        glyphLayoutOptionStartGame = new GlyphLayout(guiFont64, Constants.START_GAME);
        glyphLayoutOptionQuitGame = new GlyphLayout(guiFont64, Constants.OPTION_QUIT_GAME);
        boomSound1 = game.getAssMan().getSound(SoundRegistry.BOOM_1);
        dingSound1 = game.getAssMan().getSound(SoundRegistry.DING_1);
    }

    @Override
    public void handleInput(final float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            boomSound1.play(Constants.DEFAULT_SFX_VOLUME);

            if (selectedOption == 0) {
                removeAllEntities();
                getGame().setScreen(new EnterYourNameScreen(getGame()));
            } else {
                Gdx.app.exit();
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            if (--selectedOption < 0) {
                selectedOption = 1;
            }
            dingSound1.play(Constants.DEFAULT_SFX_VOLUME);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            if (++selectedOption > 1) {
                selectedOption = 0;
            }
            dingSound1.play(Constants.DEFAULT_SFX_VOLUME);
        }
    }


    @Override
    public void render(final float delta) {
        super.render(delta);
        getGame().getBatch().begin();
        final float newGameX = getViewport().getWorldWidth() / 2f - glyphLayoutOptionStartGame.width / 2f;
        final float newGameY = getViewport().getWorldHeight() / 2f - glyphLayoutOptionStartGame.height / 2f - Constants.LOGO_INDENT;
        guiFont64.draw(getGame().getBatch(), Constants.START_GAME, newGameX, newGameY);

        final float quitGameX = getViewport().getWorldWidth() / 2f - glyphLayoutOptionQuitGame.width / 2f;
        final float quitGameY = getViewport().getWorldHeight() / 2f - glyphLayoutOptionQuitGame.height / 2f - Constants.LOGO_INDENT
                - Constants.MENU_OPTION_INDENT;
        guiFont64.draw(getGame().getBatch(), Constants.OPTION_QUIT_GAME, quitGameX, quitGameY);

        if (selectedOption == 1) {
            guiFont64.draw(getGame().getBatch(), Constants.SELECTED_OPTION_MARK, quitGameX - Constants.DEFAULT_SELECTION_INDENT, quitGameY);
        } else {
            guiFont64.draw(getGame().getBatch(), Constants.SELECTED_OPTION_MARK, newGameX - Constants.DEFAULT_SELECTION_INDENT, newGameY);
        }
        getGame().getBatch().end();
    }

}
